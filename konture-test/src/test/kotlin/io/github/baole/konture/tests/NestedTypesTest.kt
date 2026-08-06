/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests

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
            .and().haveNameStartingWith("Data")
            .should().beData()
            .check()
    }

    @Test
    fun `nested interfaces in classes and objects are verified by interface assertion`() {
        Konture.classes()
            .that().areAssignableTo(NestedType::class)
            .and().haveNameStartingWith("NestedInterface")
            .should().beInterfaces()
            .check()
    }

    @Test
    fun `inner classes inside classes have inner modifier and implement supertype`() {
        Konture.classes()
            .that().haveName("InnerClassInClass")
            .should().haveAllModifiers(Modifier.INNER)
            .andShould().beAssignableTo(NestedType::class)
            .check()
    }

    @Test
    fun `nested enum classes inside classes are enums and implement supertype`() {
        Konture.classes()
            .that().haveName("NestedEnumInClass")
            .should().beEnums()
            .andShould().beAssignableTo(NestedType::class)
            .check()
    }

    @Test
    fun `nested classes in companion objects are discovered and implement supertype`() {
        Konture.classes()
            .that().haveName("ClassInCompanion")
            .should().beAssignableTo(NestedType::class)
            .check()
    }

    @Test
    fun `deeply nested types 3 levels deep are discovered with package matching`() {
        Konture.classes()
            .that().haveName("Level3Interface")
            .should().beInterfaces()
            .andShould().beAssignableTo(NestedType::class)
            .andShould().resideInAPackage("io.github.baole.konture.tests")
            .check()
    }
}
