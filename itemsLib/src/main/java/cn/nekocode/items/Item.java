/*
 * Copyright 2019. nekocode (nekocode.cn@gmail.com)
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
import android.view.ViewGroup;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public abstract class Item<D, V extends RecyclerView.ViewHolder, C> {
    private ItemAdapter mAdapter;
    private int mViewType;
    private C mCallback;

    public Item(ItemAdapter adapter, int viewType) {
        this.mAdapter = adapter;
        this.mViewType = viewType;
    }

    @NonNull
    public abstract V onCreateViewHolder(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent);

    public abstract void onBindViewHolder(
            @NonNull V holder,
            int position,
            @NonNull D data);

    @NonNull
    public ItemAdapter getAdapter() {
        return mAdapter;
    }

    public int getViewType() {
        return mViewType;
    }

    @Nullable
    protected C getCallback() {
        return mCallback;
    }

    public void setCallback(@Nullable C callback) {
        mCallback = callback;
    }
}
