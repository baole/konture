/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

public object KontureLogger {
    private val threadLocalMinLevel = ThreadLocal.withInitial { LogLevel.INFO }
    private val threadLocalLogger =
        ThreadLocal.withInitial<(LogLevel, String, Throwable?) -> Unit> {
            { level, message, throwable ->
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
        }

    public var minLevel: LogLevel
        get() = threadLocalMinLevel.get()
        set(value) {
            threadLocalMinLevel.set(value)
        }

    public var logger: (level: LogLevel, message: String, throwable: Throwable?) -> Unit
        get() = threadLocalLogger.get()
        set(value) {
            threadLocalLogger.set(value)
        }

    public fun log(
        level: LogLevel,
        message: String,
        throwable: Throwable? = null,
    ) {
        logger(level, message, throwable)
    }
}
