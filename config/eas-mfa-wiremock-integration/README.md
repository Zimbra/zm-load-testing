# EAS MFA (Okta Push) Performance Test Kit
## Using WireMock + JMeter

---

## Architecture

```
┌─────────────────┐     Basic Auth      ┌──────────────────┐     ROPC + Poll    ┌──────────────────┐
│   JMeter         │ ──────────────────► │  Zimbra Server   │ ──────────────────► │  WireMock        │
│   (EAS Clients)  │     EAS Protocol   │  (mailboxd)      │   OAuth2/Push     │  (Fake Okta)     │
│                  │ ◄────────────────── │  custom:idp-ropc │ ◄────────────────── │  Port 8443       │
└─────────────────┘     Sync Response   └──────────────────┘   MFA Result       └──────────────────┘
```

## What Gets Simulated

| Real Okta Flow | WireMock Simulation |
|----------------|---------------------|
| ROPC password validation | Returns `mfa_required` with factor details |
| Push notification to device | Returns `WAITING` challenge |
| User approves on phone | After 2 polls, returns `SUCCESS` |
| User rejects push | Returns `REJECTED` (configurable) |
| Push times out | Returns `TIMEOUT` (configurable) |

## Quick Start

### Step 1: Start WireMock (on test infra)
```bash
cd scripts/
./setup-wiremock.sh 8443
```

### Step 2: Configure Zimbra (on proxy server, as zimbra user)
```bash
cd scripts/
./configure-zimbra-mfa.sh platform-dev.zimbradev.com <wiremock-host> 8443
```

### Step 3: Run  WireMock MFA Delay Controller
```bash
./scripts/control-mfa-delay.sh <wiremock_host> 8443
============================================
 WireMock MFA Delay Controller
 Target: https://<wiremock_host>:8443
============================================

Options:
  1) Fast approval    (200ms  delay)  → ~0.6s   total MFA
  2) Normal approval  (2000ms delay)  → ~6s     total MFA
  3) Slow approval    (5000ms delay)  → ~15s    total MFA
  4) Timeout scenario (15000ms delay) → exceeds 30s polling_timeout
  5) 1 minute delay   (60000ms delay) → extreme latency test
  6) 5 minute delay   (300000ms delay)→ user away scenario
  7) 10 minute delay  (600000ms delay)→ abandoned push scenario

Select scenario [1-7]: 1

Setting MFA stub delays to 200ms (Fast approval)...

Fetching stub IDs...
  OOB poll stub ID:      83206843-d3c7-435f-8aef-d4949e74e22b
  Challenge stub ID:     0ee1fa40-37fa-4968-bc68-b252c6cdf5e2
  Refresh token stub ID: cd3106cb-519a-407e-a233-346035de0ce8

Updating stub delays...
  ✅ OOB poll stub      → 200ms
  ✅ Challenge stub     → 200ms (fixed)
  ✅ Refresh token stub → 250ms (fixed — cached path)

  ✅ Stubs persisted to disk

============================================
 Scenario : Fast approval
 OOB delay: 200ms per poll
 Est. MFA : ~.6s (3 polls)
 Timeout  : 30s (polling_timeout in zimbraAuthMech)
============================================

```

## File Structure

```
eas-mfa-perf-kit/
├── wiremock/
│   ├── mappings/
│   │   ├── 01-ropc-token-mfa-required.json    # ROPC → mfa_required
│   │   ├── 02-push-trigger.json                # Push notification trigger
│   │   ├── 03-poll-waiting-1.json              # 1st poll → WAITING
│   │   ├── 04-poll-waiting-2.json              # 2nd poll → WAITING
│   │   ├── 05-poll-success.json                # 3rd poll → SUCCESS
│   │   └── 06-ropc-invalid-creds.json          # Bad password → 401
|   |   └── 07-refresh-token.json
│   └── __files/
│       ├── ropc-mfa-required.json              # ROPC response body
│       ├── push-challenge-created.json         # Push trigger response
│       ├── poll-waiting.json                   # Poll WAITING response
│       ├── poll-success.json                   # Poll SUCCESS response
│       ├── poll-rejected.json                  # Poll REJECTED response
│       └── poll-timeout.json                   # Poll TIMEOUT response
│
│
├── scripts/
│   ├── setup-wiremock.sh                       # Download & start WireMock
│   ├── configure-zimbra-mfa.sh                 # Configure Zimbra for MFA
│   ├── provision-eas-users.sh                  # Create test users + CSV
│   ├── run-test.sh                             # Run JMeter non-GUI
│   └── control-mfa-delay.sh                    # Change MFA approval timing
│
└── README.md                                   # This file
```

## WireMock Scenario State Machine

```
Push-Poll-Flow:

  ┌─────────┐    1st poll    ┌──────────────┐    2nd poll    ┌──────────────┐    3rd poll    ┌─────────┐
  │ Started  │ ────────────► │ Poll-1-Done  │ ────────────► │ Poll-2-Done  │ ────────────► │ Started │
  │          │   → WAITING   │              │   → WAITING   │              │   → SUCCESS   │ (reset) │
  └─────────┘               └──────────────┘               └──────────────┘               └─────────┘
```

The scenario resets to `Started` after SUCCESS, so the next user gets the full 3-poll cycle.


### WireMock Timing

| Stub | Default Delay | Configurable? |
|------|---------------|---------------|
| ROPC Token | 50ms | Yes (fixedDelayMilliseconds in mapping) |
| Push Trigger | 100ms | Yes |
| Poll WAITING | 200ms | Yes |
| Poll SUCCESS | 100ms | Yes |

Use `control-mfa-delay.sh` to dynamically change delays during a test.


## Troubleshooting

| Issue | Cause | Fix |
|-------|-------|-----|
| HTTP 401 on all requests | WireMock not running or cert not trusted | Check WireMock logs; re-import cert |
| All requests timeout | WireMock poll delay too high | Use control-mfa-delay.sh |
| "Connection refused" in Zimbra | token_endpoint pointing to wrong host | Check zimbraAuthMech config |
| SyncKey errors (Status 3) | Sync state corruption | Reset SyncKey to 0 in JMeter |
| HTTP 449 (Provision Required) | PolicyKey not sent or expired | Check X-MS-PolicyKey header extraction |
