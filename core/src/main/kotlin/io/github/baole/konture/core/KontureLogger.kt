package io.github.baole.konture.core

object KontureLogger {
    var minLevel: LogLevel = LogLevel.INFO

    var logger: (level: LogLevel, message: String, throwable: Throwable?) -> Unit = {
            level,
            message,
            throwable,
        ->
        if (level >= minLevel) {
            val prefix =
                when (level) {
                    LogLevel.WARNING -> "[Konture] ⚠️ WARNING:"
                    LogLevel.ERROR -> "[Konture] ❌ ERROR:"
                    else -> "[Konture] [${level.name}]:"
                }
            println("$prefix $message")
            throwable?.printStackTrace()
        }
    }

    fun log(
        level: LogLevel,
        message: String,
        throwable: Throwable? = null,
    ) {
        logger(level, message, throwable)
    }
}
