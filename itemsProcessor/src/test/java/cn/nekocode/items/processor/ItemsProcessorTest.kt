/*
 * Copyright 2018. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.items.processor

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class ItemsProcessorTest {

    private fun javaFile(
        type: String = "abstract class",
        extends: String = "extends ${Names.ITEM_ADAPTER}",
        body: String = ""
    ) = JavaFileObjects.forSourceString(
        "com.test.TestAdapter",
        """
            package com.test;
            @${Names.ADAPTER}
            public $type TestAdapter $extends { $body }
        """.trimIndent()
    )

    @Test
    fun annotateInterface() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(javaFile("interface", "")))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining(
                "The @${Names.ADAPTER} should not annotates to interface"
            )
    }

    @Test
    fun notAbstractClass() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(javaFile("class")))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining(
                "The @${Names.ADAPTER} should be abstract"
            )
    }

    @Test
    fun notExtendsAdapter() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(javaFile(extends = "")))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining(
                "The adapter class should extends class ${Names.ITEM_ADAPTER}"
            )
    }

    @Test
    fun notOverrideMethods() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(javaFile()))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The adapter class should override method")

        var body = "@Override public <T> T getData(int position) { return null; }"
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(javaFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The adapter class should override method ${Names.GET_ITEM_COUNT}")

        body = "@Override public int getItemCount() { return 0; }"
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(javaFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The adapter class should override method ${Names.GET_DATA}")

        body = "@Override public <T> T getData(int position) { return null; }" +
                "@Override public int getItemCount() { return 0; }"
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(javaFile(body = body)))
            .processedWith(ItemsProcessor())
            .compilesWithoutError()
    }
}