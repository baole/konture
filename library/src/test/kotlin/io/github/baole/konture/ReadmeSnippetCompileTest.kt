package io.github.baole.konture

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReadmeSnippetCompileTest : RuleBuildersTestBase() {
    @BeforeEach
    override fun setUp() {
        // Create mock classes representing the domain layer
        val userRepository =
            ClassDeclaration(
                name = "UserRepository",
                fqName = "com.example.domain.UserRepository",
                packageName = "com.example.domain",
                isInterface = true,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/UserRepository.kt",
            )

        val fileDecl = FileDeclaration("UserRepository.kt", "com.example.domain", classes = listOf(userRepository))

        val domainModule =
            Module(
                buildId = ":",
                path = ":core:domain",
                projectDir = "core/domain",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )

        val dataModule =
            Module(
                buildId = ":",
                path = ":core:data",
                projectDir = "core/data",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = emptyList(),
            )

        val checkoutModule =
            Module(
                buildId = ":",
                path = ":feature:checkout",
                projectDir = "feature/checkout",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = emptyList(),
            )

        projectGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(domainModule, dataModule, checkoutModule)),
            )
        ProjectGraph.setDefault(projectGraph)
    }

    @Test
    fun `verify README and skill template architecture guardrail snippet compiles and runs successfully`() {
        architecture {
            // 🎯 Select modules inside domain
            modules {
                that().haveNamePath(":core:domain")
                should().notDependOnModule(":core:data")
                andShould().notDependOnModule(":feature:checkout")
            }

            // 🎯 Verify class boundary rules
            classes {
                that().resideInAPackage("..domain..")
                that().haveNameEndingWith("Repository")
                should().beInterfaces()
            }
        }
    }
}
