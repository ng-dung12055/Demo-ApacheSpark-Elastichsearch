param(
  [string]$EsVersion = "8.11.3",
  [string]$Password = "changeit"
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$certDir = Join-Path $root "certs"

New-Item -ItemType Directory -Force -Path $certDir | Out-Null

$caZip = Join-Path $certDir "ca.zip"
$esZip = Join-Path $certDir "es.zip"

if (-not (Test-Path $caZip)) {
  docker run --rm -u 0 -v "${certDir}:/certs" docker.elastic.co/elasticsearch/elasticsearch:$EsVersion `
    bash -c "elasticsearch-certutil ca --silent --pem -out /certs/ca.zip"
  Expand-Archive -Path $caZip -DestinationPath $certDir -Force
}

if (-not (Test-Path $esZip)) {
  docker run --rm -u 0 -v "${certDir}:/certs" docker.elastic.co/elasticsearch/elasticsearch:$EsVersion `
    bash -c "elasticsearch-certutil cert --silent --pem --ca-cert /certs/ca/ca.crt --ca-key /certs/ca/ca.key --name es01 -out /certs/es.zip"
  Expand-Archive -Path $esZip -DestinationPath $certDir -Force
}

$sparkKeyStore = Join-Path $certDir "spark-keystore.jks"
$sparkTrustStore = Join-Path $certDir "spark-truststore.jks"
$sparkCer = Join-Path $certDir "spark.cer"

if (-not (Test-Path $sparkKeyStore)) {
  keytool -genkeypair -alias spark -keyalg RSA -keystore $sparkKeyStore `
    -storepass $Password -keypass $Password -dname "CN=spark"
}

if (-not (Test-Path $sparkCer)) {
  keytool -export -alias spark -keystore $sparkKeyStore -storepass $Password -file $sparkCer
}

if (-not (Test-Path $sparkTrustStore)) {
  keytool -import -alias spark -file $sparkCer -keystore $sparkTrustStore -storepass $Password -noprompt
}

Write-Host "Certificates generated under $certDir"
