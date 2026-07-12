# Adding Konture as a dependency

Konture (https://github.com/baole/konture) is distributed as a Gradle plugin +
library, coordinates `io.github.baole.konture` (plugin) and `io.github.baole`
(library group). Always confirm the latest version against Maven Central /
the Gradle Plugin Portal before pinning it — do not assume a version seen in
the README or in a previous project is still current.

## Option A — version catalog (`gradle/libs.versions.toml`)

Use this if the project already has a version catalog (most modern multi-module
Gradle projects do). Add:

```toml
[versions]
konture = "<latest-version>"

[plugins]
konture = { id = "io.github.baole.konture", version.ref = "konture" }

[libraries]
konture = { group = "io.github.baole", name = "konture", version.ref = "konture" }
```

Apply the plugin in the root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.konture) apply true
}
```

Reference the library from the dedicated test module:

```kotlin
dependencies {
    testImplementation(libs.konture)
}
```

## Option B — traditional DSL (no version catalog)

Use this only if the project genuinely has no version catalog. **Don't
introduce a version catalog just for this** — that's a build-system decision
that affects the whole project and is out of scope for a Konture setup task.
Ask the user first if you think a catalog would help; otherwise use the
literal form:

Root `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.baole.konture") version "<latest-version>" apply true
}
```

Dedicated test module `build.gradle.kts`:

```kotlin
dependencies {
    testImplementation("io.github.baole:konture:<latest-version>")
}
```

## Checking the latest version

Search Maven Central (`io.github.baole` group) or the Gradle Plugin Portal
(`io.github.baole.konture`) rather than trusting a hardcoded version in any
doc, including this one. Report the version found to the user before locking
it in, since Konture is a young, actively developed library and versions may
carry breaking changes between releases.
