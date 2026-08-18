#!/bin/bash
# ================================================================
# WireMock Delay Controller
# ================================================================
# Dynamically change MFA push approval delay to simulate:
#   - Fast approval (1-2s)   → normal operation
#   - Slow approval (10-15s) → user delay scenario
#   - Timeout (30s+)         → push expiry scenario
# ================================================================

WIREMOCK_HOST="${1:-localhost}"
WIREMOCK_PORT="${2:-8443}"

echo "============================================"
echo " WireMock MFA Delay Controller"
echo "============================================"
echo ""
echo "Options:"
echo "  1) Fast approval    (200ms delay per poll, ~1.2s total MFA)"
echo "  2) Normal approval  (2s delay per poll, ~6s total MFA)"
echo "  3) Slow approval    (5s delay per poll, ~15s total MFA)"
echo "  4) Timeout scenario (15s delay per poll, will exceed 30s timeout)"
echo ""
read -p "Select scenario [1-4]: " CHOICE

case $CHOICE in
    1) DELAY=200;  DESC="Fast approval" ;;
    2) DELAY=2000; DESC="Normal approval" ;;
    3) DELAY=5000; DESC="Slow approval" ;;
    4) DELAY=15000; DESC="Timeout scenario" ;;
    *) echo "Invalid choice"; exit 1 ;;
esac

echo ""
echo "Setting poll delay to ${DELAY}ms (${DESC})..."

# Update WireMock mapping via Admin API
curl -sk -X PUT "https://${WIREMOCK_HOST}:${WIREMOCK_PORT}/__admin/settings" \
    -H "Content-Type: application/json" \
    -d "{
        \"fixedDelay\": ${DELAY}
    }"

echo ""
echo "✅ WireMock poll delay set to ${DELAY}ms"
echo "   Expected total MFA time: ~$((DELAY * 3 / 1000))s (3 polls)"
