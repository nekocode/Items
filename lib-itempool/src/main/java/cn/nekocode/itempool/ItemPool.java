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

    private final HashMap<Class, ItemType> mapOfType = new HashMap<>();
    private final SparseArray<Class<? extends Item>> mapOfItemClass = new SparseArray<>();
    private final DecoupleAdapter internalAdapter = new DecoupleAdapter();

    public static abstract class Item<Data> {

        private class InternalViewHolder extends RecyclerView.ViewHolder {
            private final Item item;

            public InternalViewHolder(View itemView) {
                super(itemView);
                this.item = Item.this;
            }
        }

        private RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            View itemView = onCreateItemView(LayoutInflater.from(parent.getContext()), parent);
            return new InternalViewHolder(itemView);
        }

        @NonNull
        public abstract View onCreateItemView(
                @NonNull LayoutInflater inflater,
                @NonNull ViewGroup parent);

        public abstract void onBindViewHolder(
                @NonNull final Data data,
                ItemEventHandler eventHandler);
    }

    public void addType(@NonNull Class<? extends Item> itemClass) {
        ParameterizedType parameterizedType = (ParameterizedType) itemClass.getGenericSuperclass();
        Class dataClass = (Class) parameterizedType.getActualTypeArguments()[0];

        ItemType type = new ItemType(itemClass);
        mapOfType.put(dataClass, type);
        mapOfItemClass.put(type.TYPE_ID, itemClass);
    }

    public final void onEvent(ItemEventHandler handler) {
        Collection<ItemType> itemTypes = mapOfType.values();

        for (ItemType type : itemTypes) {
            type.handler = handler;
        }
    }

    @NonNull
    public RecyclerView.Adapter getAdapter() {
        return internalAdapter;
    }

    public void updateViews() {
        internalAdapter.notifyDataSetChanged();
    }


    /**
     * Private members
     */

    private int getItemType(int index) {
        Class dataClass = get(index).getClass();
        ItemType type = mapOfType.get(dataClass);
        if (type == null) {
            throw new RuntimeException("No item set for the data type: " + dataClass.getSimpleName());
        }
        return type.TYPE_ID;
    }

    private Class<? extends Item> getItemClass(int typeId) {
        return mapOfItemClass.get(typeId);
    }

    private Pair getItemClass(Class dataClass) {
        ItemType type = mapOfType.get(dataClass);
        return new Pair(type.itemClass, type.handler);
    }

    private static Item newItem(Class<? extends Item> itemClass) {
        Item item;
        try {
            item = itemClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return item;
    }

    private static class ItemType {
        private final int TYPE_ID;
        private final Class<? extends Item> itemClass;
        private ItemEventHandler handler;

        private ItemType(Class<? extends Item> itemClass) {
            TYPE_ID = ID_COUNTER.getAndIncrement();
            this.itemClass = itemClass;
        }
    }

    private class DecoupleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final ItemPool items;

        public DecoupleAdapter() {
            this.items = ItemPool.this;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Item item = newItem(items.getItemClass(viewType));
            return item.onCreateViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            final Object data = items.get(position);
            Pair<Class<? extends Item>, ItemEventHandler> pair = items.getItemClass(data.getClass());

            final Item item = ((Item.InternalViewHolder) holder).item;
            final ItemEventHandler handler = pair.second;
            item.onBindViewHolder(data, handler);

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
