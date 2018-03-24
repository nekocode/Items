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

import android.support.annotation.Nullable;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ItemEvent<T> {
    private int mWhat;
    private T mData;
    private Object mExtra;


    public ItemEvent(int what) {
        this(what, null, null);
    }

    public ItemEvent(int what, @Nullable T data) {
        this(what, data, null);
    }

    public ItemEvent(int what, @Nullable T data, @Nullable Object extra) {
        this.mWhat = what;
        this.mData = data;
        this.mExtra = extra;
    }

    public int getWhat() {
        return mWhat;
    }

    @Nullable
    public T getData() {
        return mData;
    }

    @Nullable
    public Object getExtra() {
        return mExtra;
    }
}
