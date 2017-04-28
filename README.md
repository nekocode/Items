# ItemPool
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://jitpack.io/v/nekocode/ItemPool.svg)](https://jitpack.io/#nekocode/ItemPool)

Decouple the item(/nested viewholder) from recyclerview's adapter. 

Reuse itemview in every recyclerview.

![description](art/description.png)

## Install

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.nekocode:ItemPool:{lastest-version}'
}
```

## Usage

Firstly, create a new `Item` (It's a bit similar to the `ViewHolder`). Override the `onCreateItemView()` method to create the view for this item. And override the `onBindItem()` method for binding the corresponding type (the generic type of the class) of data to the item view.

```java
public class TestItem extends Item<String> {
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
```

And then obtain an `ItemPool` instance. It extends the `ArrayList<Object>` so you can add any data object to it. And for telling the itempool which `Item` will show in the recyclerview, you need to add classes of items.

```java
ItemPool items = new ItemPool();
items.addType(TestItem.class);
items.addType(TestItem2.class);

items.add(new ItemData());
items.add("A");
items.add("B");
```

Attach this itempool to the target recyclerview.

```java
items.attachTo(recyclerView);
```

It just like a mixture of data list and adapter because it also has the `notifyXXX()` methods for refreshing the recyclerview.

```java
items.notifyDataSetChanged();
```

**That's all! You don't need create `Adapter` any more! And every `Item` you create can be reused in any new recyclerview!**

### Handle view event

If you want to handle the item's view events. You can set an `ItemEventHandler` for the itempool:

```java
items.onEvent(new ItemEventHandler() {
    @Override
    public void onEvent(@NonNull Class<? extends Item> clazz, @NonNull ItemEvent event) {
        if (clazz.equals(TestItem.class)) {
            switch (event.action) {
                case ItemEvent.ITEM_CLICK:
                    // Handler the event
                    break;
            }

        } else if (clazz.equals(TestItem2.class)) {
            switch (event.action) {
                case ItemEvent.ITEM_CLICK:
                    // Handler the event
                    break;

                case TestItem2.CLICK_TEXT:
                    // Handler the event
                    break;
            }
    }
});
```

It will auto trigger the `ItemEvent.ITEM_CLICK` and `ItemEvent.ITEM_LONGCLICK` events internally. But you need to manually trigger other view events that you want to handle. For example:

```java
public class TestItem2 extends Item<Header> {
    public static final int CLICK_TEXT = 1;
    TextView textView;

    // ...

    @Override
    public void onBindItem(@NonNull final RecyclerView.ViewHolder holder, @NonNull Header header, final ItemEventHandler eventHandler) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventHandler.onEvent(TestItem2.class, event(CLICK_TEXT, null));
            }
        });
    }
}
```
