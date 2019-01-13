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
        const val RECYCLER_VIEW = "android.support.v7.widget.RecyclerView"
        const val METHOD_1 = "@Override public <T> T getData(int p) { return null; }"
        const val METHOD_2 = "@Override public int getItemCount() { return 0; }"
    }

    @Test
    fun annotateInterface() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(type = "interface", extends = "", implements = "")))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining(
                "The @${Names.ADAPTER_ANNOTATION} should not annotates to interface"
            )
    }

    @Test
    fun notAbstractClass() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(type = "class")))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining(
                "This adapter should be abstract"
            )
    }

    @Test
    fun notImplementsAdapter() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(implements = "")))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining(
                "This adapter class should implements interface ${Names.ITEM_ADAPTER}"
            )
    }

    @Test
    fun constructor() {
        val body = "public TestAdapter(int i) {}"
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining(
                "This adapter should not have constructor having parameters"
            )
    }

    @Test
    fun notOverrideMethods() {
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile()))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("This adapter class should override method")

        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(body = METHOD_1)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("This adapter class should override method ${Names.GET_ITEM_COUNT}")

        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(body = METHOD_2)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("This adapter class should override method ${Names.GET_DATA}")

        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(adapterFile(body = METHOD_1 + METHOD_2)))
            .processedWith(ItemsProcessor())
            .compilesWithoutError()

        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(
                adapterFile(className = "BaseAdapter", annotation = "", body = METHOD_1 + METHOD_2),
                adapterFile(body = METHOD_1 + METHOD_2, extends = "extends BaseAdapter", implements = "")
            ))
            .processedWith(ItemsProcessor())
            .compilesWithoutError()
    }

    @Test
    fun itemMethods() {
        fun wrap(method: String, others: String = "") = """
            $METHOD_1
            $METHOD_2
            @${Names.ITEM_ANNOTATION}
            $method
            $others
        """.trimIndent()

        var body = wrap("public com.test.TestItem testItem() {};")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("This item method should be abstract")

        body = wrap("public abstract com.test.TestItem testItem(int i);")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("This item method should not have parameters")

        body = wrap("public abstract com.test.TestItem testItem();")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(type = "abstract class"), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The return type of this item method should not be abstract")

        body = wrap("public abstract com.test.TestItem testItem();")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(extends = ""), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The return type of this item method should extends")

        body = wrap("public abstract com.test.TestItem testItem();")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .compilesWithoutError()

        body = wrap(
            "public abstract com.test.TestItem testItem();",
            "@${Names.ITEM_ANNOTATION} public abstract com.test.TestItem testItem2();"
        )
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("There is a duplicate item")
    }

    @Test
    fun selectorMethods() {
        fun wrap(others: String) = """
            $METHOD_1
            $METHOD_2
            $others
        """.trimIndent()

        var body = wrap("@${Names.SELECTOR_ANNOTATION} public abstract int viewTypeForString(int p, String d);")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("This selector method should be implemented")

        body = wrap("@${Names.SELECTOR_ANNOTATION} public int viewTypeForString(int p) { return 0; };")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The parameters of this selector method should be (int index, YourDataType data)")

        body = wrap("@${Names.SELECTOR_ANNOTATION} public int viewTypeForString(String p, String d) { return 0; };")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("The parameters of this selector method should be (int index, YourDataType data)")

        body = wrap("@${Names.SELECTOR_ANNOTATION} public int viewTypeForString(int p, String d) { return 0; };")
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .compilesWithoutError()

        body = wrap(
            "@${Names.SELECTOR_ANNOTATION} public int viewTypeForString(int p, String d) { return 0; };" +
                    "@${Names.SELECTOR_ANNOTATION} public int viewTypeForString2(int p, String d) { return 0; };"
        )
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(arrayListOf(itemFile(), adapterFile(body = body)))
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("More than one selector method has one same data type in adapter")

        body = wrap(
            "@${Names.ITEM_ANNOTATION} public abstract com.test.TestItem testItem();" +
                    "@${Names.ITEM_ANNOTATION} public abstract com.test.TestItem2 testItem2();"
        )
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(
                arrayListOf(
                    itemFile(),
                    itemFile(className = "TestItem2"),
                    adapterFile(body = body)
                )
            )
            .processedWith(ItemsProcessor())
            .failsToCompile()
            .withErrorContaining("Missing view selector for duplicate data type")

        body = wrap(
            "@${Names.ITEM_ANNOTATION} public abstract com.test.TestItem testItem();" +
                    "@${Names.ITEM_ANNOTATION} public abstract com.test.TestItem2 testItem2();" +
                    "@${Names.SELECTOR_ANNOTATION} public int viewTypeForString(int p, String d) { return 0; };"
        )
        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(
                arrayListOf(
                    itemFile(),
                    itemFile(className = "TestItem2"),
                    adapterFile(body = body)
                )
            )
            .processedWith(ItemsProcessor())
            .compilesWithoutError()
    }

    private fun adapterFile(
        className: String = "TestAdapter",
        annotation: String = "@${Names.ADAPTER_ANNOTATION}",
        type: String = "abstract class",
        extends: String = "extends ${Names.RECYCLER_VIEW_ADAPTER}<$RECYCLER_VIEW.ViewHolder>",
        implements: String = "implements ${Names.ITEM_ADAPTER}",
        body: String = ""
    ) = JavaFileObjects.forSourceString(
        "com.test.$className",
        """
package com.test;
$annotation
public $type $className $extends $implements {
    $body
}
        """.trimIndent()
    )

    private fun itemFile(
        className: String = "TestItem",
        type: String = "class",
        extends: String = "extends ${Names.BASE_ITEM}<String, $className.Holder, $className.Callback>"
    ) = JavaFileObjects.forSourceString(
        "com.test.$className",
        """
package com.test;
public $type $className $extends {
    public $className(${Names.ITEM_ADAPTER} adapter, int viewType) {
        super(adapter, viewType);
    }
    @Override
    public Holder onCreateViewHolder($LAYOUT_INFLATER i, $VIEW_GROUP p) {
        return null;
    }
    @Override
    public void onBindViewHolder(Holder h, int p, String d) {
    }
    static class Holder extends $RECYCLER_VIEW.ViewHolder {
        Holder(android.view.View itemView) {
            super(itemView);
        }
    }
    public interface Callback {
        void onClick(String d);
    }
}
        """.trimIndent()
    )
}