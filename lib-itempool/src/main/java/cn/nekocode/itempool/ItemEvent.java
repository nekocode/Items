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

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ItemEvent {
    private int action;
    private Object data;
    private Item item;

    public ItemEvent(@NonNull Item item, int action, @Nullable Object data) {
        this.action = action;
        this.data = data;
        this.item = item;
    }

    @NonNull
    public Item getItem() {
        return item;
    }

    public int getAction() {
        return action;
    }

    @Nullable
    public Object getData() {
        return data;
    }
}
