/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

/** Kotlin and JVM types that Kotlin makes available without an explicit import. */
internal object KotlinDefaultTypes {
    val defaultPackages: List<String> =
        listOf(
            "kotlin",
            "kotlin.annotation",
            "kotlin.collections",
            "kotlin.comparisons",
            "kotlin.io",
            "kotlin.ranges",
            "kotlin.sequences",
            "kotlin.text",
            "kotlin.math",
            "java.lang",
            "kotlin.jvm",
            "kotlin.js",
        )

    private val coreTypes: Map<String, String> =
        mapOf(
            "Any" to "kotlin.Any",
            "Boolean" to "kotlin.Boolean",
            "Byte" to "kotlin.Byte",
            "Char" to "kotlin.Char",
            "CharSequence" to "kotlin.CharSequence",
            "Double" to "kotlin.Double",
            "Float" to "kotlin.Float",
            "Int" to "kotlin.Int",
            "Long" to "kotlin.Long",
            "Nothing" to "kotlin.Nothing",
            "Number" to "kotlin.Number",
            "Short" to "kotlin.Short",
            "String" to "kotlin.String",
            "Unit" to "kotlin.Unit",
            "Array" to "kotlin.Array",
            "Comparable" to "kotlin.Comparable",
            "Enum" to "kotlin.Enum",
            "Exception" to "kotlin.Exception",
            "Pair" to "kotlin.Pair",
            "Result" to "kotlin.Result",
            "Throwable" to "kotlin.Throwable",
            "Triple" to "kotlin.Triple",
            "Deprecated" to "kotlin.Deprecated",
            "DeprecatedSinceKotlin" to "kotlin.DeprecatedSinceKotlin",
            "DslMarker" to "kotlin.DslMarker",
            "OptIn" to "kotlin.OptIn",
            "RequiresOptIn" to "kotlin.RequiresOptIn",
            "PublishedApi" to "kotlin.PublishedApi",
            "Suppress" to "kotlin.Suppress",
            "SinceKotlin" to "kotlin.SinceKotlin",
            "Annotation" to "kotlin.Annotation",
        )

    private val annotationTypes: Map<String, String> =
        mapOf(
            "Target" to "kotlin.annotation.Target",
            "Retention" to "kotlin.annotation.Retention",
            "Repeatable" to "kotlin.annotation.Repeatable",
            "MustBeDocumented" to "kotlin.annotation.MustBeDocumented",
            "AnnotationTarget" to "kotlin.annotation.AnnotationTarget",
            "AnnotationRetention" to "kotlin.annotation.AnnotationRetention",
        )

    private val collectionTypes: Map<String, String> =
        mapOf(
            "Collection" to "kotlin.collections.Collection",
            "Iterable" to "kotlin.collections.Iterable",
            "Iterator" to "kotlin.collections.Iterator",
            "List" to "kotlin.collections.List",
            "Map" to "kotlin.collections.Map",
            "Map.Entry" to "kotlin.collections.Map.Entry",
            "MutableCollection" to "kotlin.collections.MutableCollection",
            "MutableIterable" to "kotlin.collections.MutableIterable",
            "MutableIterator" to "kotlin.collections.MutableIterator",
            "MutableList" to "kotlin.collections.MutableList",
            "MutableMap" to "kotlin.collections.MutableMap",
            "MutableMap.MutableEntry" to "kotlin.collections.MutableMap.MutableEntry",
            "MutableSet" to "kotlin.collections.MutableSet",
            "Set" to "kotlin.collections.Set",
            "ArrayList" to "kotlin.collections.ArrayList",
            "HashMap" to "kotlin.collections.HashMap",
            "HashSet" to "kotlin.collections.HashSet",
            "LinkedHashMap" to "kotlin.collections.LinkedHashMap",
            "LinkedHashSet" to "kotlin.collections.LinkedHashSet",
        )

    private val comparisonTypes: Map<String, String> =
        mapOf(
            "Comparator" to "kotlin.comparisons.Comparator",
        )

    private val rangeAndSequenceTypes: Map<String, String> =
        mapOf(
            "IntRange" to "kotlin.ranges.IntRange",
            "LongRange" to "kotlin.ranges.LongRange",
            "CharRange" to "kotlin.ranges.CharRange",
            "Sequence" to "kotlin.sequences.Sequence",
        )

    private val textTypes: Map<String, String> =
        mapOf(
            "StringBuilder" to "kotlin.text.StringBuilder",
            "Regex" to "kotlin.text.Regex",
            "MatchResult" to "kotlin.text.MatchResult",
        )

    private val javaLangTypes: Map<String, String> =
        mapOf(
            "Appendable" to "java.lang.Appendable",
            "ProcessBuilder" to "java.lang.ProcessBuilder",
            "Process" to "java.lang.Process",
            "Thread" to "java.lang.Thread",
            "ThreadLocal" to "java.lang.ThreadLocal",
            "System" to "java.lang.System",
            "Runtime" to "java.lang.Runtime",
            "Class" to "java.lang.Class",
            "ClassLoader" to "java.lang.ClassLoader",
            "Object" to "java.lang.Object",
            "Void" to "java.lang.Void",
            "AutoCloseable" to "java.lang.AutoCloseable",
            "Cloneable" to "java.lang.Cloneable",
            "Runnable" to "java.lang.Runnable",
            "IllegalArgumentException" to "java.lang.IllegalArgumentException",
            "IllegalStateException" to "java.lang.IllegalStateException",
            "NullPointerException" to "java.lang.NullPointerException",
            "UnsupportedOperationException" to "java.lang.UnsupportedOperationException",
            "IndexOutOfBoundsException" to "java.lang.IndexOutOfBoundsException",
        )

    private val jvmTypes: Map<String, String> =
        mapOf(
            "JvmField" to "kotlin.jvm.JvmField",
            "JvmInline" to "kotlin.jvm.JvmInline",
            "JvmName" to "kotlin.jvm.JvmName",
            "JvmOverloads" to "kotlin.jvm.JvmOverloads",
            "JvmRecord" to "kotlin.jvm.JvmRecord",
            "JvmStatic" to "kotlin.jvm.JvmStatic",
            "JvmSynthetic" to "kotlin.jvm.JvmSynthetic",
            "Synchronized" to "kotlin.jvm.Synchronized",
            "Throws" to "kotlin.jvm.Throws",
            "Transient" to "kotlin.jvm.Transient",
            "Volatile" to "kotlin.jvm.Volatile",
        )

    val bySimpleName: Map<String, String> =
        coreTypes + annotationTypes + collectionTypes +
            comparisonTypes + rangeAndSequenceTypes + textTypes +
            javaLangTypes + jvmTypes
}
