param(
  [string]$BaseUrl = "http://localhost:8081",
  [string]$DoctorUser = "doctor",
  [string]$DoctorPass = "Doctor@123",
  [string]$PatientCode = "",
  [string]$PatientUser = "patient",
  [string]$PatientPass = "Patient@123",
  [int]$Count = 2000,
  [int]$ProgressEvery = 100,
  [int]$DelayMs = 0,
  [switch]$SkipTlsVerify
)

$ErrorActionPreference = "Stop"

function Normalize-BaseUrl {
  param([string]$Url)
  if ($Url.EndsWith("/")) { return $Url.TrimEnd("/") }
  return $Url
}

function Invoke-Json {
  param(
    [string]$Method,
    [string]$Url,
    [hashtable]$Headers = $null,
    [object]$Body = $null
  )
  $invokeParams = @{
    Method = $Method
    Uri = $Url
    Headers = $Headers
    ContentType = "application/json"
  }
  if ($SkipTlsVerify) {
    $cmd = Get-Command Invoke-RestMethod
    if ($cmd.Parameters.ContainsKey("SkipCertificateCheck")) {
      $invokeParams.SkipCertificateCheck = $true
    } else {
      [System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }
    }
  }
  if ($Body -ne $null) {
    $invokeParams.Body = ($Body | ConvertTo-Json -Depth 6)
  }
  return Invoke-RestMethod @invokeParams
}

function Login {
  param(
    [string]$BaseUrl,
    [string]$Username,
    [string]$Password
  )
  $payload = @{ username = $Username; password = $Password }
  return Invoke-Json -Method "POST" -Url "$BaseUrl/api/auth/login" -Body $payload
}

$BaseUrl = Normalize-BaseUrl $BaseUrl
if ($BaseUrl.StartsWith("https://")) {
  try {
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
  } catch {
  }
}

if (-not $PatientCode) {
  Write-Host "Fetching patient code via patient login..."
  $patientLogin = Login -BaseUrl $BaseUrl -Username $PatientUser -Password $PatientPass
  $PatientCode = $patientLogin.patientCode
  if (-not $PatientCode) {
    throw "Patient code not returned. Provide -PatientCode explicitly."
  }
  Write-Host "Using patient code: $PatientCode"
}

Write-Host "Logging in as doctor..."
$doctorLogin = Login -BaseUrl $BaseUrl -Username $DoctorUser -Password $DoctorPass
$token = $doctorLogin.token
if (-not $token) {
  throw "Doctor token missing. Check doctor credentials."
}

$headers = @{
  Authorization = "Bearer $token"
}

$keywordsPool = @(
  "sot", "ho", "dau-dau", "met-moi", "dau-hong", "viem", "nhiem",
  "suc-khoe", "cum", "covid", "tieu-hoa", "huyet-ap",
  "tim-mach", "noi-tiet", "da-lieu", "nhi-khoa"
)
$levels = @("PUBLIC", "CONFIDENTIAL", "RESTRICTED")

$success = 0
$failed = 0
$lastError = ""

for ($i = 1; $i -le $Count; $i++) {
  $kwCount = Get-Random -Minimum 2 -Maximum 5
  $keywords = (Get-Random -InputObject $keywordsPool -Count $kwCount) -join ","
  $level = $levels[(Get-Random -Minimum 0 -Maximum $levels.Count)]
  $diagnosis = "Demo diagnosis #$i"
  $summary = "Demo summary #$i - $keywords"

  $payload = @{
    patientCode = $PatientCode
    diagnosis = $diagnosis
    keywords = $keywords
    summary = $summary
    securityLevel = $level
  }

  try {
    Invoke-Json -Method "POST" -Url "$BaseUrl/api/doctor/record" -Headers $headers -Body $payload | Out-Null
    $success++
  } catch {
    $failed++
    $lastError = $_.Exception.Message
  }

  if ($ProgressEvery -gt 0 -and ($i % $ProgressEvery) -eq 0) {
    Write-Host "Progress: $i/$Count | ok=$success | failed=$failed"
  }

  if ($DelayMs -gt 0) {
    Start-Sleep -Milliseconds $DelayMs
  }
}

Write-Host "Done. Created: $success | Failed: $failed"
if ($lastError) {
  Write-Host "Last error: $lastError"
}
