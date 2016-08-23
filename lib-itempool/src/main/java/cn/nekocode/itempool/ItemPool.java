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
    private final ItemAdapter internalAdapter = new ItemAdapter(this);

    public void addType(@NonNull Class<? extends Item> itemClass) {
        ParameterizedType parameterizedType = (ParameterizedType) itemClass.getGenericSuperclass();
        Class dataClass = (Class) parameterizedType.getActualTypeArguments()[0];

        ItemType type = new ItemType(itemClass);
        mapOfType.put(dataClass, type);
        mapOfItemClass.put(type.TYPE_ID, itemClass);
    }

    public void onEvent(ItemEventHandler handler) {
        Collection<ItemType> itemTypes = mapOfType.values();

        for (ItemType type : itemTypes) {
            type.handler = handler;
        }
    }

    public void attachTo(RecyclerView recyclerView) {
        recyclerView.setAdapter(internalAdapter);
    }


    /**
     * Transfer to the internal adapter
     */

    public void notifyDataSetChanged() {
        internalAdapter.notifyDataSetChanged();
    }

    public void notifyItemChanged(int position) {
        internalAdapter.notifyItemRangeChanged(position, 1);
    }

    public void notifyItemRangeChanged(int positionStart, int itemCount) {
        internalAdapter.notifyItemRangeChanged(positionStart, itemCount);
    }

    public void notifyItemInserted(int position) {
        internalAdapter.notifyItemRangeInserted(position, 1);
    }

    public void notifyItemMoved(int fromPosition, int toPosition) {
        internalAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    public void notifyItemRangeInserted(int positionStart, int itemCount) {
        internalAdapter.notifyItemRangeInserted(positionStart, itemCount);
    }

    public void notifyItemRemoved(int position) {
        internalAdapter.notifyItemRangeRemoved(position, 1);
    }

    public void notifyItemRangeRemoved(int positionStart, int itemCount) {
        internalAdapter.notifyItemRangeRemoved(positionStart, itemCount);
    }


    /**
     * Protected and private members
     */

    protected int getItemType(int index) {
        Class dataClass = get(index).getClass();
        ItemType type = mapOfType.get(dataClass);
        if (type == null) {
            throw new RuntimeException("No item set for the data type: " + dataClass.getSimpleName());
        }
        return type.TYPE_ID;
    }

    protected Class<? extends Item> getItemClass(int typeId) {
        return mapOfItemClass.get(typeId);
    }

    protected Pair getItemClass(Class dataClass) {
        ItemType type = mapOfType.get(dataClass);
        return new Pair(type.itemClass, type.handler);
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
}
