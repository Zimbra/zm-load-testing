#!/bin/bash
# ================================================================
# Configure Zimbra Domain for MFA with WireMock
# ================================================================
# Run this ON THE ZIMBRA SERVER as the 'zimbra' user.
# This points Zimbra's auth mechanism to WireMock instead of Okta.
# ================================================================

set -e

# ==================== CONFIGURATION ====================
DOMAIN="${1:-platform-dev.zimbradev.com}"
WIREMOCK_HOST="${2:-localhost}"
WIREMOCK_PORT="${3:-8443}"
CLIENT_ID="${4:-0oa13t1q33u0efXBo698}"

echo "============================================"
echo " Zimbra EAS MFA Configuration"
echo "============================================"
echo " Domain:       $DOMAIN"
echo " WireMock:     https://${WIREMOCK_HOST}:${WIREMOCK_PORT}"
echo " Client ID:    $CLIENT_ID"
echo "============================================"
echo ""

# Step 1: Set auth mechanism to custom:idp-ropc pointing to WireMock
echo "[1/5] Setting zimbraAuthMech to custom:idp-ropc..."
zmprov md "$DOMAIN" zimbraAuthMech \
    "custom:idp-ropc \"token_endpoint=https://${WIREMOCK_HOST}:${WIREMOCK_PORT}/oauth2/v1/token\" \"client_id=${CLIENT_ID}\" \"provider=okta\" \"polling_interval=5\" \"polling_timeout=30\""

echo "      ✅ Done"

# Step 2: Configure local config for MFA threading
echo "[2/5] Setting MFA local config values..."
zmlocalconfig -e mfa_idp_max_connection_allowed=1000
zmlocalconfig -e mfa_idp_max_retry_wait_timeout=5000
zmlocalconfig -e mfa_idp_pool_max_size=32

echo "      ✅ Done"

# Step 3: Trust WireMock's self-signed cert (CRITICAL)
echo "[3/5] Importing WireMock cert into Zimbra truststore..."

# Export WireMock cert
echo | openssl s_client -connect ${WIREMOCK_HOST}:${WIREMOCK_PORT} 2>/dev/null | \
    openssl x509 > /tmp/wiremock-cert.pem

# Import into Zimbra's cacerts
ZIMBRA_CACERTS="/opt/zimbra/common/lib/jvm/java/lib/security/cacerts"
keytool -import -trustcacerts \
    -alias wiremock-perf \
    -file /tmp/wiremock-cert.pem \
    -keystore "$ZIMBRA_CACERTS" \
    -storepass changeit \
    -noprompt 2>/dev/null || echo "      (cert may already be imported)"

echo "      ✅ Done"

# Step 4: Enable fallback to local auth (safety net)
echo "[4/5] Enabling fallback to local auth..."
zmprov md "$DOMAIN" +zimbraAuthFallbackToLocal TRUE

echo "      ✅ Done"

# Step 5: Restart mailboxd to pick up changes
echo "[5/5] Restarting mailboxd..."
zmmailboxdctl restart

echo ""
echo "============================================"
echo " ✅ Configuration Complete"
echo "============================================"
echo ""
echo "Verify with:"
echo "  zmprov gd $DOMAIN zimbraAuthMech"
echo ""
echo "Test single auth:"
echo "  curl -k -u testuser@${DOMAIN}:password \\
      https://localhost:443/Microsoft-Server-ActiveSync"
echo ""
