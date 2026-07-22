# Architecture Baselines

When integrating architecture tests into large, established codebases, you will often encounter thousands of pre-existing violations. Forcing developers to fix all historical violations immediately is a massive blocker.

Konture solves this with **Architecture Baselines**, allowing you to record all existing violations to a baseline file, ignore them in subsequent runs, and focus entirely on preventing **new** architectural erosion!

---

## 📦 Distributed Baselines

Unlike traditional architecture test frameworks that record violations into a single, massive monolithic baseline file at the project root (creating merge conflicts on every pull request), Konture features **Distributed Baselines**.

If your project is a multi-module workspace and you specify a relative baseline file name, Konture's baseline engine (`BaselineManager`) automatically correlates recorded violations back to the submodules they originated from. It writes localized baseline files **directly inside each submodule directory**!

```text
my-project/
├── core/
│   └── database/
│       ├── src/
│       └── konture-baseline.json   <-- Violations local to database module
├── feature/
│   └── payment/
│       ├── src/
│       └── konture-baseline.json   <-- Violations local to payment module
└── konture-test/
```

This isolates changes, completely prevents git merge conflicts on PRs, and makes ownership of technical debt crystal clear!

---

## 🏃 Generating & Committing Baselines

To establish your baseline and freeze existing technical debt:

### Step 1: Run your tests in Generate Mode

You can run the built-in Gradle task registered by the Konture plugin, which automatically sets `konture.baseline.generate=true` and runs all test tasks:

#### 🐘 Gradle (Recommended)
```bash
./gradlew generateKontureBaseline
```

Or target a specific submodule to generate or update its baseline in isolation:
```bash
./gradlew :feature:payment:generateKontureBaseline
```

#### 🐘 Gradle (Alternative Manual CLI Override)
You can also manually execute your test task while passing the generate flag as a JVM system property:
```bash
./gradlew :konture-test:test -Dkonture.baseline.generate=true
```

#### 📦 Maven
For Maven projects, pass the system property directly:
```bash
mvn test -pl konture-test -Dkonture.baseline.generate=true
```

Upon completion, Konture will succeed and automatically generate a localized `konture-baseline.json` file in each module containing structural violations.

### Step 2: Commit baselines to Git

Commit the generated `konture-baseline.json` files to your version control repository alongside your tests:

```bash
git add **/konture-baseline.json
git commit -m "chore: freeze architecture violations baseline"
```

In subsequent standard test executions (without the generate flag), Konture will load these files, filter out the pre-existing violations, and pass. Any **new** violation introduced by developers will fail the build immediately!

---

## ⚙️ Baseline Configuration Reference

You can customize baseline behavior either programmatically on the `Konture` runtime or via JVM system properties.

> [!IMPORTANT]
> **Precedence Rules**: JVM system properties always take precedence over programmatic properties configured via the `Konture` singleton runtime or Gradle DSL extension.

| Programmatic Property | System Property | Default Value | Description |
| :--- | :--- | :--- | :--- |
| **`Konture.baselinePath`** | `konture.baseline.path` | `"konture-baseline.json"` | Path of the baseline files. Can also be set in Gradle via the `konture { baselinePath("custom.json") }` block. If relative, and a Gradle/Maven graph is present, it writes distributed baseline files in submodules. |
| **`Konture.generateBaseline`** | `konture.baseline.generate` | `false` | When `true`, enables generate mode to write or update baseline files. |
| *N/A* | `konture.baseline.dir` | *System property* | Overrides the target output directory for baseline files (see below for absolute-path check behavior). |

### 🔍 Absolute-Path Check & Directory Override Behavior

When Konture runs inside a multi-project Gradle workspace, the output directory resolves as follows:
- **Distributed Baseline Mode**: If `konture.baseline.dir` is NOT specified, or matches a project submodule directory, Konture executes in distributed mode, organizing baseline files inside the corresponding submodules.
- **Single Baseline Mode**: If `konture.baseline.dir` is overridden to a directory that does NOT correspond to any project module directory (e.g. an arbitrary absolute path or any other non-module directory), Konture automatically disables distributed baseline mode and falls back to saving a single unified baseline file using that specified directory.
- **Absolute File Path**: If `Konture.baselinePath` or `konture.baseline.path` is set to an absolute file path, distributed baseline mode is automatically disabled, and the baseline is read/written to that exact absolute path.

---

## 📋 JSON Baseline Schema

Baselines are serialized using `kotlinx-serialization` for speed and consistency. The schema maps violations down to specific test classes and methods, preventing a violation under one test from accidentally ignoring a different rule.

### Schema Format

Konture supports a multi-class baseline schema, grouping all recorded violations under the `testClasses` block by their respective class and test/method:

```json
{
  "version": 1,
  "testClasses": [
    {
      "name": "com.acme.konture.MyArchitectureTest",
      "tests": [
        {
          "name": "repositories should be interfaces",
          "violations": [
            {
              "location": ":database, main source set, com/acme/database/SomeRepository.kt:12",
              "message": "Class com.acme.database.SomeRepository must be declared as interface"
            }
          ]
        }
      ]
    },
    {
      "name": "com.acme.konture.OtherArchitectureTest",
      "tests": [
        {
          "name": "controllers should only depend on services",
          "violations": [
            {
              "location": ":web, main source set, com/acme/web/SomeController.kt:34",
              "message": "Class com.acme.web.SomeController depends on repository directly"
            }
          ]
        }
      ]
    }
  ]
}
```

## 🔄 Regenerating Baselines After an Upgrade

Baseline entries are matched against the exact violation text a rule produces. When an upgrade improves the wording or location of violation messages, previously recorded entries no longer match and their violations resurface as if new.

After upgrading Konture, regenerate your baselines once and commit the result:

```bash
./gradlew generateKontureBaseline
```

This is a one-time step per upgrade that changes message formatting; it does not weaken any rule, it only refreshes the stored text.

