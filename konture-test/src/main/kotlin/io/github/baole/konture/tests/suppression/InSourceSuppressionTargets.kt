/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.suppression

@Suppress("konture:classes.rule")
class InSourceSuppressedClass {
    val message: String = "Suppressed Class"
}

@Suppress("konture:*")
class WildcardSuppressedClass {
    val message: String = "Wildcard Suppressed Class"
}

@java.lang.SuppressWarnings("konture:classes.rule")
class JavaAnnotationSuppressedClass {
    val message: String = "Java Annotation Suppressed Class"
}

class MemberSuppressedClass {
    @Suppress("konture:functions.rule")
    fun suppressedMemberFunc() {
    }

    @Suppress("konture:properties.rule")
    val suppressedMemberProp: String = "Suppressed Member Prop"
}
