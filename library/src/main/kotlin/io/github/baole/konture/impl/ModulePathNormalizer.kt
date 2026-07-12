package io.github.baole.konture.impl

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel

/**
 * Normalizes Gradle module paths by prepending ':' when missing.
 * Glob patterns starting with '**' are left unchanged.
 */
internal fun normalizeModulePath(path: String): String =
    if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
        KontureLogger.log(
            LogLevel.WARNING,
            "Module path '$path' lacks a leading colon (':'). Suggest matching with ':$path' instead.",
        )
        ":$path"
    } else {
        path
    }
