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

package cn.nekocode.items;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public abstract class ItemView<T, C> {
    private ItemAdapter adapter;
    private int viewType;
    private Holder holder;
    private T data;

    public Holder onCreateViewHolder(ItemAdapter adapter, ViewGroup parent, int viewType) {
        this.viewType = viewType;
        this.adapter = adapter;
        this.holder = new Holder(
                onCreateItemView(LayoutInflater.from(parent.getContext()), parent));

        return holder;
    }

    @NonNull
    public abstract View onCreateItemView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent);

    public abstract void onBindData(
            @NonNull T data);

    public void setData(T data) {
        this.data = data;
        onBindData(data);
    }

    @NonNull
    public ItemAdapter getAdapter() {
        return adapter;
    }

    @NonNull
    public T getData() {
        return data;
    }

    @NonNull
    public RecyclerView.ViewHolder getViewHolder() {
        return holder;
    }

    @Nullable
    protected C getCallback() {
        return getAdapter().getCallback(viewType);
    }

    public final class Holder extends RecyclerView.ViewHolder {
        Holder(View itemView) {
            super(itemView);
        }

        public ItemView outer() {
            return ItemView.this;
        }
    }
}
