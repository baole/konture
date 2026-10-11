---
description: 10-step GitHub issue-to-PR workflow with Git worktrees, quality gates (spotlessApply, checkKotlinAbi), deep 6-dimension code review, CI check resolution, and human PR delivery (no auto-merge).
---

# GitHub Issue-to-PR Workflow

Follow this procedure when assigned a GitHub issue or asked to work on a ticket.

## Arguments
- `issue`: The GitHub issue number or URL (e.g. `105` or `#105`)

---

## Workflow Steps

### 1. Ticket Triage & Feasibility Review
- View the ticket: `gh issue view $issue --json number,title,body,labels,author,milestone`
- Evaluate:
  - **Usefulness & Alignment**: Does it align with Konture's architecture testing mission? Is it out-of-scope or an anti-pattern?
  - **Feasibility**: Are reproduction steps, sample code, or API specs clear?
- **If NOT ready to implement**:
  - Add label: `gh issue edit $issue --add-label "needs-info"`
  - Post comment:
    ```bash
    gh issue comment $issue --body "### Ticket Triage & Review
    **Status**: \`Needs Further Clarification\`
    
    #### 1. Alignment & Scope:
    <Explanation>
    
    #### 2. Feasibility & Missing Info:
    <Questions or missing reproduction code>
    
    Please provide clarification before implementation begins."
    ```
  - Halt workflow and ask the user for guidance.

### 2. Implementation Planning & Human Approval Gate
- Draft `implementation_plan.md` artifact with:
  - Root cause & solution architecture
  - Affected files and modules
  - Binary compatibility impact (`checkKotlinAbi`)
  - Testing & verification plan
- **Halt and obtain user approval before writing code.**

### 3. State Transition
- Update GitHub issue:
  ```bash
  gh issue edit $issue --remove-label "needs-info" --add-label "in-progress"
  gh issue comment $issue --body "Started work on this issue.\n\nBranch: \`fix/issue-$issue\`"
  ```

### 4. Create Isolated Git Worktree
- Create an isolated worktree outside the primary directory:
  ```bash
  BRANCH_NAME="fix/issue-$issue"
  WORKTREE_PATH="../worktrees/konture-issue-$issue"

  git fetch origin main
  git worktree add -b "$BRANCH_NAME" "$WORKTREE_PATH" origin/main
  ```

### 5. Implement, Self-Audit & Local Quality Gate
- Implement the fix inside `$WORKTREE_PATH`.
- Run Pre-PR Self-Audit on the git diff:
  - [ ] No double disk reads (e.g. `hashFile` + `readText` duplicates).
  - [ ] No unbounded caches in Gradle daemons / IDE plugins.
  - [ ] No intermediate string allocations in loops/hashing.
  - [ ] ThreadLocal state doesn't reset global singletons.
- Run Konture verification suite inside `$WORKTREE_PATH`:
  ```bash
  ./gradlew -q spotlessApply
  ./gradlew -q :core:test :library:test :plugin-gradle:test
  ./gradlew -q checkKotlinAbi
  python3 script/add_front_matter.py --validate
  ```

### 6. Create GitHub Pull Request
- Commit and push from `$WORKTREE_PATH`:
  ```bash
  git add .
  git commit -m "fix(issue-$issue): <concise summary>"
  git push -u origin "$BRANCH_NAME"
  ```
- Open PR:
  ```bash
  gh pr create \
    --title "fix(issue-#$issue): <Title>" \
    --body "Closes #$issue

  ## Summary of Changes
  - <Key changes>

  ## Verification
  - [x] \`./gradlew -q spotlessApply\`
  - [x] \`./gradlew -q test\`
  - [x] \`./gradlew -q checkKotlinAbi\`" \
    --base main
  ```
- Capture PR number: `PR_NUMBER=$(gh pr view --json number -q .number)`

### 7. 6-Dimension Deep Technical Code Review
- Spawn an isolated Reviewer Subagent (`invoke_subagent`) with only the PR diff and requirements:
  - **1. 💾 I/O & Resources**: No duplicate file reads on cache misses; streams in `.use {}`.
  - **2. 🧠 Memory & Daemons**: Bounded caches/LRU for persistent Gradle daemons.
  - **3. ⚡ GC & Allocations**: No UTF-16 to UTF-8 re-encoding; streaming hash I/O.
  - **4. 🔒 Concurrency**: Thread-safe parallel test execution.
  - **5. 🛡️ ABI & Architecture**: Binary compatibility (`checkKotlinAbi`) and visibility.
  - **6. 🧪 Edge Cases**: Zero-byte files, nullability, missing files.

### 8. Post Review & Convergence Loop (Max 3 Rounds)
- If `CHANGES_REQUESTED` (any 🔴 BLOCKERS):
  - Post findings: `gh pr review "$PR_NUMBER" --comment -b "<FINDINGS>"`
  - Implement fixes in `$WORKTREE_PATH`, re-verify (Step 5), commit & push.
  - Re-run review (Step 7) up to 3 rounds.
- If `APPROVED` (0 BLOCKERS):
  - Post approval: `gh pr review "$PR_NUMBER" --comment -b "### Code Review\n\n**Verdict**: \`APPROVED\`\n\nAll quality gates and technical dimensions verified."`

### 9. Remote CI Watch & Auto-Fix Loop
- Watch GitHub Actions CI:
  ```bash
  gh pr checks "$PR_NUMBER" --watch
  ```
- If any CI job fails:
  - Inspect logs: `gh run view <RUN_ID> --log-failed`
  - Fix inside `$WORKTREE_PATH`, verify locally, and push `fix(ci): ...`
  - Re-watch until **100% of CI checks are GREEN**.

### 10. Deliver Verified PR to Human (NO AUTO-MERGE)
- Post PR ready comment:
  ```bash
  gh pr comment "$PR_NUMBER" --body "### PR Ready for Review\n- Local tests & ABI checks passed\n- Deep code review approved (0 blockers)\n- All remote GitHub Actions CI checks passed"
  ```
- Deliver summary to human in chat:
  - 🔗 **PR Link**
  - 🟢 **CI Status**: All GitHub Actions passed
  - 🧪 **Verification Summary**: Spotless, test results, ABI checks
  - 📂 **Worktree Path** (cleanup after manual merge: `git worktree remove $WORKTREE_PATH`)
