# JMeter Load Test Evidence

## Test plan

File: `load_test.jmx`

| Setting | Value |
|---------|-------|
| Endpoint | `POST http://localhost:8080/movements` |
| Threads | 100 |
| Ramp-up | 10 seconds |
| Loops | 10 per thread |
| Total requests | 1000 |
| Payload | Deposit on seed account `11111111-1111-1111-1111-111111111111` |
| Auth | JWT for user `1710034065` (role: `user`, not admin) |

## Why you get 401 Unauthorized in GUI

`POST /movements` **requires** a valid JWT in the `Authorization` header:

```
Authorization: Bearer <token>
```

The test plan sends `Bearer ${__P(JWT_TOKEN)}`. That property is **empty** unless:

1. The **Setup Thread Group** runs first and logs in (automatic in GUI), or
2. You pass `-JJWT_TOKEN=...` on the CLI (as `run-load-test.sh` does).

If you open JMeter and click **Run** without a token, the header becomes literally `Bearer ` (empty), and Spring Security returns **401 Unauthorized** — exactly what you saw in View Results Tree.

Cookies are not used; only the Bearer token matters.

> **Important:** use a **user** token (`1710034065`), not admin. Administrators get **403** on movement creation (catalog `ERR-003`).

### Troubleshooting: `Authorization: Bearer 1` (or other non-JWT values)

If **View Results Tree** shows `Authorization: Bearer 1` (or any short value that is not a long JWT starting with `eyJ`), the **JSON Extractor** in **Setup - Obtain JWT** is reading the wrong field.

The login API wraps the payload in `ApiResponse`:

```json
{
  "code": "OK-000",
  "type": "success",
  "message": "...",
  "data": { "token": "eyJ...", "clientId": 1, ... }
}
```

The extractor must use **`$.data.token`**, not `$.token`. With the old path, JMeter may fail to find the token or pick a wrong value (e.g. `clientId` = `1`). Every movement request then returns **401 Unauthorized** even though login returns HTTP 200.

**Fix:** reload `load_test.jmx` from the repo (path already updated) or edit **Extract JWT Token** → JSON Path Expressions → `$.data.token`. Re-run from a clean start (no stale `-JJWT_TOKEN=1` on the CLI).

**Quick check:** after Setup runs, the Authorization header on **POST Create Movement** should look like `Bearer eyJhbGciOiJ...` (hundreds of characters), not `Bearer 1`.

## Quick run (recommended, CLI)

```bash
./deploy.sh --skip-tests          # stack must be up
./scripts/run-load-test.sh        # fetches JWT, runs test, writes report + EVIDENCE.md
open docs/jmeter/report/index.html
```

The script automatically:

1. Checks `GET /actuator/health`
2. Logs in via `POST /auth/login` and passes the token to JMeter (`-JJWT_TOKEN`)
3. Generates `docs/jmeter/report/index.html` (HTML dashboard)
4. Copies `docs/jmeter/statistics.json` and writes `docs/jmeter/EVIDENCE.md`

## GUI execution (step by step)

### 1. Start the API

```bash
./deploy.sh --skip-tests
curl http://localhost:8080/actuator/health   # must return {"status":"UP"}
```

### 2. Open JMeter

```bash
./start-jmeter.sh
```

Or manually:

```bash
./tools/apache-jmeter-5.6.3/bin/jmeter -t load_test.jmx
```

### 3. Verify test plan variables

In the tree, select **Medianet Core Banking Load Test** → **User Defined Variables**:

| Variable | Default | Purpose |
|----------|---------|---------|
| `API_HOST` | `localhost` | API hostname |
| `API_PORT` | `8080` | API port |
| `LOGIN_ID` | `1710034065` | User (not admin) |
| `LOGIN_PASSWORD` | `1234` | Password |

### 4. Run the test

1. Enable **View Results Tree** (already in the plan).
2. Click the green **Start** button (or **Run → Start**).
3. JMeter runs **Setup - Obtain JWT** first:
   - `POST /auth/login` → extracts `$.data.token` → sets property `JWT_TOKEN`.
4. Then **Concurrent Users** sends `POST /movements` with `Authorization: Bearer <token>`.

### 5. Check results

- **Setup - POST Auth Login** → green, HTTP 200.
- **POST Create Movement** → green, HTTP 200.
- Response JSON includes `movementId`, `movementType`, `balance`, `clientName`.

If Setup fails (red), the API is down or credentials are wrong — fix that before load testing.

### Optional: smoke test with 1 thread

Before running 100×10 requests, reduce load for a quick check:

1. **Concurrent Users** → Number of Threads: `1`, Loop Count: `1`.
2. Run → confirm 200 OK.
3. Restore `100` threads and `10` loops for the full load test.

## Manual verification with curl

```bash
# Without token → 401
curl -s -o /dev/null -w "%{http_code}\n" \
  -X POST http://localhost:8080/movements \
  -H "Content-Type: application/json" \
  -d '{"accountId":"11111111-1111-1111-1111-111111111111","movementType":"DEPOSITO","value":10.00}'

# With user token → 200
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identification":"1710034065","password":"1234"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

curl -s -X POST http://localhost:8080/movements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"accountId":"11111111-1111-1111-1111-111111111111","movementType":"DEPOSITO","value":10.00}'
```

## Manual CLI

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identification":"1710034065","password":"1234"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

./tools/apache-jmeter-5.6.3/bin/jmeter -n -t load_test.jmx \
  -l docs/jmeter/results.jtl \
  -e -o docs/jmeter/report \
  -JJWT_TOKEN="$TOKEN"
```

> The Setup Thread Group also performs login, so `-JJWT_TOKEN` is optional for CLI runs.

## Expected result

- HTTP 200 for successful movement creation
- Response JSON contains `movementId`, `movementType`, `balance`, `clientName`
- Administrators receive HTTP 403 if attempting to create movements

## Committed artifacts

| File | Purpose |
|------|---------|
| `load_test.jmx` | Test plan (includes automatic JWT setup) |
| `docs/jmeter/EVIDENCE.md` | Summary metrics for reviewers |
| `docs/jmeter/statistics.json` | Raw aggregated stats from last run |
| `docs/jmeter/report/` | Full HTML report (generated locally, gitignored) |
