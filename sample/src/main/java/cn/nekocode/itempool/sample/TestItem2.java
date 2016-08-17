package cn.nekocode.itempool.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.nekocode.itempool.Item;
import cn.nekocode.itempool.ItemEventHandler;

/**
 * Created by nekocode on 16/8/17.
 */
public class TestItem2 extends Item<Header, TestItem2.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.textView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent) {

        View itemView = inflater.inflate(R.layout.item_test2, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            @NonNull Header header,
            ItemEventHandler eventHandler) {
    }
}