/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.baole.konture.tests.defaultImports

import java.io.File
import java.util.Collection as JavaCollection

class CustomCollection

object DefaultImports {
    fun run(dir: File): String =
        ProcessBuilder(listOf("echo", "hi")).directory(dir).start()
            .inputStream.bufferedReader().readText()

    fun kotlinCollection(): Collection<String> = listOf("a", "b")

    fun javaCollection(coll: JavaCollection<String>): Int = coll.size()

    fun customCollection(): CustomCollection = CustomCollection()

    fun aliasType(): AliasArrayList = AliasArrayList()

    // Additional package coverage demonstrations
    fun rangeCheck(i: Int): IntRange = 1..10

    fun sequenceCheck(): Sequence<Int> = sequenceOf(1, 2, 3)

    fun textCheck(): StringBuilder = StringBuilder("hello")

    fun systemCheck(): String = System.getProperty("user.name") ?: ""

    fun regexCheck(): Regex = Regex(".*")

    fun comparatorCheck(): Comparator<String> = compareBy { it.length }

    fun annotationRetention(): AnnotationRetention = AnnotationRetention.RUNTIME

    fun exceptionCheck(): Exception = IllegalArgumentException("invalid argument")
}
