package cn.nekocode.itempool.sample;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.nekocode.itempool.Item;

public class HeaderItem extends Item<Header> implements View.OnClickListener {
    public static final int EVENT_TEXT_CLICK = 1;

    private TextView textView;


    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        final View itemView = inflater.inflate(R.layout.item_test2, parent, false);
        textView = (TextView) itemView.findViewById(R.id.textView);
        textView.setOnClickListener(this);
        return itemView;
    }

    @Override
    public void onBindData(@NonNull Header header) {
        textView.setText("HEADER");
    }

    @Override
    public void onClick(View v) {
        if (v == textView) {
            event(EVENT_TEXT_CLICK, null);
        }
    }
}