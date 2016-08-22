package cn.nekocode.itempool.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.nekocode.itempool.ItemEventHandler;
import cn.nekocode.itempool.ItemPool;

/**
 * Created by nekocode on 16/8/17.
 */
public class TestItem extends ItemPool.Item<String> {
    TextView textView;

    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View itemView = inflater.inflate(R.layout.item_test, parent, false);
        textView = (TextView) itemView.findViewById(R.id.textView);
        return itemView;
    }

    @Override
    public void onBindItem(@NonNull final RecyclerView.ViewHolder holder, @NonNull String s, ItemEventHandler eventHandler) {
        textView.setText(s);
    }
}