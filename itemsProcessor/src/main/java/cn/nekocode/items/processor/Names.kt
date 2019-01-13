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

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
object Names {
    const val ADAPTER_ANNOTATION = "cn.nekocode.items.annotation.AdapterClass"
    const val ITEM_ANNOTATION = "cn.nekocode.items.annotation.ItemMethod"
    const val SELECTOR_ANNOTATION = "cn.nekocode.items.annotation.SelectorMethod"

    const val ITEM_ADAPTER = "cn.nekocode.items.ItemAdapter"
    const val BASE_ITEM = "cn.nekocode.items.BaseItem"
    const val ITEM_SELECTOR = "cn.nekocode.items.ItemSelector"
    const val GET_DATA = "getData"
    const val GET_ITEM_COUNT = "getItemCount"

    const val RECYCLER_VIEW_ADAPTER = "android.support.v7.widget.RecyclerView.Adapter"
    const val NON_NULL = "android.support.annotation.NonNull"
}