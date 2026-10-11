---
name: ship-feature
description: Autonomous end-to-end feature delivery lifecycle for Konture with zero human intervention. Implements feature/rule, writes unit & dogfooding tests, runs script/check.sh, creates PR, conducts honest iterative code review (up to 5x), posts PR review summary, merges PR, and cleans worktree.
metadata:
  author: Konture Team
  version: '1.0.0'
---

# Ship Feature Playbook (Fully Autonomous)

This skill executes the complete end-to-end delivery lifecycle for a feature, DSL rule addition, engine optimization, or bugfix in **Konture** with **zero human intervention**.

```
[Worktree / Branch Setup (Fetch latest main)]
           │
           ▼
[Phase 1: Feature / Rule / Plugin Implementation & i18n Sync]
           │
           ▼
[Phase 2: Unit Tests, Integration Tests & Dogfooding (konture-test)]
           │
           ▼
[Phase 2.5: Sync with Main (Rebase / Merge origin/main)]
           │
           ▼
[Phase 3: script/check.sh (Auto-Fix Loop until Green)]
           │
           ▼
[Phase 4: Non-Interactive Git Commit & PR Creation]
           │
           ▼
[Phase 5: Honest 6-Dimension Code Review Loop (Up to 5 Iterations)]
           │
           ▼
[Phase 6: Final Main Sync, PR Summary, Auto-Merge & Cleanup]
```

---

## Zero-Intervention Operating Rules

To ensure 100% autonomous execution without stalling or prompting the user:
1. **Never Ask for Intermediary Confirmation**: Proceed immediately through all phases. Do not ask *"Should I create the PR now?"* or *"Tests passed, should I merge?"*.
2. **Never Run Interactive CLI Commands**:
   - `gh pr create` **MUST** always include `--title "<title>"` and `--body "<body>"`. Never rely on interactive editors (`vim`/`nano`) or stdin.
   - `git commit` **MUST** always supply `-m "<message>"`.
   - `gh pr merge` **MUST** include non-interactive flags (`--squash --delete-branch` or `--auto --squash --delete-branch`).
3. **Continuous Main Synchronization**:
   - Always fetch and rebase/merge `origin/main` before running verification and before final PR merge to prevent outdated-base drift or merge conflicts.
4. **Automatic Error Resolution**:
   - When `./script/check.sh`, `spotlessApply`, `checkKotlinAbi`, `detekt`, or tests fail, **do not report the failure to the user**.
   - Read the stack trace, locate the failing file, apply the fix, and re-run. Loop until green.
5. **No `cd` Command**: Antigravity prohibits `cd`. When operating inside a git worktree, pass the worktree's absolute directory to the tool's `Cwd` parameter.
6. **Always Use `-q`/`--quiet` with Gradle**: Avoid noisy output by appending `-q` or `--quiet` to `./gradlew` commands.
7. **Serialize Gradle Tasks**: To avoid Gradle daemon contention and file lock conflicts, never run Gradle commands concurrently.

---

## Phase-by-Phase Execution

### Step 0: Branch & Worktree Setup

1. Fetch latest changes from remote `main`:
   ```bash
   git fetch origin main
   ```
2. Check current git status and branch:
   ```bash
   git status --porcelain
   git branch --show-current
   ```
3. Derive a clean branch name: `feat/<feature-kebab-name>` or `fix/<fix-kebab-name>`.
4. If not already working inside a dedicated worktree, create one based on latest `origin/main`:
   ```bash
   WORKTREE_DIR="$HOME/.gemini/antigravity/worktrees/konture/<branch-name>"
   git worktree add -b <branch-name> "$WORKTREE_DIR" origin/main
   ```
   *All subsequent file edits and terminal commands must target `Cwd: "$WORKTREE_DIR"`.*

---

### Phase 1: Feature Implementation & Invariants

1. **Scaffold Architecture**:
   - For DSL additions: Implement in `library/src/main/kotlin/...` using `@KontureDsl` and fluent grammar.
   - For engine / parser additions: Implement in `library/src/main/kotlin/io/github/baole/konture/impl/...`.
   - For build plugins: Implement in `plugin-gradle` or `plugin-maven`.
2. **Binary Compatibility**:
   - Check if public signatures changed. If new public declarations are added intentionally, prepare to run `./gradlew -q updateKotlinAbi`.
3. **8-Language Localization (i18n)**:
   - Zero hardcoded English strings in violation messages or assertion failures.
   - Add new message keys to `messages.properties` and mirror across all 7 locale bundles (`messages_fr.properties`, `messages_es.properties`, `messages_it.properties`, `messages_vi.properties`, `messages_zh.properties`, `messages_zh_CN.properties`, `messages_zh_TW.properties`).
   - Double all literal apostrophes: `''` (e.g. `doesn''t`).
   - Preserve locale-neutral baseline normalization.
4. **Documentation Sync**:
   - If adding a new `.md` documentation page in `docs/`: **Never add YAML front matter**. Add metadata entry to `FRONT_MATTER_MAP` in `script/add_front_matter.py` and run `python3 script/add_front_matter.py --validate`.

---

### Phase 2: Unit, Integration & Dogfooding Tests

1. **Unit Tests**:
   - Add comprehensive tests in `:core:test` or `:library:test`.
   - Include positive tests (conforming code passes) and negative tests (violating code triggers exact `Violation` message and location).
2. **Dogfooding Architecture Tests**:
   - Add or verify dogfooding rules in `konture-test/src/test/kotlin/...`.
3. **Run Targeted Tests**:
   ```bash
   ./gradlew -q :library:test --tests "*TargetTest"
   ```

---

### Phase 2.5: Synchronize with Main (Pre-Verification Rebase / Merge)

Before running the full verification pipeline, ensure the branch incorporates all latest updates from `origin/main`:

1. Fetch latest remote commits:
   ```bash
   git fetch origin main
   ```
2. Rebase (or merge) `origin/main` into the current branch:
   ```bash
   git rebase origin/main || (git rebase --abort && git merge origin/main --no-edit -m "chore(git): merge origin/main into feature branch")
   ```
3. If conflicts arise, resolve them cleanly and ensure working tree is clean.

---

### Phase 3: Verification Pipeline & Auto-Fix Loop

1. **Fast Pre-Flight Check (< 5s)**:
   Run the rapid static quality check to catch formatting, i18n, ABI, and architecture invariant issues immediately:
   ```bash
   ./script/preflight.sh
   ```
   If any pre-flight check fails, fix the flagged issues and re-run until green.

2. **Full Local Validation Pipeline**:
   Execute the comprehensive project verification script:
   ```bash
   ./script/check.sh
   ```

**Auto-Fix Loop**:
- If `./script/check.sh` fails:
  - **Spotless / Ktlint failures**: Run `./gradlew -q spotlessApply` or `./script/format.sh`.
  - **ABI check failures**: If changes are intentional, run `./gradlew -q updateKotlinAbi`.
  - **JaCoCo coverage failures**: Bundle instruction coverage must meet $\ge 80\%$. Add missing unit tests for untested branches.
  - **Detekt issues**: Refactor flagged lines (complexity, naming, imports) and re-run.
  - **Compilation / Test failures**: Read stack traces, fix code or test fixtures, and re-run.
  - **Maven plugin failures**: Ensure `mvn clean compile -f plugin-maven/pom.xml` passes.
- Repeat until `./script/check.sh` succeeds with `All Local Validation Checks Passed Successfully!` and exit code 0.

---

### Phase 4: Non-Interactive Git Commit & PR Creation

1. Stage and commit all changes:
   ```bash
   git add .
   git commit -m "feat(<scope>): <short-description>"
   ```
2. Push branch to remote:
   ```bash
   git push -u origin <branch-name>
   ```
3. Create Pull Request non-interactively using `gh`:
   ```bash
   gh pr create \
     --title "feat(<scope>): <concise-title>" \
     --body "$(cat <<'EOF'
   ## Summary
   - <Concise description of changes>
   - Updated 8-language localization message bundles.
   - Added unit tests and dogfooding architecture tests.
   
   ## Verification
   - Ran \`./script/check.sh\` (Spotless, Detekt, unit tests, JaCoCo ≥80% coverage, Maven Local, konture-test, plugin-maven).
   - Validated binary compatibility (\`./gradlew -q checkKotlinAbi\`).
   - Validated Jekyll documentation map (\`python3 script/add_front_matter.py --validate\`).
   EOF
   )" \
     --base main \
     --head <branch-name>
   ```
4. Extract the PR number and URL from command output.

---

### Phase 5: Honest 6-Dimension Code Review Loop (Up to 5 Iterations)

Perform an adversarial self-review of the entire changeset (`git diff main...HEAD`).

#### Review Checklist (6 Dimensions):
1. **I/O & Resource Management**: All file handles, streams, and readers closed with `.use {}`.
2. **Memory & Daemon Safety**: No AST trees or classloaders held across test runs; no memory leaks in persistent daemons.
3. **Concurrency & Thread Safety**: Shared caches (`ConcurrentHashMap`, `ThreadLocal`) properly synchronized.
4. **Binary Compatibility (ABI)**: No unintended breaking changes to public symbols (`checkKotlinAbi`).
5. **Allocations & Performance**: Minimal object allocation in tight AST traversal loops.
6. **Edge Cases & Test Quality**: Negative tests present, corner cases handled (empty projects, missing files, wildcards), JaCoCo coverage $\ge 80\%$, file size limits respected (source $\le 300$ lines, tests $\le 450$ lines).

#### Loop Execution:
```
Iteration = 1
While Iteration <= 5:
  1. Audit diff for Blockers (P0) or Important Improvements (P1).
  2. If 0 issues remain -> Break loop.
  3. If issues found:
     a. Fix code / decompose oversized files.
     b. Run ./script/check.sh and ensure exit code 0.
     c. git add . && git commit -m "refactor(review): address review findings (iteration $Iteration)"
     d. git push origin <branch-name>
     e. Iteration++
```

---

### Phase 6: Final Main Sync, PR Summary, Auto-Merge & Cleanup

1. **Final Sync with Latest Main**:
   Before merging, ensure the PR branch is fully updated with any new commits on `main`:
   ```bash
   git fetch origin main
   git rebase origin/main || (git rebase --abort && git merge origin/main --no-edit -m "chore(git): sync with main before PR merge")
   git push origin <branch-name> --force-with-lease || git push origin <branch-name>
   ```
   If new changes were pulled in from `main`, run `./script/check.sh` once more.

2. **Post Honest Review Summary on PR**:
   ```bash
   gh pr comment <pr-number> --body "$(cat <<'EOF'
   ### Autonomous Quality Review Summary
   - **Review Iterations**: <N> of 5 completed.
   - **Architectural Guardrails**: Layer isolation verified, zero compiler classpath leaks.
   - **ABI Compatibility**: Validated via \`checkKotlinAbi\`.
   - **Code Coverage**: JaCoCo bundle instruction coverage threshold (≥80%) satisfied.
   - **File Size Audit**: All modified source files ≤ 300 lines; test files ≤ 450 lines.
   - **Localization Audit**: 8 locales synchronized, zero hardcoded English strings.
   - **Branch Synchronization**: Successfully rebased/merged with latest `origin/main`.
   - **Local Pipeline**: \`./script/check.sh\` passing cleanly.
   EOF
   )"
   ```

3. **Auto-Merge Pull Request**:
   Merge non-interactively:
   ```bash
   gh pr merge <pr-number> --squash --delete-branch
   ```

4. **Clean Worktree**:
   ```bash
   git worktree remove "$WORKTREE_DIR"
   ```
