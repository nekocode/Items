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
import cn.nekocode.items.ItemAdapter;
import cn.nekocode.items.annotation.Adapter;
import cn.nekocode.items.annotation.ViewDelegate;
import cn.nekocode.items.annotation.ViewSelector;

import java.util.ArrayList;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
@Adapter
public abstract class TestAdapter extends ItemAdapter {
    private final ArrayList list = new ArrayList();

    public ArrayList list() {
        return list;
    }

    @NonNull
    @Override
    public <T> T getData(int position) {
        return (T) list.get(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @ViewDelegate
    public abstract HeaderItemView.Delegate headerView();

    @NonNull
    @ViewDelegate
    public abstract StringItemView.Delegate stringView();

    @NonNull
    @ViewDelegate
    public abstract FooterItemView.Delegate footerView();

    @ViewSelector
    public int viewForHeaderOrFooter(int position, @NonNull HeaderOrFooterData data) {
        if (data.isHeader()) {
            return headerView().viewType();
        } else {
            return footerView().viewType();
        }
    }
}