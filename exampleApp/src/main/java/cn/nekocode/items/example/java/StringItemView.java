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

package cn.nekocode.items.example.java;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cn.nekocode.items.ItemView;
import cn.nekocode.items.ItemViewDelegate;
import cn.nekocode.items.annotation.ViewDelegateOf;
import cn.nekocode.items.example.R;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class StringItemView extends ItemView<String, StringItemView.Callback> {
    private TextView textView;
    private Button button;

    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        final View itemView = inflater.inflate(R.layout.item_string, parent, false);
        textView = itemView.findViewById(R.id.textView);
        button = itemView.findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (getCallback() != null) {
                getCallback().onButtonClick(getData());
            }
        });
        return itemView;
    }

    @Override
    public void onBindData(@NonNull String data) {
        textView.setText(data);
    }

    @ViewDelegateOf(StringItemView.class)
    public interface Delegate extends ItemViewDelegate<Callback> {
    }

    public interface Callback {
        void onButtonClick(@NonNull String data);
    }
}
