# Deciding how Konture tests run in CI

Since the dedicated test module (commonly `:konture-test`) is a plain
`kotlin("jvm")` module with a standard `test` task, it is automatically picked
up by root-level `./gradlew test` and `./gradlew check` (which depends on
`test`) with **zero extra CI configuration**. Don't assume that's the right
place for it just because it works, though — measure first, then choose.

## Step 1: measure before deciding

Run `:konture-test:test` standalone and compare its time against the existing
unit-test suite. Architecture tests that walk the whole build graph via the
Gradle Tooling API can be noticeably slower on first evaluation than typical
unit tests, especially on a large multi-module project.

Also check: does the project have a fast pre-merge CI gate meant to give quick
feedback? If so, folding in a slower architecture-test pass could hurt that
signal.

## Step 2: pick one approach and state why

### A. Leave it in the default lifecycle (simplest)

No extra CI config needed — `test`/`check` already picks it up. Good default
when the run time is comparable to the rest of the unit tests and the project
doesn't care about separating failure types.

### B. Separate CI stage

Add a distinct CI step/job that runs `./gradlew :konture-test:test` on its own.
Good when:
- an architecture-guardrail failure should be reported distinctly from a
  generic unit-test failure so reviewers see "architecture guardrail failed"
  rather than a failure buried in a long test list, or
- the run time is long enough to want independent parallelization/gating.

### C. Tagged split within the same module

Use JUnit 5 `@Tag("architecture")` on the guardrail tests plus:

```kotlin
tasks.test {
    useJUnitPlatform {
        // includeTags("architecture")   // dedicated architecture-only run
        // excludeTags("architecture")   // fast run without architecture checks
    }
}
```

Good when the project wants architecture tests to coexist with other tests in
the same module but still be selectively includable/excludable (e.g. a fast
local dev loop that skips them, but a full CI run that includes them).

## Step 3: confirm it's actually wired in

Whichever approach is chosen, don't assume the default lifecycle is already
invoked by every CI job just because `./gradlew test` works locally — check
the actual CI config (`.github/workflows/`, Azure Pipelines YAML, or
equivalent) and confirm the relevant task is part of the pipeline that
actually gates merges.
