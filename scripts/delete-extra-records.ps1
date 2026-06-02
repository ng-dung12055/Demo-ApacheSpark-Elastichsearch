param(
  [string]$BaseUrl = "http://localhost:8081",
  [string]$DoctorUser = "doctor",
  [string]$DoctorPass = "Doctor@123",
  [string]$PatientCode = "",
  [string]$PatientUser = "patient",
  [string]$PatientPass = "Patient@123",
  [int]$Keep = 2000,
  [int]$ProgressEvery = 100,
  [switch]$DryRun
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

if ($Keep -lt 0) {
  throw "Keep must be >= 0."
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

Write-Host "Loading records for patient $PatientCode..."
$patientDetail = Invoke-Json -Method "GET" -Url "$BaseUrl/api/doctor/patient/$PatientCode" -Headers $headers
$records = @()
if ($patientDetail -and $patientDetail.records) {
  $records = @($patientDetail.records)
}

$total = $records.Count
Write-Host "Found $total records."

if ($total -le $Keep) {
  Write-Host "Nothing to delete. Total <= Keep ($Keep)."
  exit 0
}

$sorted = $records | Sort-Object { [long]$_.recordId } -Descending
$toDelete = $sorted | Select-Object -Skip $Keep

Write-Host ("Deleting {0} records (keeping newest {1})." -f $toDelete.Count, $Keep)
if ($DryRun) {
  Write-Host "DryRun enabled. No deletions performed."
  exit 0
}

$success = 0
$failed = 0
$lastError = ""
$i = 0

foreach ($item in $toDelete) {
  $i++
  $id = $item.recordId
  try {
    Invoke-Json -Method "DELETE" -Url "$BaseUrl/api/doctor/record/$id" -Headers $headers | Out-Null
    $success++
  } catch {
    $failed++
    $lastError = $_.Exception.Message
  }

  if ($ProgressEvery -gt 0 -and ($i % $ProgressEvery) -eq 0) {
    Write-Host "Progress: $i/$($toDelete.Count) | ok=$success | failed=$failed"
  }
}

Write-Host "Done. Deleted: $success | Failed: $failed"
if ($lastError) {
  Write-Host "Last error: $lastError"
}
