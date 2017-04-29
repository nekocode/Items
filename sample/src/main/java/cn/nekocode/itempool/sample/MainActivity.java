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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        assert recyclerView != null;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final ItemPool itemPool = new ItemPool();
        itemPool.addType(HeaderItem.class);
        itemPool.addType(TextItem.class);

        itemPool.add(new Header());
        itemPool.add("A");
        itemPool.add("B");
        itemPool.add("C");
        itemPool.add("D");
        itemPool.add("E");
        itemPool.add("F");
        itemPool.add("G");

        itemPool.attachTo(recyclerView);

        itemPool.onEvent(TextItem.class, new ItemEventHandler() {
            @Override
            public void onEvent(@NonNull ItemEvent event) {
                switch (event.getAction()) {
                    case Item.EVENT_ITEM_CLICK:
                        Toast.makeText(MainActivity.this,
                                "You just clicked item:" + event.getData() + ".", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        itemPool.onEvent(HeaderItem.class, new ItemEventHandler() {
            @Override
            public void onEvent(@NonNull ItemEvent event) {
                switch (event.getAction()) {
                    case Item.EVENT_ITEM_CLICK:
                        Toast.makeText(MainActivity.this,
                                "You just clicked the header.", Toast.LENGTH_SHORT).show();
                        break;

                    case HeaderItem.EVENT_TEXT_CLICK:
                        Toast.makeText(MainActivity.this,
                                "You just clicked the TextView.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }
}
