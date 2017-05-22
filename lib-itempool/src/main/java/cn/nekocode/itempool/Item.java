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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public abstract class Item<T> {
    public static final int EVENT_ITEM_CLICK = -1201;

    private ViewHolder holder;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;
    private ItemEventHandler eventHandler;
    private T data;


    RecyclerView.ViewHolder onCreateViewHolder(
            RecyclerView.Adapter<RecyclerView.ViewHolder> adapter,
            final ItemEventHandler handler,
            ViewGroup parent) {

        this.adapter = adapter;
        this.eventHandler = handler;
        holder = new ViewHolder(onCreateItemView(LayoutInflater.from(parent.getContext()), parent));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event(EVENT_ITEM_CLICK, getData());
            }
        });

        return holder;
    }

    void _onBindData(T data) {
        this.data = data;
        onBindData(data);
    }

    @NonNull
    public abstract View onCreateItemView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent);

    public abstract void onBindData(@NonNull final T data);

    @NonNull
    public RecyclerView.ViewHolder getViewHolder() {
        return holder;
    }

    @NonNull
    public RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return adapter;
    }

    @NonNull
    public T getData() {
        return data;
    }

    public void event(int action, @Nullable Object data) {
        if (eventHandler != null) {
            eventHandler.onEvent(new ItemEvent(this, action, data));
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final Item item;

        ViewHolder(View itemView) {
            super(itemView);
            this.item = Item.this;
        }
    }
}