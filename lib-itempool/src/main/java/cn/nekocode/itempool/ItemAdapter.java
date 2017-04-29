/*
 * Copyright 2016 nekocode (nekocode.cn@gmail.com)
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
package cn.nekocode.itempool;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ItemPool itemPool;


    ItemAdapter(ItemPool itemPool) {
        this.itemPool = itemPool;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Class<? extends Item> itemClass = itemPool.getItemClass(viewType);
        final ItemPool.ItemType itemType = itemPool.getItemType(itemClass);
        return newItem(itemClass).onCreateViewHolder(this, itemType.getHandler(), parent);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Object data = itemPool.get(position);
        final Item item = ((Item.ViewHolder) holder).item;
        item.onBindData(data);
    }

    @Override
    public int getItemCount() {
        return itemPool.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemPool.getItemType(position);
    }

    private static <T extends Item> T newItem(Class<T> itemClass) {
        Item item;
        try {
            item = itemClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return (T) item;
    }
}
