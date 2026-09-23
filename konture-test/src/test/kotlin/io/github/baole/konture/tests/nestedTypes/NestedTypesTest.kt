/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.nestedTypes

import io.github.baole.konture.Konture
import io.github.baole.konture.Modifier
import io.github.baole.konture.beAssignableTo
import io.github.baole.konture.classes
import org.junit.jupiter.api.Test

class NestedTypesTest {
    @Test
    fun `nested data objects and data classes are verified by data modifier assertion`() {
        Konture.classes()
            .that().areAssignableTo(NestedType::class)
            .and().nameStartsWith("Data")
            .should().beData()
            .check()
    }

    @Test
    fun `nested interfaces in classes and objects are verified by interface assertion`() {
        Konture.classes()
            .that().areAssignableTo(NestedType::class)
            .and().nameStartsWith("NestedInterface")
            .should().beInterfaces()
            .check()
    }

    @Test
    fun `inner classes inside classes have inner modifier and implement supertype`() {
        Konture.classes()
            .that().named("InnerClassInClass")
            .should().haveAllModifiers(Modifier.INNER)
            .andShould().beAssignableTo(NestedType::class)
            .check()
    }

    @Test
    fun `nested enum classes inside classes are enums and implement supertype`() {
        Konture.classes()
            .that().named("NestedEnumInClass")
            .should().beEnums()
            .andShould().beAssignableTo(NestedType::class)
            .check()
    }

    @Test
    fun `nested classes in companion objects are discovered and implement supertype`() {
        Konture.classes()
            .that().named("ClassInCompanion")
            .should().beAssignableTo(NestedType::class)
            .check()
    }

    @Test
    fun `deeply nested types 3 levels deep are discovered with package matching`() {
        Konture.classes()
            .that().named("Level3Interface")
            .should().beInterfaces()
            .andShould().beAssignableTo(NestedType::class)
            .andShould().inPackage("io.github.baole.konture.tests.nestedTypes")
            .check()
    }
}
