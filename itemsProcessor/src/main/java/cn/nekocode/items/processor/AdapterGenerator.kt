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

import cn.nekocode.items.processor.model.Item
import cn.nekocode.items.processor.model.Selector
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class AdapterGenerator(
    private val mEvn: ProcessingEnvironment,
    private val mAdapter: TypeElement,
    private val mItems: List<Item>,
    private val mItemToId: Map<Item, Int>,
    private val mDataToItem: Map<TypeElement, Item?>,
    private val mDataToSelector: Map<TypeElement, Selector>
) {

    companion object {
        const val CLASSNAME_POSTFIX = "Impl"
    }

    fun generate() {
        val adapterName = mAdapter.qualifiedName
        val packageName = adapterName.substring(0, adapterName.length - mAdapter.simpleName.length - 1)
        val nonNullAnnotation = element(Names.NON_NULL)?.let { "@${it.simpleName}" } ?: ""

        // Obtain some elements
        val baseItemClass = element(Names.BASE_ITEM)
        val itemSelectorClass = element(Names.ITEM_SELECTOR)

        // Code of items initializing
        val initializingOfItems = mItems.joinToString("\n") {
            val id = mItemToId[it]
            """
    private final ${it.item.qualifiedName} mItem$id = new ${it.item.qualifiedName}(this, $id);
""".trimStartEndBlanks()
        }

        // Code of item array
        val itemArray = mItems.joinToString(", ") {
            "mItem${mItemToId[it]}"
        }

        // Code of map initializing
        var initializingOfMap = ""
        for ((data, item) in mDataToItem) {
            if (item == null) {
                continue
            }
            val id = mItemToId[item]
            initializingOfMap += "${indent(2)}mDataClassToViewType.put(${data.qualifiedName}.class, $id);\n"
        }
        if (initializingOfMap != "") {
            initializingOfMap += "\n"
        }
        for ((data, selector) in mDataToSelector) {
            initializingOfMap += """
        mDataClassToSelector.put(${data.qualifiedName}.class, new ${itemSelectorClass.simpleName}<${data.qualifiedName}>() {
           @Override
            public int select(int position, $nonNullAnnotation ${data.qualifiedName} data) {
                return ${selector.method.simpleName}(position, data);
            }
        });
""".trimStartEndBlanks()
        }

        // Code of item getters
        val itemGetters = mItems.joinToString("\n\n") {
            val id = mItemToId[it]
            """
    $nonNullAnnotation
    @Override
    public ${it.item.qualifiedName} ${it.method.simpleName}() {
        return mItem$id;
    }
""".trimStartEndBlanks()
        }

        // Code of class
        val code = """
package $packageName;

${if (nonNullAnnotation != "") "import ${Names.NON_NULL};" else ""}
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import ${baseItemClass.qualifiedName};
import ${itemSelectorClass.qualifiedName};
import java.util.HashMap;
import java.util.Map;

public class ${mAdapter.simpleName}$CLASSNAME_POSTFIX extends ${mAdapter.simpleName} {
$initializingOfItems

    private final BaseItem[] mItems;
    private final Map<Class, Integer> mDataClassToViewType;
    private final Map<Class, ${itemSelectorClass.simpleName}> mDataClassToSelector;

    {
        mItems = new BaseItem[] { $itemArray };
        mDataClassToViewType = new HashMap<>();
        mDataClassToSelector = new HashMap<>();

$initializingOfMap
    }

$itemGetters

    @Override
    public int getItemViewType(int position) {
        final Object data = getData(position);
        Integer viewType = mDataClassToViewType.get(data.getClass());
        if (viewType == null) {
            final ${itemSelectorClass.simpleName} selector = mDataClassToSelector.get(data.getClass());
            if (selector != null) {
                viewType = selector.select(position, data);
            }
        }
        if (viewType != null) {
            if (viewType < mItems.length) {
                return viewType;
            }
            throw new RuntimeException("Unknown view type: " + String.valueOf(viewType));
        }
        throw new RuntimeException("Unknown data type: " + data.getClass().getName());
    }

    $nonNullAnnotation
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder($nonNullAnnotation ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return mItems[viewType].onCreateViewHolder(inflater, viewGroup);
    }

    @Override
    public void onBindViewHolder($nonNullAnnotation RecyclerView.ViewHolder viewHolder, int position) {
        mItems[viewHolder.getItemViewType()].onBindViewHolder(viewHolder, position, getData(position));
    }
}
""".trimStartEndBlanks()

        // Write code of class to file
        mEvn.filer.createSourceFile("${mAdapter.qualifiedName}$CLASSNAME_POSTFIX").openWriter().use {
            it.write(code)
        }
    }

    private fun element(name: String) = mEvn.elementUtils.getTypeElement(name)

    private fun indent(count: Int) = "    ".repeat(count)

    private fun String.trimStartEndBlanks(): String {
        val lines = ArrayList(lines())
        if (lines.isNotEmpty()) {
            if (lines[0].isBlank()) {
                lines.removeAt(0)
            }
        }
        if (lines.isNotEmpty()) {
            val last = lines.size - 1
            if (lines[last].isBlank()) {
                lines.removeAt(last)
            }
        }
        return lines.joinToString("\n")
    }
}