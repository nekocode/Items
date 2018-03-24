package cn.nekocode.items.example.test;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cn.nekocode.items.example.R;
import cn.nekocode.items.view.ItemEvent;
import cn.nekocode.items.view.RecyclerViewItemView;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class TestItemView extends RecyclerViewItemView<TestData> {
    public final static int EVENT_BUTTON_CLICK = 1;
    private TextView mTextView;
    private Button mButton;


    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        final View itemView = inflater.inflate(R.layout.item_test, parent, false);
        mTextView = itemView.findViewById(R.id.textView);
        mButton = itemView.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEventHandler().sendEvent(new ItemEvent<>(EVENT_BUTTON_CLICK, getData(), null));
            }
        });
        return itemView;
    }

    @Override
    public void onBindData(@NonNull TestData data) {
        mTextView.setText(data.text);
    }
}
