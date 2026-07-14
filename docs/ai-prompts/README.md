# 🤖 AI Prompts & Skills Catalog

Welcome to the **AI Prompts & Skills** directory. Konture is built from the ground up to be **AI-agent friendly**, offering structured, high-context, and API-accurate prompt assets.

By providing these prompts to modern AI coding assistants (such as **Gemini Advanced, Claude Pro, ChatGPT**) or loading them as system instructions in AI-integrated IDEs (such as **Cursor, Windsurf, GitHub Copilot**), you can automate architectural test setup, test generation, and review processes with zero syntax errors or hallucinations.

---

## 📐 Catalog of AI Prompts & Skills

| Prompt / Skill File | Description | Target Use Case | Executable Agent Skill |
| :--- | :--- | :--- | :--- |
| **[🤖 AI Onboarding & Setup](setup-prompt.md)** | Directs an AI agent to inspect the current codebase structure (modules, catalog config) and install/configure a dedicated test module. | Automated installation, `:konture-test` module creation, and CI wiring. | **[🤖 setup-konture](../../skills/setup-konture/SKILL.md)** |
| **[✍️ Unified AI Test Writing & Extensible Guardrails](writing-tests-prompt.md)** | Directs an AI assistant to systematically build, extend, or review architecture rules using extensible guardrails and compile-safe DSL API references. | Designing and extending large-scale, syntax-accurate Konture architecture tests. | **[📐 konture-architecture-tests](../../skills/konture-architecture-tests/SKILL.md)** |

---

## 📂 How to Open and Use These Prompts

### 1. Traditional Chat Assistants (Gemini, Claude, ChatGPT Web)
1. **Open** any of the prompt markdown files above in your IDE or directly on GitHub.
2. **Copy** the content from the `## 📋 Copy-Pasteable System Prompt` section of the file.
3. **Paste** the prompt as the initial context at the start of your chat session.
4. Add your custom instructions (e.g., *"Set up Konture in this multi-module Android project"* or *"Write a test to ensure our presentation layer doesn't leak into the database layer"*).

### 2. AI-Integrated IDEs (Cursor, Windsurf, Copilot, etc.)
* **System Instructions / AI Rules**: Copy the content of the prompt and append it to your workspace's `.cursorrules`, `.windsurfrules`, or Copilot instruction settings.
* **Direct File Referencing (`@` mentions)**: Use the reference features (e.g., typing `@docs/ai-prompts/writing-tests-prompt.md` in Cursor/Windsurf) to instantly feed the complete API reference list directly into the model's active context window.

---

## 🔌 What are "Custom Skills" for Autonomous Agents?

If you are using fully autonomous coding agents (such as **Google Antigravity**, **Cursor Composer**, or other MCP-compatible systems), this repository contains pre-configured **Custom Skill Packages** in the root **[skills/](../../skills/)** directory:

* **Auto-Discovery**: Agentic tools automatically scan and discover files in customization roots (e.g., `skills/` or `.agents/skills/`).
* **Tool Equipping**: When you ask an agent to *"Set up architecture tests"* or *"Check our layer boundaries,"* the agent discovers these files, registers them as "skills," and loads the detailed markdown instructions into its background system prompts.
  * **[🤖 setup-konture](../../skills/setup-konture/SKILL.md)**: Installs the library and registers modules.
  * **[📐 konture-architecture-tests](../../skills/konture-architecture-tests/SKILL.md)**: Formulates boundary constraints across core pillars and custom project guidelines.
* **Frictionless Action**: Equipped with these rules, autonomous agents can safely run inspections, edit Gradle builds, create modules, and compile tests without needing your manual oversight at each step.

---

> [!TIP]
> ### 💡 Pro-Tip: Zero-Hallucination Testing
> When asking AI tools to write tests, always mention: *"Please check `@writing-tests-prompt.md` first to reconcile actual Konture DSL signatures rather than generating ArchUnit-style pseudo-code."* This saves time and ensures your generated code compiles perfectly on the first run.
