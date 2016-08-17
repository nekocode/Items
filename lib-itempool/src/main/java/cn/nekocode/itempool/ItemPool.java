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

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nekocode on 16/8/16.
 */
public final class ItemPool extends ArrayList<Object> {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final HashMap<Class, ItemWrapper> mapOfWrapper = new HashMap<>();
    private final SparseArray<Item> mapOfItem = new SparseArray<>();
    private final DecoupleAdapter internalAdapter = new DecoupleAdapter();

    public void addItemType(@NonNull Class<? extends Item> itemClass) {
        ParameterizedType parameterizedType = (ParameterizedType) itemClass.getGenericSuperclass();
        Class dataClass = (Class) parameterizedType.getActualTypeArguments()[0];

        Item item;
        try {
            item = itemClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        ItemWrapper wrapper = new ItemWrapper(item);
        mapOfWrapper.put(dataClass, wrapper);
        mapOfItem.put(wrapper.TYPE_ID, item);
    }

    public final void setEventHandler(ItemEventHandler handler) {
        Collection<ItemWrapper> itemWrappers = mapOfWrapper.values();

        for (ItemWrapper wrapper : itemWrappers) {
            wrapper.handler = handler;
        }
    }

    @NonNull
    public DecoupleAdapter getAdapter() {
        return internalAdapter;
    }


    /**
     * Private members
     */

    private int getItemType(int index) {
        Class dataClass = get(index).getClass();
        return mapOfWrapper.get(dataClass).TYPE_ID;
    }

    private Item getItem(int typeId) {
        return mapOfItem.get(typeId);
    }

    private Pair getItem(Class dataClass) {
        ItemWrapper wrapper = mapOfWrapper.get(dataClass);
        return new Pair(wrapper.item, wrapper.handler);
    }

    private static class ItemWrapper {
        int TYPE_ID;
        ItemEventHandler handler;
        Item item;

        ItemWrapper(Item item) {
            TYPE_ID = ID_COUNTER.getAndIncrement();
            this.item = item;
        }
    }

    private class DecoupleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ItemPool items;

        public DecoupleAdapter() {
            this.items = ItemPool.this;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Item item = items.getItem(viewType);
            RecyclerView.ViewHolder holder =
                    item.onCreateViewHolder(LayoutInflater.from(parent.getContext()), parent);
            return holder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            final Object data = items.get(position);
            Pair<Item, ItemEventHandler> pair = items.getItem(data.getClass());

            final Item item = pair.first;
            final ItemEventHandler handler = pair.second;
            item.onBindViewHolder(holder, data, handler);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (handler != null)
                        handler.onEvent(item.getClass(), new ItemEvent(ItemEvent.ITEM_CLICK, holder.getAdapterPosition(), data));
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (handler != null) {
                        handler.onEvent(item.getClass(), new ItemEvent(ItemEvent.ITEM_LONGCLICK, holder.getAdapterPosition(), data));
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return items.getItemType(position);
        }
    }
}
