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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nekocode on 16/8/23.
 */
public abstract class Item<Data> {

    protected class InternalViewHolder extends RecyclerView.ViewHolder {
        protected final Item item;

        public InternalViewHolder(View itemView) {
            super(itemView);
            this.item = Item.this;
        }
    }
    private InternalViewHolder internalViewHolder;

    @NonNull
    public abstract View onCreateItemView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent);

    public abstract void onBindItem(
            @NonNull final RecyclerView.ViewHolder holder,
            @NonNull final Data data,
            ItemEventHandler eventHandler);

    public ItemEvent event(int action, Object data) {
        return new ItemEvent(action, data, internalViewHolder);
    }

    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View itemView = onCreateItemView(LayoutInflater.from(parent.getContext()), parent);
        internalViewHolder = new InternalViewHolder(itemView);
        return internalViewHolder;
    }
}