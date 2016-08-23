package cn.nekocode.itempool.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import cn.nekocode.itempool.Item;
import cn.nekocode.itempool.ItemEvent;
import cn.nekocode.itempool.ItemEventHandler;
import cn.nekocode.itempool.ItemPool;

public class MainActivity extends AppCompatActivity implements ItemEventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        assert recyclerView != null;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemPool items = new ItemPool();
        items.addType(TestItem.class);
        items.addType(TestItem2.class);
        items.onEvent(this);

        items.add(new Header());
        items.add("A");
        items.add("B");
        items.add("C");
        items.add("D");
        items.add("E");
        items.add("F");
        items.add("G");

        items.attachTo(recyclerView);
    }

    @Override
    public void onEvent(@NonNull Class<? extends Item> clazz, @NonNull ItemEvent event) {
        switch (event.action) {
            case ItemEvent.ITEM_CLICK:
                if (clazz.equals(TestItem.class)) {
                    Toast.makeText(MainActivity.this,
                            "You just clicked item:" + event.data + ".", Toast.LENGTH_SHORT).show();

                } else if (clazz.equals(TestItem2.class)) {
                    Toast.makeText(MainActivity.this,
                            "You just clicked the header.", Toast.LENGTH_SHORT).show();
                }
                break;

            case TestItem2.CLICK_TEXT:
                Toast.makeText(MainActivity.this,
                        "You just clicked the TextView.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
