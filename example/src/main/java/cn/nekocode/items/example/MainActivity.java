package cn.nekocode.items.example;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.nekocode.items.data.ItemData;
import cn.nekocode.items.example.test.TestData;
import cn.nekocode.items.example.test.TestData2;
import cn.nekocode.items.example.test.TestItemView;
import cn.nekocode.items.example.test.TestItemViewB;
import cn.nekocode.items.example.test.TestItemsAdapter;
import cn.nekocode.items.view.ItemEvent;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        assert recyclerView != null;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final TestItemsAdapter adapter = new TestItemsAdapter();
        final ArrayList<ItemData> list = adapter.getDataCollection();
        list.add(adapter.TestData(new TestData()));
        list.add(adapter.TestData2(new TestData2()));
        final TestData2 data2 = new TestData2();
        data2.useA = false;
        list.add(adapter.TestData2(data2));

        adapter.addEventListener(new TestItemsAdapter.EventListener() {
            @Override
            public void onItemClick(int position, Object data) {
                Toast.makeText(MainActivity.this,
                        "Item" + String.valueOf(position) + " clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTestItemViewEvent(@NonNull ItemEvent<TestData> event) {
                if (event.getWhat() == TestItemView.EVENT_BUTTON_CLICK) {
                    Toast.makeText(MainActivity.this,
                            "TestItem's button clicked", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTestItemViewBEvent(@NonNull ItemEvent<TestData2> event) {
                if (event.getWhat() == TestItemViewB.EVENT_CHECKBOX_CHECKEDCHANGE) {
                    boolean checked = (Boolean) event.getExtra();
                    Toast.makeText(MainActivity.this,
                            "TestItemB's checkbox " + (checked ? "checked" : "unchecked"), Toast.LENGTH_SHORT).show();
                }
            }
        });


        recyclerView.setAdapter(adapter);
    }
}
