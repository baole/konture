package io.github.baole.konture.core

import kotlinx.serialization.Serializable

@Serializable
data class ExclusionsModel(
    val excludeModules: List<String> = emptyList(),
    val excludePackages: List<String> = emptyList(),
    val excludeClasses: List<String> = emptyList(),
    val excludeConfigurations: List<String> = listOf("test", "benchmark", "profile", "testedapks"),
)
