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
    private val env: ProcessingEnvironment,
    private val adapter: TypeElement,
    private val items: List<Item>,
    private val itemToIds: Map<Item, Int>,
    private val dataToItems: Map<TypeElement, Item?>,
    private val dataToSelectors: Map<TypeElement, Selector>
) {

    companion object {
        const val CLASSNAME_POSTFIX = "_Impl"
    }

    fun generate() {
        val adapterName = adapter.qualifiedName
        val packageName = adapterName.substring(0, adapterName.length - adapter.simpleName.length - 1)
        val nonNull = element(Names.NON_NULL)?.let { "@${it.simpleName}" } ?: ""

        // Obtain some elements
        val itemClass = element(Names.ITEM)
        val itemSelectorClass = element(Names.ITEM_SELECTOR)

        // Code of map initializing
        var mapInitializing = ""
        for ((data, item) in dataToItems) {
            if (item == null) {
                continue
            }
            val id = itemToIds[item]!!
            mapInitializing += "${indent(2)}viewTypes.put(${data.qualifiedName}.class, $id);\n"
        }
        if (mapInitializing != "") {
            mapInitializing += "\n"
        }
        for ((data, selector) in dataToSelectors) {
            mapInitializing += """
        selectors.put(${data.qualifiedName}.class, new ${itemSelectorClass.simpleName}<${data.qualifiedName}>() {
           @Override
            public int select(int position, $nonNull ${data.qualifiedName} data) {
                return ${selector.method.simpleName}(position, data);
            }
        });
""".trimStartEndBlanks()
        }
//        mapInitializing.prependIndent(indent(2))

        // Code of delegate methods
        val itemMethods = items.joinToString("\n") {
            val id = itemToIds[it]
            """
    private final ${it.item.qualifiedName} item$id =
            new ${it.item.qualifiedName}(this, $id);
    $nonNull
    @Override
    public ${it.item.qualifiedName} ${it.method.simpleName}() {
        return item$id;
    }

""".trimStartEndBlanks()
        }

        // Code of holder switch cases
        val holderSwitchCases = items.joinToString("\n") {
            val id = itemToIds[it]
            """
            case $id: {
                holder = item$id.onCreateViewHolder(inflater, viewGroup);
                break;
            }
""".trimStartEndBlanks()
        }

        // Code of holder bindings
        val holderBindings = items.joinToString("\n") {
            val id = itemToIds[it]
            """
            case $id: {
                item$id.onBindViewHolder((${it.holder.qualifiedName}) viewHolder, position, getData(position));
                break;
            }
""".trimStartEndBlanks()
        }

        // Code of class
        val code = """
package $packageName;

${if (nonNull != "") "import ${Names.NON_NULL};" else ""}
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import ${itemClass.qualifiedName};
import ${itemSelectorClass.qualifiedName};
import java.util.HashMap;

public class ${adapter.simpleName}$CLASSNAME_POSTFIX extends ${adapter.simpleName} {
    private final HashMap<Class, Integer> viewTypes = new HashMap<>();
    private final HashMap<Class, ${itemSelectorClass.simpleName}> selectors = new HashMap<>();
    {
$mapInitializing
    }

$itemMethods

    @Override
    public int getItemViewType(int position) {
        final Object data = getData(position);
        final Integer viewType = viewTypes.get(data.getClass());
        if (viewType != null) {
            return viewType;
        } else {
            final ${itemSelectorClass.simpleName} selector = selectors.get(data.getClass());
            if (selector != null) {
                return selector.select(position, data);
            }
        }
        throw new RuntimeException("Unknown data type: " + data.getClass().getName());
    }

    $nonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder($nonNull ViewGroup viewGroup, int viewType) {
        final RecyclerView.ViewHolder holder;
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        switch (viewType) {
$holderSwitchCases
            default: {
                holder = null;
            }
        }
        if (holder == null) {
            throw new RuntimeException("Unsupported view type.");
        }
        return holder;
    }

    @Override
    public void onBindViewHolder($nonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
$holderBindings
        }
    }
}
""".trimStartEndBlanks()

        // Write code of class to file
        env.filer.createSourceFile("${adapter.qualifiedName}$CLASSNAME_POSTFIX").openWriter().use {
            it.write(code)
        }
    }

    private fun element(name: String) = env.elementUtils.getTypeElement(name)

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