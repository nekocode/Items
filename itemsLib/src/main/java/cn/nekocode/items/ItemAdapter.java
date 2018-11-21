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
import android.util.SparseArray;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public abstract class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected final SparseArray callbacks = new SparseArray();

    /**
     * Get data in specified position
     */
    @NonNull
    public abstract <T> T getData(int position);

    @Nullable
    final <C> C getCallback(int viewType) {
        return (C) callbacks.get(viewType);
    }
}
