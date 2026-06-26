#!/bin/bash
# Run JMeter load test in non-GUI mode and generate HTML report
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
JMETER="$ROOT_DIR/tools/apache-jmeter-5.6.3/bin/jmeter"
OUT_DIR="$ROOT_DIR/docs/jmeter/report"
JTL="$ROOT_DIR/docs/jmeter/results.jtl"
STATS="$ROOT_DIR/docs/jmeter/statistics.json"
EVIDENCE="$ROOT_DIR/docs/jmeter/EVIDENCE.md"
API_URL="${API_URL:-http://localhost:8080}"
LOGIN_ID="${LOGIN_ID:-1710034065}"
LOGIN_PASSWORD="${LOGIN_PASSWORD:-1234}"

if [ ! -x "$JMETER" ]; then
  echo "JMeter not found at $JMETER"
  echo "See tools/README.md for installation instructions."
  exit 1
fi

echo "Checking API health at $API_URL ..."
curl -sf "$API_URL/actuator/health" > /dev/null || {
  echo "API is not reachable. Start the stack first: ./deploy.sh --skip-tests"
  exit 1
}

echo "Fetching JWT for load test user ($LOGIN_ID) ..."
JWT=$(curl -sf -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"identification\":\"$LOGIN_ID\",\"password\":\"$LOGIN_PASSWORD\"}" \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['token'])")

mkdir -p "$ROOT_DIR/docs/jmeter"
rm -rf "$OUT_DIR" "$JTL"

echo "Running load test against POST /movements (100 threads x 10 loops = 1000 requests) ..."
"$JMETER" -n -t "$ROOT_DIR/load_test.jmx" -l "$JTL" -e -o "$OUT_DIR" -JJWT_TOKEN="$JWT"

if [ -f "$OUT_DIR/statistics.json" ]; then
  cp "$OUT_DIR/statistics.json" "$STATS"
fi

python3 - "$JTL" "$STATS" "$EVIDENCE" "$API_URL" "$LOGIN_ID" <<'PY'
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

jtl, stats_path, evidence_path, api_url, login_id = sys.argv[1:6]
lines = Path(jtl).read_text(encoding="utf-8", errors="ignore").strip().splitlines()
rows = []
if len(lines) > 1:
    headers = lines[0].split(",")
    for line in lines[1:]:
        parts = line.split(",")
        if len(parts) >= len(headers):
            rows.append(dict(zip(headers, parts)))

total = len(rows)
errors = sum(1 for r in rows if r.get("success", "").lower() == "false")
codes = {}
for r in rows:
    code = r.get("responseCode", "?")
    codes[code] = codes.get(code, 0) + 1

stats = {}
if Path(stats_path).exists():
    stats = json.loads(Path(stats_path).read_text(encoding="utf-8"))

def metric(name, field):
    item = stats.get(name, {})
    val = item.get(field, "n/a")
    if isinstance(val, float):
        return f"{val:.2f}"
    return val

content = f"""# JMeter Load Test Evidence

Generated: {datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")}

## Configuration

| Setting | Value |
|---------|-------|
| Endpoint | `POST {api_url}/movements` |
| Test plan | `load_test.jmx` |
| Threads | 100 |
| Ramp-up | 10 seconds |
| Loops per thread | 10 |
| Total samples | {total} |
| Auth user | `{login_id}` (role: user) |

## Results summary

| Metric | Value |
|--------|-------|
| Total requests | {total} |
| Successful | {total - errors} |
| Failed | {errors} |
| Error rate | {(errors / total * 100) if total else 0:.2f}% |
| HTTP 200 | {codes.get("200", 0)} |
| Other codes | {", ".join(f"{k}: {v}" for k, v in sorted(codes.items()) if k != "200") or "none"} |

## Aggregated metrics (JMeter report)

| Metric | Value |
|--------|-------|
| Mean response time | {metric("POST Create Movement", "meanResTime")} ms |
| Median response time | {metric("POST Create Movement", "medianResTime")} ms |
| 90th percentile | {metric("POST Create Movement", "pct1ResTime")} ms |
| 95th percentile | {metric("POST Create Movement", "pct2ResTime")} ms |
| 99th percentile | {metric("POST Create Movement", "pct3ResTime")} ms |
| Throughput | {metric("POST Create Movement", "throughput")} req/s |
| Min / Max | {metric("POST Create Movement", "minResTime")} / {metric("POST Create Movement", "maxResTime")} ms |

## How to reproduce

```bash
./deploy.sh --skip-tests
./scripts/run-load-test.sh
open docs/jmeter/report/index.html
```

## Artifacts

- Raw results: `docs/jmeter/results.jtl` (gitignored)
- HTML dashboard: `docs/jmeter/report/index.html` (gitignored, generated locally)
- Statistics JSON: `docs/jmeter/statistics.json`
"""

Path(evidence_path).write_text(content, encoding="utf-8")
print(f"Evidence written to {evidence_path}")
PY

echo ""
echo "Done."
echo "  HTML report : $OUT_DIR/index.html"
echo "  Evidence    : $EVIDENCE"
echo "  Statistics  : $STATS"
