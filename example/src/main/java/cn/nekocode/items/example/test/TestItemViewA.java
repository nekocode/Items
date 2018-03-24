package cn.nekocode.items.example.test;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.nekocode.items.example.R;
import cn.nekocode.items.view.RecyclerViewItemView;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class TestItemViewA extends RecyclerViewItemView<TestData2> {
    private TextView mTextView;


    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        final View itemView = inflater.inflate(R.layout.item_test2, parent, false);
        mTextView = itemView.findViewById(R.id.textView);
        return itemView;
    }

    @Override
    public void onBindData(@NonNull TestData2 data) {
        mTextView.setText(data.text);
    }
}
