/*
 * Copyright 2018 nekocode (nekocode.cn@gmail.com)
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

package cn.nekocode.items.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public abstract class RecyclerViewItemView<T> implements ItemView<T> {
    private InnerViewHolder mHolder;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;
    private T mData;
    private ItemEventHandler<T> mEventHandler;


    public InnerViewHolder onCreateViewHolder(
            RecyclerView.Adapter<RecyclerView.ViewHolder> adapter,
            final ItemEventHandler<T> handler,
            ViewGroup parent) {

        this.mAdapter = adapter;
        this.mEventHandler = handler;
        this.mHolder = new InnerViewHolder(onCreateItemView(LayoutInflater.from(parent.getContext()), parent));

        return mHolder;
    }

    public void _onBindData(T data) {
        this.mData = data;
        onBindData(data);
    }

    @NonNull
    public RecyclerView.ViewHolder getViewHolder() {
        return mHolder;
    }

    @NonNull
    public RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return mAdapter;
    }

    @NonNull
    public T getData() {
        return mData;
    }

    @NonNull
    @Override
    public ItemEventHandler<T> getEventHandler() {
        return mEventHandler;
    }


    public final class InnerViewHolder extends RecyclerView.ViewHolder {
        InnerViewHolder(View itemView) {
            super(itemView);
        }

        public RecyclerViewItemView outter() {
            return RecyclerViewItemView.this;
        }
    }
}