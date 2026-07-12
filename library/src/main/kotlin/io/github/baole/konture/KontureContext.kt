package io.github.baole.konture

/**
 * DSL Context wrapper that allows defining and verifying multiple independent rule suites.
 *
 * This context lets you specify module, class, function, property, and file assertions together,
 * and verify all of them in a single batch operation via [Konture.architecture].
 * All declared suites are executed even when earlier suites fail; violations are aggregated.
 */
@KontureDsl
class KontureContext(
    private val projectGraph: ProjectGraph,
) {
    private data class RuleSuite(
        val label: String,
        val run: () -> Unit,
    )

    private val ruleSuites = mutableListOf<RuleSuite>()

    private fun addSuite(
        label: String,
        block: () -> Unit,
    ) {
        val duplicateCount = ruleSuites.count { it.label == label }
        val resolvedLabel = if (duplicateCount > 0) "$label (${duplicateCount + 1})" else label
        ruleSuites.add(RuleSuite(resolvedLabel, block))
    }

    /**
     * Declares a suite of module structure/dependency rules inside this architecture validation context.
     */
    fun modules(block: ModulesRuleBuilder.() -> Unit) {
        val builder = ModulesRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("modules") { builder.check() }
    }

    /**
     * Declares a suite of class structure/dependency rules inside this architecture validation context.
     */
    fun classes(block: ClassesRuleBuilder.() -> Unit) {
        val builder = ClassesRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("classes") { builder.check() }
    }

    /**
     * Declares a suite of function structure/dependency rules inside this architecture validation context.
     */
    fun functions(block: FunctionsRuleBuilder.() -> Unit) {
        val builder = FunctionsRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("functions") { builder.check() }
    }

    /**
     * Declares a suite of property structure/dependency rules inside this architecture validation context.
     */
    fun properties(block: PropertiesRuleBuilder.() -> Unit) {
        val builder = PropertiesRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("properties") { builder.check() }
    }

    /**
     * Declares a suite of file structure/dependency rules inside this architecture validation context.
     */
    fun files(block: FilesRuleBuilder.() -> Unit) {
        val builder = FilesRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("files") { builder.check() }
    }

    /**
     * Declares a suite of layered-architecture rules inside this architecture validation context.
     */
    fun layeredArchitecture(block: LayeredArchitectureBuilder.() -> Unit) {
        val builder = LayeredArchitectureBuilder(projectGraph)
        builder.apply(block)
        addSuite("layeredArchitecture") { builder.check() }
    }

    /**
     * Declares a nested, type-safe layered-architecture specification inside this architecture validation context.
     */
    fun layered(block: LayeredArchitectureDsl.() -> Unit) {
        val dsl = LayeredArchitectureDsl(projectGraph)
        dsl.apply(block)
        addSuite("layered") { dsl.verify() }
    }

    internal fun verifyAll() {
        val failures = mutableListOf<String>()
        for (suite in ruleSuites) {
            try {
                suite.run()
            } catch (e: AssertionError) {
                failures.add("[${suite.label}]\n${e.message}")
            }
        }
        if (failures.isNotEmpty()) {
            throw AssertionError(
                "Architecture validation failed in ${failures.size} suite(s):\n\n" +
                    failures.joinToString("\n\n"),
            )
        }
    }
}
