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

    companion object {
        const val VIEW = "android.view.View"
        const val VIEW_GROUP = "android.view.ViewGroup"
        const val LAYOUT_INFLATER = "android.view.LayoutInflater"
        const val METHOD_1 = "@Override public <T> T getData(int p) { return null; }"
        const val METHOD_2 = "@Override public int getItemCount() { return 0; }"
    }

    @Test
    fun annotateInterface() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile("interface", "")))
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
            .that(arrayListOf(adapterFile("class")))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining(
                "The adapter should be abstract"
            )
    }

    @Test
    fun notExtendsAdapter() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(extends = "")))
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
            .that(arrayListOf(adapterFile()))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The adapter class should override method")

        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(body = METHOD_1)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The adapter class should override method ${Names.GET_ITEM_COUNT}")

        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(body = METHOD_2)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The adapter class should override method ${Names.GET_DATA}")

        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(body = METHOD_1 + METHOD_2)))
            .processedWith(ItemsProcessor())
            .compilesWithoutError()
    }

    @Test
    fun delegateMethods() {
        fun wrap(body: String) = """
            $METHOD_1
            $METHOD_2
            @${Names.VIEW_DELEGATE}
            $body
        """.trimIndent()

        var body = wrap("public com.test.TestItemView.Delegate testItemView() {};")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemViewFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The delegate method should be abstract")

        body = wrap("public abstract com.test.TestItemView.Delegate testItemView(int i) {};")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemViewFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The delegate method should not have parameters")

        body = wrap("public abstract com.test.TestItemView.Delegate testItemView();")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemViewFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .compilesWithoutError()
    }

    private fun adapterFile(
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

    private fun itemViewFile(
    ) = JavaFileObjects.forSourceString(
        "com.test.TestItemView",
        """
            package com.test;
            public class TestItemView extends ${Names.ITEM_VIEW}<String, TestItemView.Callback> {
                @Override
                public $VIEW onCreateItemView($LAYOUT_INFLATER i, $VIEW_GROUP p) {
                    return null;
                }
                @Override
                public void onBindData(String d) {
                }
                @${Names.VIEW_DELEGATE_OF}(TestItemView.class)
                interface Delegate extends ${Names.ITEM_VIEW_DELEGATE}<Callback> {}
                public interface Callback {
                    void onClick(String d);
                }
            }
        """.trimIndent()
    )
}