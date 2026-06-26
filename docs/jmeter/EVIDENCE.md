# JMeter Load Test Evidence

Generated: 2026-06-26 18:37:49 UTC

## Configuration

| Setting | Value |
|---------|-------|
| Endpoint | `POST http://localhost:8080/movements` |
| Test plan | `load_test.jmx` |
| Threads | 100 |
| Ramp-up | 10 seconds |
| Loops per thread | 10 |
| Total samples | 1001 |
| Auth user | `1710034065` (role: user) |

## Results summary

| Metric | Value |
|--------|-------|
| Total requests | 1001 |
| Successful | 1001 |
| Failed | 0 |
| Error rate | 0.00% |
| HTTP 200 | 1001 |
| Other codes | none |

## Aggregated metrics (JMeter report)

| Metric | Value |
|--------|-------|
| Mean response time | 13979.09 ms |
| Median response time | 10724.50 ms |
| 90th percentile | 27509.80 ms |
| 95th percentile | 32471.90 ms |
| 99th percentile | 41686.39 ms |
| Throughput | 6.67 req/s |
| Min / Max | 217.00 / 47829.00 ms |

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
