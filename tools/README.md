# Local tools

## Apache JMeter

JMeter is **not** committed to the repository (see root `.gitignore`). Install it locally before running load tests:

1. Download [Apache JMeter 5.6.3](https://jmeter.apache.org/download_jmeter.cgi) (binary archive).
2. Extract into this folder so the binary path is:

   ```
   tools/apache-jmeter-5.6.3/bin/jmeter
   ```

3. Run the GUI:

   ```bash
   ./start-jmeter.sh
   ```

4. Or run the CLI + HTML report:

   ```bash
   ./scripts/run-load-test.sh
   ```

See `docs/jmeter/LOAD_TEST.md` for prerequisites (stack running, JWT token in `load_test.jmx`).

## generate_jwt.py

Optional helper to mint JWT tokens for manual testing. Requires Python 3 and project dependencies as documented in the script.
