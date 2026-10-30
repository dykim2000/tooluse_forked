# Session Log - neat-tooluse Codex Refactor

Date: April 21, 2026 (America/Chicago)
Repo: `/home/yschoe/git/tooluse-forked/neat-tooluse`

## Objective
Refactor the fork so experiments are easier to manage from CLI:
1. Select chromosome ID from command line when evaluating.
2. Select properties file from command line for training/evaluation.
3. Save run artifacts under `db/<properties-file-prefix>/`.
4. Add a two-stick properties preset.

## Chronological Work Log

### 1. Initial evaluate CLI refactor
- Read `README.md` and located target class: `src/example/EvaluateIt.java`.
- Found hardcoded chromosome load:
  - `db.loadChromosome("11678", config)`
- Implemented argument-based chromosome selection:
  - default remains `11678`
  - if arg provided, use `args[0]`
- Improved error message to include requested chromosome ID.

Files updated:
- `src/example/EvaluateIt.java`
- `run.sh` (forward argument to `EvaluateIt`)
- `README.md` (updated evaluate instructions)

### 2. Java/runtime mismatch diagnosis
User reported:
- `UnsupportedClassVersionError`
- class file version `65.0` used with Java 8 runtime (`52.0` max)

Diagnostics performed:
- Confirmed active runtime: Java 8 (`openjdk 1.8.0_462`)
- Confirmed compiled classes in `bin/example` were Java 21 bytecode (`major version: 65`)

Guidance provided:
- Switch to Java 21 via SDKMAN for running this build.

### 3. Why `./run.sh 5016` still failed
User reported:
- `no chromosome found:` (empty or old behavior)

Root cause:
- stale/old compiled classes in `bin` were being executed, not newly edited source.

Action taken:
- adjusted script behavior to prefer `bin` first in classpath and pass `"$@"` through.

File updated:
- `run.sh`

### 4. Two-stick support and properties-driven runs
User requested:
- create `example_twosticks.properties`
- make properties file selectable via CLI
- save results under `db/<propertyfile-prefix>/`

Refactor implemented:

#### 4.1 New central runtime config helper
Created `src/example/RunConfig.java` to:
- pick properties filename from CLI with default `example.properties`
- derive output prefix from properties filename (e.g., `example_twosticks`)
- ensure results directory exists: `./db/<prefix>`
- override runtime ANJI persistence keys:
  - `persistence.base.dir`
  - `id.file`
  - `neat.id.file`
  - `presentation.dir`

#### 4.2 Driver refactor
Updated `src/example/Driver.java` to:
- accept optional properties arg (`args[0]`)
- apply output overrides via `RunConfig`
- initialize `DashboardLogger` with run-specific root and loaded `Properties`
- print selected properties and output directory at startup

#### 4.3 EvaluateIt refactor
Updated `src/example/EvaluateIt.java` to accept:
- `args[0]`: chromosome ID (default `11678`)
- `args[1]`: properties file (default `example.properties`)

Also:
- applies output overrides through `RunConfig`
- writes activation log to `db/<prefix>/neurons_actLog.txt`
- logs selected properties/chromosome/output path at startup

#### 4.4 Dashboard logger output isolation
Updated `src/example/DashboardLogger.java` to:
- write CSV outputs to `db/<prefix>/dashboard/csv/`
- stop hardcoding `example.properties` internally
- read `num.generations` and `popul.size` from injected runtime `Properties`
- normalize enum switch syntax for compatibility

#### 4.5 Fitness function property support
Updated `src/example/ExampleFitnessFunction.java` to:
- read `stick.state` from properties in `init(...)`
- support values: `0/1/2` and `NO_STICK/ONE_STICK/TWO_STICKS`
- make `stickState` and max reach derived at runtime
- allow activation log file path injection

#### 4.6 Shell scripts
Updated scripts to pass through CLI args and use bin-first classpath:
- `run.sh` usage: `./run.sh [chromosome_id] [properties_file]`
- `evolve.sh` usage: `./evolve.sh [properties_file]`

#### 4.7 Properties
- Added `stick.state=1` to `properties/example.properties`
- Added new file `properties/example_twosticks.properties` with:
  - `run.name=testrun_twosticks`
  - `stick.state=2`

#### 4.8 Documentation
Updated `README.md` usage sections for:
- new CLI signatures
- `db/<properties-file-prefix>/` output convention
- two-stick preset mention

### 5. Follow-up warning diagnosis from training output
User reported warnings during:
- `./evolve.sh example_twosticks.properties`
- warnings like: `chromosome in run not found: <id>`

Diagnosis:
- runtime was still using stale old classes from `bin` (Driver bytecode still hardcoded to `example.properties`)
- old default-path run metadata (e.g. `db/run/runtestrun.xml`) can reference IDs not present in current chromosome set

Resolution guidance provided:
- recompile modified Java classes with Java 21 so new runtime path logic is active
- optional cleanup of stale run file if continuing to use default db path

## Final State of Requested Refactor
Implemented in source and scripts:
- CLI chromosome selection for evaluation
- CLI properties selection for training/evaluation
- properties-driven stick mode (`stick.state`)
- output isolation under `db/<properties-file-prefix>/`
- `example_twosticks.properties` preset

## Important Operational Note
For behavior to match source changes, corresponding classes in `bin/` must be rebuilt with a compatible JDK (Java 21 in this repo’s current compiled setup).
