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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import cn.nekocode.items.Item;
import cn.nekocode.items.ItemAdapter;
import cn.nekocode.items.example.R;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class FooterItem extends Item<HeaderOrFooterData, FooterItem.Holder, FooterItem.Callback> {

    public FooterItem(ItemAdapter adapter, int viewType) {
        super(adapter, viewType);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        final View itemView = inflater.inflate(R.layout.item_footer, parent, false);
        final Holder holder = new Holder(itemView);
        holder.textView = itemView.findViewById(R.id.textView);
        holder.checkBox = itemView.findViewById(R.id.checkBox);
        holder.checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            holder.data.setChecked(b);
            if (getCallback() != null) {
                getCallback().onCheckedChanged(holder.data);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position, @NonNull HeaderOrFooterData data) {
        holder.data = data;
        holder.textView.setText(data.getText());
        holder.checkBox.setChecked(data.isChecked());
    }


    static class Holder extends RecyclerView.ViewHolder {
        private TextView textView;
        private CheckBox checkBox;
        private HeaderOrFooterData data;

        Holder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    public static class Callback {
        public void onCheckedChanged(HeaderOrFooterData data) {
            // do nothing
        }
        public void otherMethod() {
            // do nothing
        }
    }
}
