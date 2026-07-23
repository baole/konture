/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

/** Kotlin and JVM types that Kotlin makes available without an explicit import. */
internal object KotlinDefaultTypes {
    val bySimpleName: Map<String, String> =
        mapOf(
            "Any" to "kotlin.Any", "Annotation" to "kotlin.Annotation", "Appendable" to "java.lang.Appendable",
            "Boolean" to "kotlin.Boolean", "Byte" to "kotlin.Byte", "Char" to "kotlin.Char",
            "CharSequence" to "kotlin.CharSequence", "Deprecated" to "kotlin.Deprecated",
            "DeprecatedSinceKotlin" to "kotlin.DeprecatedSinceKotlin", "DslMarker" to "kotlin.DslMarker",
            "Double" to "kotlin.Double", "Float" to "kotlin.Float", "Int" to "kotlin.Int", "Long" to "kotlin.Long",
            "Nothing" to "kotlin.Nothing", "Number" to "kotlin.Number", "OptIn" to "kotlin.OptIn",
            "RequiresOptIn" to "kotlin.RequiresOptIn", "PublishedApi" to "kotlin.PublishedApi",
            "Short" to "kotlin.Short", "String" to "kotlin.String", "Suppress" to "kotlin.Suppress",
            "SinceKotlin" to "kotlin.SinceKotlin", "Target" to "kotlin.annotation.Target", "Unit" to "kotlin.Unit",
            "Array" to "kotlin.Array", "Comparable" to "kotlin.Comparable", "Enum" to "kotlin.Enum",
            "Exception" to "kotlin.Exception", "Pair" to "kotlin.Pair", "Result" to "kotlin.Result",
            "Retention" to "kotlin.annotation.Retention", "Repeatable" to "kotlin.annotation.Repeatable",
            "Throwable" to "kotlin.Throwable", "Triple" to "kotlin.Triple",
            "MustBeDocumented" to "kotlin.annotation.MustBeDocumented", "JvmField" to "kotlin.jvm.JvmField",
            "JvmInline" to "kotlin.jvm.JvmInline", "JvmName" to "kotlin.jvm.JvmName",
            "JvmOverloads" to "kotlin.jvm.JvmOverloads", "JvmRecord" to "kotlin.jvm.JvmRecord",
            "JvmStatic" to "kotlin.jvm.JvmStatic", "JvmSynthetic" to "kotlin.jvm.JvmSynthetic",
            "Synchronized" to "kotlin.jvm.Synchronized", "Throws" to "kotlin.jvm.Throws",
            "Transient" to "kotlin.jvm.Transient", "Volatile" to "kotlin.jvm.Volatile",
            "Collection" to "kotlin.collections.Collection", "Iterable" to "kotlin.collections.Iterable",
            "Iterator" to "kotlin.collections.Iterator", "List" to "kotlin.collections.List",
            "Map.Entry" to "kotlin.collections.Map.Entry", "MutableCollection" to "kotlin.collections.MutableCollection",
            "MutableIterable" to "kotlin.collections.MutableIterable", "MutableIterator" to "kotlin.collections.MutableIterator",
            "MutableList" to "kotlin.collections.MutableList", "Set" to "kotlin.collections.Set",
            "MutableSet" to "kotlin.collections.MutableSet", "Map" to "kotlin.collections.Map",
            "MutableMap" to "kotlin.collections.MutableMap",
        )
}
