# Configuration

Konture's behavior can be customized to match your project's specific conventions. This guide details plugin configuration parameters and pattern-matching rules. For managing technical debt in legacy codebases, see our dedicated [Architecture Baselines](baseline.md) guide.

---

## 🛠️ Plugin Customization

You can configure the Konture plugin inside your build files to exclude legacy directories, ignore external packages, and set custom trace outputs.

### 🐘 Gradle Configuration

Add the `konture { ... }` block inside your root `build.gradle.kts` or inside your dedicated architecture test project:

```kotlin
konture {
    // Exclude subprojects from analysis
    excludeModules(":legacy-app", ":experimental:*")

    // Exclude specific packages from being parsed
    excludePackages("com.acme.generated..", "..databinding..")

    // Exclude specific classes or patterns
    excludeClasses("ExcludedService", "*Helper")

    // Exclude certain Gradle dependency configurations from being traversed
    excludeConfigurations("test*", "profile")

    // Set the target file name for architecture baselines (default is "konture-baseline.json")
    baselinePath("custom-baseline.json")

    // Set the task execution log level
    logLevel("INFO")
}
```

---

### 📦 Maven Configuration

Declare your plugin configurations inside the `<configuration>` block of the `konture-maven-plugin` inside your dedicated `konture-test/pom.xml`:

```xml
<plugin>
    <groupId>io.github.baole.konture</groupId>
    <artifactId>konture-maven-plugin</artifactId>
    <version>0.6.10</version>
    <executions>
        <execution>
            <phase>process-test-resources</phase>
            <goals>
                <goal>generate-layout</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <!-- Exclude subprojects from analysis -->
        <excludeModules>
            <module>legacy-app</module>
        </excludeModules>

        <!-- Exclude specific packages from being parsed -->
        <excludePackages>
            <package>com.acme.generated..</package>
            <package>..databinding..</package>
        </excludePackages>

        <!-- Exclude specific classes or patterns -->
        <excludeClasses>
            <class>ExcludedService</class>
            <class>*Helper</class>
        </excludeClasses>

        <!-- Exclude certain dependency configurations from being traversed -->
        <excludeConfigurations>
            <configuration>test</configuration>
            <configuration>profile</configuration>
        </excludeConfigurations>

        <!-- Set the task execution log level -->
        <logLevel>INFO</logLevel>
    </configuration>
</plugin>
```

---

## 📋 Available Settings & Matching Rules

| Parameter | Default Value | Description & Matching Rules |
| :--- | :--- | :--- |
| **`excludeModules`** | `emptyList()` | Excludes matching Gradle subprojects from the layout graph.<br>• Supports **Module Glob Patterns** (e.g., `:feature:*` or `:feature:**`). |
| **`excludePackages`** | `emptyList()` | Excludes class files in matching packages from being parsed.<br>• Supports **Package Segment Wildcards (`..`)**. |
| **`excludeClasses`** | `emptyList()` | Excludes matching classes from the analysis. Fully qualified names and simple class names are checked.<br>• Supports **Package Wildcards (`..`)** and **Simple Globs (`*`)**. |
| **`excludeConfigurations`** | `listOf("test", "benchmark", "profile")` | Excludes specific dependency configurations from being traversed.<br>• Supports simple glob matching (`*`). E.g., `test*` matches `testImplementation`. |
| **`logLevel`** | `"INFO"` | Configures logging level of the Konture plugin execution.<br>• Supported levels: `"INFO"`, `"DEBUG"`, `"WARNING"`, `"TRACE"`. |

---

## 🧩 Wildcard & Pattern Matching in Depth

Konture utilizes high-performance, lightweight matching engines optimized specifically for Kotlin package hierarchies and Gradle module structures, making them significantly faster than regex parsers.

### 1. Package Matching (`..`)

Package paths are dot-separated (e.g., `com.acme.feature.payment.service`).
*   **Double Dot (`..`)**: Represents **zero or more package segments**.
*   **Single Dot (`.`)**: Separates explicit segments.

| Pattern | Matches | Does NOT Match | Why? |
| :--- | :--- | :--- | :--- |
| `com.acme.domain..` | `com.acme.domain`<br>`com.acme.domain.repository` | `com.acme.api`<br>`org.acme.domain` | Matches any package starting with `com.acme.domain` followed by any depth. |
| `..generated..` | `com.acme.generated`<br>`com.acme.feature.generated.service` | `com.acme.generate`<br>`com.acme.regenerated` | Matches any package path where `generated` appears as a complete segment. |
| `..` | *Matches everything* | None | Matches any package sequence. |

---

### 2. Module Glob Matching (`*` vs `**`)

Gradle subprojects are colon-separated (e.g., `:feature:checkout:impl`).
*   **Single Star (`*`)**: Matches **exactly one module segment** (all characters except the colon `:`).
*   **Double Star (`**`)**: Matches **zero or more module segments** (any depth sequence).

| Pattern | Matches | Does NOT Match | Why? |
| :--- | :--- | :--- | :--- |
| `:feature:*` | `:feature:checkout`<br>`:feature:catalog` | `:feature:checkout:impl`<br>`:feature` | `*` matches exactly one levels deep under `:feature`. |
| `:feature:**` | `:feature:checkout`<br>`:feature:checkout:impl` | `:core:network` | `**` matches any depth of subprojects under `:feature`. |
| `:*-api` | `:payment-api`<br>`:auth-api` | `:feature:payment-api` | Matches any root-level module ending with `-api`. |

---

### 3. Simple Glob Matching (`*`)

Used for configuration matching (`excludeConfigurations`) and simple class name matching (`excludeClasses`).
*   **Single Star (`*`)**: Matches zero or more characters of any kind.

| Pattern | Matches | Does NOT Match |
| :--- | :--- | :--- |
| `*Helper` | `AuthHelper`<br>`Helper`<br>`com.acme.MyHelper` | `Helpers`<br>`HelperClass` |
| `test*` | `test`<br>`testImplementation`<br>`testRuntimeOnly` | `latest` |

---

## 🛡️ Architecture Baselines

When integrating architecture tests into large, established codebases, you will often encounter thousands of pre-existing violations. Forcing developers to fix all historical violations immediately is a massive blocker.

Konture solves this with **Architecture Baselines**, allowing you to record all existing violations to localized, distributed files, ignore them in subsequent runs, and focus entirely on preventing new architectural erosion!

> **Dedicated Guide**: Because managing baselines is critical to scaling architecture tests in enterprise teams, we have split this feature into its own dedicated guide.
>
> 🏃 **[Read the Architecture Baselines Guide](baseline.md)** to learn about Distributed Baselines, recording existing technical debt, configuration references, and serialization schemas.
{: .important }
{: .important }
