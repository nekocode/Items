/*
 * Copyright 2016 nekocode
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

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nekocode on 16/8/23.
 */
class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ItemPool itemPool;

    public ItemAdapter(ItemPool itemPool) {
        this.itemPool = itemPool;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Item item = newItem(itemPool.getItemClass(viewType));
        return item.onCreateViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Object data = itemPool.get(position);
        Pair<Class<? extends Item>, ItemEventHandler> pair = itemPool.getItemClass(data.getClass());

        final Item item = ((Item.InternalViewHolder) holder).item;
        final ItemEventHandler handler = pair.second;
        item.onBindItem(holder, data, handler);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handler != null)
                    handler.onEvent(item.getClass(), new ItemEvent(ItemEvent.ITEM_CLICK, data, holder));
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (handler != null) {
                    handler.onEvent(item.getClass(), new ItemEvent(ItemEvent.ITEM_LONGCLICK, data, holder));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemPool.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemPool.getItemType(position);
    }

    private static <ItemToCreate extends Item> ItemToCreate newItem(Class<ItemToCreate> itemClass) {
        Item item;
        try {
            item = itemClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return (ItemToCreate) item;
    }
}
