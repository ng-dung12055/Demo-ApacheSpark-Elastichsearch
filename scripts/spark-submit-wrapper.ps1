param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Args
)

$ErrorActionPreference = "Stop"
$debugEnabled = $env:SPARK_WRAPPER_DEBUG -eq "1"

if (-not $Args -or $Args.Count -lt 1) {
    Write-Error "Missing spark-submit arguments."
    exit 2
}

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Resolve-Path (Join-Path $scriptRoot "..")

function Resolve-CaCertPath {
    param(
        [string]$PathValue,
        [string]$RepoRootPath
    )

    try {
        $resolved = Resolve-Path -Path $PathValue -ErrorAction Stop
    } catch {
        return $PathValue
    }

    $certsRoot = $null
    try {
        $certsRoot = Resolve-Path -Path (Join-Path $RepoRootPath "certs") -ErrorAction Stop
    } catch {
        $certsRoot = $null
    }

    if ($certsRoot -and $resolved.Path.StartsWith($certsRoot.Path, [System.StringComparison]::OrdinalIgnoreCase)) {
        $relative = $resolved.Path.Substring($certsRoot.Path.Length).TrimStart("\", "/")
        return ("/opt/bitnami/spark/certs/" + ($relative -replace "\\", "/"))
    }

    return $PathValue
}

function Normalize-EsUrl {
    param([string]$UrlValue)
    if ([string]::IsNullOrWhiteSpace($UrlValue)) {
        return $UrlValue
    }
    $normalized = $UrlValue -replace "^(https?://)(localhost|127\\.0\\.0\\.1|0\\.0\\.0\\.0)(:)", "`$1elasticsearch`$3"
    $normalized = $normalized -replace "^(https?://)\\[::1\\](:)", "`$1elasticsearch`$2"
    return $normalized
}

function Normalize-JdbcUrl {
    param([string]$UrlValue)
    if ([string]::IsNullOrWhiteSpace($UrlValue)) {
        return $UrlValue
    }

    if ($UrlValue -notmatch '^jdbc:mysql://([^/:]+)(?::(\d+))?(/.*)$') {
        return $UrlValue
    }

    $jdbcHost = $Matches[1]
    $jdbcPort = $Matches[2]
    $suffix = $Matches[3]

    if ($jdbcHost -notin @("localhost", "127.0.0.1", "0.0.0.0")) {
        return $UrlValue
    }

    $overrideHost = $env:SPARK_JDBC_HOST
    $overridePort = $env:SPARK_JDBC_PORT
    if (-not $overrideHost) {
        try {
            $mysqlContainer = docker ps --format "{{.Names}}" | Select-String -SimpleMatch "mysql-hospital" | Select-Object -First 1
            if ($mysqlContainer) {
                $overrideHost = "mysql-hospital"
                if (-not $overridePort) {
                    $overridePort = "3306"
                }
            }
        } catch {
        }
    }
    if (-not $overrideHost) {
        $overrideHost = "host.docker.internal"
    }
    if (-not $overridePort) {
        $overridePort = $jdbcPort
    }

    if ($overridePort) {
        return ("jdbc:mysql://{0}:{1}{2}" -f $overrideHost, $overridePort, $suffix)
    }
    return ("jdbc:mysql://{0}{1}" -f $overrideHost, $suffix)
}

$container = $env:SPARK_DOCKER_CONTAINER
if (-not $container) {
    try {
        $container = docker ps --format "{{.Names}}" | Select-String -SimpleMatch "spark-master" | Select-Object -First 1 | ForEach-Object { $_.Line }
    } catch {
        $container = $null
    }
}
if (-not $container) {
    $container = "sanpham-spark-master-1"
}

$jobJarArg = $Args[-1]
try {
    $jobJarPath = Resolve-Path -Path $jobJarArg -ErrorAction Stop
} catch {
    try {
        $jobJarPath = Resolve-Path -Path (Join-Path $repoRoot $jobJarArg) -ErrorAction Stop
    } catch {
        Write-Error "Job jar not found: $jobJarArg"
        exit 2
    }
}

$jobJarName = Split-Path -Leaf $jobJarPath
$containerJarPath = "/opt/bitnami/spark/$jobJarName"

& docker cp $jobJarPath "${container}:${containerJarPath}"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$envKeys = @(
    "JDBC_URL",
    "JDBC_USER",
    "JDBC_PASSWORD",
    "JDBC_DRIVER",
    "ES_URL",
    "ES_INDEX",
    "ES_USER",
    "ES_PASSWORD",
    "ES_CA_CERT"
)

$execArgs = @("exec")
foreach ($key in $envKeys) {
    $value = [Environment]::GetEnvironmentVariable($key)
    if ([string]::IsNullOrWhiteSpace($value)) {
        continue
    }
    if ($key -eq "ES_CA_CERT") {
        $value = Resolve-CaCertPath -PathValue $value -RepoRootPath $repoRoot
    }
    if ($key -eq "ES_URL") {
        $value = Normalize-EsUrl -UrlValue $value
    }
    if ($key -eq "JDBC_URL") {
        $value = Normalize-JdbcUrl -UrlValue $value
    }
    if ($debugEnabled) {
        Write-Host ("spark-wrapper env {0}={1}" -f $key, $value)
    }
    $execArgs += "-e"
    $execArgs += ("{0}={1}" -f $key, $value)
}

$execArgs += $container
$execArgs += "spark-submit"

$submitArgs = @($Args)
$submitArgs[-1] = $containerJarPath
$execArgs += $submitArgs

& docker @execArgs
exit $LASTEXITCODE
