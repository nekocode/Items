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

package cn.nekocode.items.example;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import cn.nekocode.items.ItemView;
import cn.nekocode.items.ItemViewSelector;

import java.util.HashMap;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class TestAdapter_ extends TestAdapter {
    private final HashMap<Class, Integer> viewTypes;
    private final HashMap<Class, ItemViewSelector> selectors;
    {
        // todo: the number 3
        viewTypes = new HashMap<>(3);
        selectors = new HashMap<>(3);

        // todo: check if there has duplicate keys
        viewTypes.put(TestData.class, 0);

        // Do not write as lambda
        selectors.put(TestData.class, new ItemViewSelector<TestData>() {
            @Override
            public int select(int position, @NonNull TestData data) {
                return viewTypeForTestData(position, data);
            }
        });
    }

    @NonNull
    @Override
    public TestItemView.Delegate testItemView() {
        return new TestItemView.Delegate() {
            @Override
            public int viewType() {
                return 0;
            }

            @Override
            public void setCallback(TestItemView.Callback callback) {
                callbacks.put(viewType(), callback);
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        final Object data = getData(position);
        final Integer viewType = viewTypes.get(data.getClass());
        if (viewType != null) {
            return viewType;
        } else {
            final ItemViewSelector selector = selectors.get(data.getClass());
            if (selector != null) {
                return selector.select(position, data);
            }
        }
        throw new RuntimeException("Unknown data type: " + data.getClass().getName());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final ItemView.Holder holder;
        switch (viewType) {
            case 0: {
                holder = new TestItemView().onCreateViewHolder(this, viewGroup, viewType);
                break;
            }
            default: {
                throw new RuntimeException("Unsupported view type.");
            }
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((ItemView.Holder) viewHolder).outer().setData(getData(position));
    }
}
