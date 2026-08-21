/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.classstructure

import io.github.baole.konture.Konture
import io.github.baole.konture.Modifier
import io.github.baole.konture.Visibility
import io.github.baole.konture.beAssignableTo
import io.github.baole.konture.beAssignableToAllOf
import io.github.baole.konture.beAssignableToAnyOf
import io.github.baole.konture.beAssignableFrom
import io.github.baole.konture.classes
import org.junit.jupiter.api.Test

class ClassStructureAndMetadataTest {
    private val pkg = "io.github.baole.konture.tests.classstructure"

    @Test
    fun `interface and abstract class assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("SampleInterface")
            should().beInterfaces().andShould().notBeData()
        }

        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("SampleAbstractBase")
            should().beAbstract().andShould().notBeInterface().andShould().notBeSealed()
        }
    }

    @Test
    fun `enum class assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("SampleEnum")
            should().beEnums().andShould().notBeInterface().andShould().notBeAbstract()
        }
    }

    @Test
    fun `open and data class assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("SampleOpenClass")
            should().beOpen().andShould().notBeData().andShould().notBeInner()
        }

        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("ConfiguredDataClass")
            should().beData().andShould().notBeOpen().andShould().notBeInterface()
        }
    }

    @Test
    fun `value and inner class assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("SampleValueClass")
            should().beInline().andShould().notBeInner()
        }

        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("InnerMember")
            should().beInner().andShould().notBeData()
        }
    }

    @Test
    fun `top level vs nested class assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("OuterContainer")
            should().beTopLevel()
        }

        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("NestedStatic")
            should().beNested()
        }
    }

    @Test
    fun `type hierarchy and assignability assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("ConfiguredDataClass")
            should().beAssignableTo<SampleInterface>()
                .andShould().beAssignableToAnyOf("io.github.baole.konture.tests.classstructure.SampleInterface", "java.lang.Object")
                .andShould().beAssignableToAllOf("io.github.baole.konture.tests.classstructure.SampleInterface")
        }

        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("SampleInterface")
            should().beAssignableFrom<ConfiguredDataClass>()
        }
    }

    @Test
    fun `member function and property containment assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("ConfiguredDataClass")
            should().containProperty("title")
                .andShould().containProperty("count")
                .andShould().notContainProperty("nonExistentProp")
                .andShould().containFunction("customMethod")
                .andShould().notContainFunction("nonExistentMethod")
        }
    }

    @Test
    fun `modifier list and visibility assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("ConfiguredDataClass")
            should().haveAllModifiers(Modifier.DATA)
                .andShould().haveAnyModifier(Modifier.DATA, Modifier.OPEN)
                .andShould().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL)
        }
    }

    @Test
    fun `annotation with argument matching assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("ConfiguredDataClass")
            should().haveAnnotationWithArgument("CustomConfig", "key", "feature")
        }
    }
}
