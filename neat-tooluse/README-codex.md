# README-codex

This guide documents the Codex-based refactor workflow for `neat-tooluse`, including Java setup, compilation, properties-driven runs, and experiment output management.

## What This Refactor Adds
- CLI property-file selection for training and evaluation.
- CLI chromosome ID selection for evaluation.
- Per-experiment output isolation under:
  - `db/<properties-file-prefix>/`
- Properties-driven stick mode via `stick.state`.

Examples:
- `example.properties` -> outputs under `db/example/`
- `example_twosticks.properties` -> outputs under `db/example_twosticks/`

## 1. Prerequisites
- Linux/macOS shell (examples use `bash`).
- SDKMAN installed.
- JDK 21 available via SDKMAN (recommended: Temurin 21).

Repository root used below:
- `~/git/tooluse-forked/neat-tooluse`

## 2. Install SDKMAN (if needed)

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version
```

If `sdk` is not found in a new shell:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

## 3. Install and Use Java 21 with SDKMAN

List available Java distributions:

```bash
sdk list java
```

Install Java 21 (example candidate):

```bash
sdk install java 21.0.8-tem
```

Use Java 21 for current shell:

```bash
sdk use java 21.0.8-tem
java -version
javac -version
```

Set default globally (optional):

```bash
sdk default java 21.0.8-tem
```

## 4. Enter Repo and Set Environment

```bash
cd ~/git/tooluse-forked/neat-tooluse
```

Classpath used by scripts and manual compile:

```bash
export CLASSPATH=./bin:./src:./properties:./src/anji_2_01/lib/anji.jar:./src/log4j.jar:./src/anji_2_01_actlog.jar:./src/example
```

Notes:
- `bin` is intentionally first so freshly compiled classes are used.
- If behavior seems stale, recompile before running.

## 5. Compile

### Full compile (legacy script)

```bash
./build.sh
```

### Targeted compile for Codex-refactor classes
Use this when updating CLI/properties/output-routing logic.

```bash
javac -classpath "$CLASSPATH" -d bin \
  src/example/RunConfig.java \
  src/example/DashboardLogger.java \
  src/example/ExampleFitnessFunction.java \
  src/example/Driver.java \
  src/example/EvaluateIt.java
```

## 6. Properties Files

### Existing default
- `properties/example.properties`
- Includes `stick.state=1` (one-stick config).

### Two-stick preset
- `properties/example_twosticks.properties`
- Includes `stick.state=2`.

### `stick.state` accepted values
- Numeric: `0`, `1`, `2`
- Text: `NO_STICK`, `ONE_STICK`, `TWO_STICKS`

### Create your own experiment properties file

```bash
cp properties/example.properties properties/example_myexp.properties
# edit as needed (e.g., stick.state, num.generations, popul.size)
```

Result directory will be derived from filename prefix:
- `example_myexp.properties` -> `db/example_myexp/`

## 7. Run Training

Training entrypoint:

```bash
./evolve.sh [properties_file]
```

Examples:

```bash
./evolve.sh
./evolve.sh example.properties
./evolve.sh example_twosticks.properties
```

Startup should print selected config/output path. Outputs are written under:

```text
db/<properties-prefix>/
```

Typical files/folders:
- `db/<prefix>/chromosome/*.xml`
- `db/<prefix>/id.xml`
- `db/<prefix>/neatid.xml`
- `db/<prefix>/dashboard/csv/*.csv`
- `db/<prefix>/nevt/`

## 8. Evaluate a Chromosome

Evaluation entrypoint:

```bash
./run.sh [chromosome_id] [properties_file]
```

Argument order:
1. `chromosome_id` (optional, default: `11678`)
2. `properties_file` (optional, default: `example.properties`)

Examples:

```bash
./run.sh
./run.sh 5016
./run.sh 5016 example_twosticks.properties
```

Evaluation logs/outputs go under matching run prefix:
- `db/<prefix>/neurons_actLog.txt`

## 9. Find Best Chromosome from a Training Run

Read generation summary:

```bash
tail -n 5 db/<prefix>/dashboard/csv/generation_fitness.csv
```

Use final row `max_chromosome_id` with `run.sh`:

```bash
./run.sh <max_chromosome_id> <properties_file>
```

## 10. Recommended Session Workflow

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk use java 21.0.8-tem
cd ~/git/tooluse-forked/neat-tooluse

export CLASSPATH=./bin:./src:./properties:./src/anji_2_01/lib/anji.jar:./src/log4j.jar:./src/anji_2_01_actlog.jar:./src/example

javac -classpath "$CLASSPATH" -d bin \
  src/example/RunConfig.java \
  src/example/DashboardLogger.java \
  src/example/ExampleFitnessFunction.java \
  src/example/Driver.java \
  src/example/EvaluateIt.java

./evolve.sh example_twosticks.properties
# after training, choose chromosome id from dashboard CSV
./run.sh <chromosome_id> example_twosticks.properties
```

## 11. Troubleshooting

### A) `UnsupportedClassVersionError` / class file version mismatch
Cause:
- Running with older Java (e.g., Java 8) against classes compiled for newer Java.

Fix:

```bash
sdk use java 21.0.8-tem
java -version
javac -version
# then recompile
```

### B) CLI args appear ignored
Cause:
- stale classes in `bin`.

Fix:
- Ensure `bin` is first in classpath.
- Recompile target classes.
- Re-run script.

### C) `chromosome in run not found` warnings
Cause:
- stale run metadata referencing old chromosome IDs.

Fix options:
- Continue (warning-only in many cases).
- Clean old run metadata for that output root and re-run.

Example cleanup for one output prefix:

```bash
rm -f db/<prefix>/run/*.xml
```

### D) Expected output folder not created
Cause:
- old compiled `Driver`/`EvaluateIt` still in use.

Fix:
- Recompile with Java 21.
- Confirm startup logs show selected properties and output directory.

## 12. Quick Command Reference

```bash
# Train default
./evolve.sh

# Train two sticks
./evolve.sh example_twosticks.properties

# Evaluate default chromosome/default props
./run.sh

# Evaluate specific chromosome/default props
./run.sh 5016

# Evaluate specific chromosome/two-stick props
./run.sh 5016 example_twosticks.properties
```
