#!/bin/bash
# ================================================================
# WireMock Setup Script for EAS MFA (Okta Push) Simulation
# ================================================================
# UPDATED: Fixed keystore to use PKCS12 format + correct password
#
# Usage:
#   ./setup-wiremock.sh [port]
#   ./setup-wiremock.sh 8443
#   ./setup-wiremock.sh 9443
# ================================================================

set -e

WIREMOCK_VERSION="3.5.4"
WIREMOCK_PORT="${1:-8443}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WIREMOCK_DIR="${SCRIPT_DIR}/../wiremock"
WIREMOCK_JAR="wiremock-standalone-${WIREMOCK_VERSION}.jar"
KEYSTORE="${WIREMOCK_DIR}/wiremock-keystore.jks"
KEYSTORE_PASS="password"

echo "============================================"
echo " WireMock EAS MFA Setup"
echo "============================================"
echo " Port:     ${WIREMOCK_PORT}"
echo " Root Dir: ${WIREMOCK_DIR}"
echo "============================================"
echo ""

# ----------------------------------------------------------
# Step 1: Download WireMock JAR if not present
# ----------------------------------------------------------
if [ ! -f "${WIREMOCK_DIR}/${WIREMOCK_JAR}" ]; then
    echo "[1/5] Downloading WireMock ${WIREMOCK_VERSION}..."
    curl -L -o "${WIREMOCK_DIR}/${WIREMOCK_JAR}" \
        "https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/${WIREMOCK_VERSION}/wiremock-standalone-${WIREMOCK_VERSION}.jar"
    echo "      Downloaded: $(ls -lh "${WIREMOCK_DIR}/${WIREMOCK_JAR}" | awk '{print $5}')"
else
    echo "[1/5] WireMock JAR already present ($(ls -lh "${WIREMOCK_DIR}/${WIREMOCK_JAR}" | awk '{print $5}'))."
fi

# ----------------------------------------------------------
# Step 2: Always regenerate keystore (delete old + create new)
#         Uses PKCS12 format required by WireMock 3.x
# ----------------------------------------------------------
echo "[2/5] Generating PKCS12 TLS keystore..."
rm -f "${KEYSTORE}"

keytool -genkeypair \
    -alias wiremock \
    -keyalg RSA \
    -keysize 2048 \
    -validity 365 \
    -storetype PKCS12 \
    -keystore "${KEYSTORE}" \
    -storepass "${KEYSTORE_PASS}" \
    -keypass "${KEYSTORE_PASS}" \
    -dname "CN=localhost, OU=PerfTest, O=Zimbra, L=Pune, ST=MH, C=IN" \
    -ext "san=dns:localhost,ip:127.0.0.1"

echo "      Keystore: ${KEYSTORE}"
echo "      Type:     PKCS12"
echo "      Password: ${KEYSTORE_PASS}"

# ----------------------------------------------------------
# Step 3: Verify keystore is valid
# ----------------------------------------------------------
echo "[3/5] Verifying keystore..."
keytool -list -keystore "${KEYSTORE}" \
    -storepass "${KEYSTORE_PASS}" \
    -storetype PKCS12 2>/dev/null | grep -q "wiremock"

if [ $? -eq 0 ]; then
    echo "      ✅ Keystore valid — alias 'wiremock' found"
else
    echo "      ❌ Keystore verification failed!"
    exit 1
fi

# ----------------------------------------------------------
# Step 4: Verify mapping files
# ----------------------------------------------------------
MAPPING_COUNT=$(ls -1 "${WIREMOCK_DIR}/mappings/"*.json 2>/dev/null | wc -l)
RESPONSE_COUNT=$(ls -1 "${WIREMOCK_DIR}/__files/"*.json 2>/dev/null | wc -l)
echo "[4/5] Found ${MAPPING_COUNT} stub mappings and ${RESPONSE_COUNT} response files."

if [ "$MAPPING_COUNT" -eq 0 ]; then
    echo "      ⚠️  WARNING: No mappings found in ${WIREMOCK_DIR}/mappings/"
    echo "      WireMock will start but won't simulate any MFA flows."
fi

# ----------------------------------------------------------
# Step 5: Kill any existing WireMock + start fresh
# ----------------------------------------------------------
echo "[5/5] Starting WireMock on HTTPS port ${WIREMOCK_PORT}..."

# Kill any previous WireMock instance
pkill -f "wiremock-standalone" 2>/dev/null && sleep 2 && echo "      Killed previous WireMock instance" || true

# Check if port is free
if ss -tlnp 2>/dev/null | grep -q ":${WIREMOCK_PORT} "; then
    echo "      ❌ Port ${WIREMOCK_PORT} is still in use!"
    echo "      Run: sudo lsof -i :${WIREMOCK_PORT}"
    echo "      Kill the process and retry."
    exit 1
fi

echo ""
echo "  ┌──────────────────────────────────────────┐"
echo "  │  WireMock Starting...                    │"
echo "  │  HTTPS: https://localhost:${WIREMOCK_PORT}       │"
echo "  │  Admin: https://localhost:${WIREMOCK_PORT}/__admin│"
echo "  │  Press Ctrl+C to stop                    │"
echo "  └──────────────────────────────────────────┘"
echo ""

java -jar "${WIREMOCK_DIR}/${WIREMOCK_JAR}" \
    --port 0 \
    --https-port "${WIREMOCK_PORT}" \
    --https-keystore "${KEYSTORE}" \
    --keystore-type PKCS12 \
    --keystore-password "${KEYSTORE_PASS}" \
    --key-manager-password "${KEYSTORE_PASS}" \
    --root-dir "${WIREMOCK_DIR}" \
    --global-response-templating \
    --verbose \
    --async-response-enabled true \
    --async-response-threads 16
