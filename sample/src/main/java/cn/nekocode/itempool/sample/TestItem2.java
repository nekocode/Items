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
public class TestItem2 extends Item<Header> {
    public static final int CLICK_TEXT = 4;
    TextView textView;

    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View itemView = inflater.inflate(R.layout.item_test2, parent, false);
        textView = (TextView) itemView.findViewById(R.id.textView);
        return itemView;
    }

    @Override
    public void onBindItem(@NonNull final RecyclerView.ViewHolder holder, @NonNull Header header, final ItemEventHandler eventHandler) {
        textView.setText("HEADER");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventHandler.onEvent(TestItem2.class, event(CLICK_TEXT, null));
            }
        });
    }
}