package cn.nekocode.items.example.test;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import cn.nekocode.items.example.R;
import cn.nekocode.items.view.ItemEvent;
import cn.nekocode.items.view.RecyclerViewItemView;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class TestItemViewB extends RecyclerViewItemView<TestData2> {
    public final static int EVENT_CHECKBOX_CHECKEDCHANGE = 1;
    private TextView mTextView;
    private CheckBox mCheckBox;


    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        final View itemView = inflater.inflate(R.layout.item_test3, parent, false);
        mTextView = itemView.findViewById(R.id.textView);
        mCheckBox = itemView.findViewById(R.id.checkBox);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getEventHandler().sendEvent(new ItemEvent<>(EVENT_CHECKBOX_CHECKEDCHANGE, getData(), isChecked));
            }
        });
        return itemView;
    }

    @Override
    public void onBindData(@NonNull TestData2 data) {
        mTextView.setText(data.text);
    }
}
