# ItemPool
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://jitpack.io/v/nekocode/ItemPool.svg)](https://jitpack.io/#nekocode/ItemPool)

Decouple the item(/nested viewholder) from recyclerview's adapter. 

Reuse itemview in every recyclerview.

![description](art/description.png)

## Setting up
- Add the JitPack repository to your project root build.gradle:
```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```

- Add the dependency to your app or lib build.gradle:
```gradle
dependencies {
    compile 'com.github.nekocode:ItemPool:{lastest-version}'
}
```

## Usage

Firstly, create a new `Item` class. It can help create the itemView and bind data to the view, just like the behavior of `ViewHolder`. It is pluggable and can be used for every `ItemPool`(`RecyclerView`). Please note that the generic type argument in class declaration define the binding data type of this item.

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

And then obtain an `ItemPool` instance. (You don't need to create adapter or data list. The `ItemPool` like a mixture of them.) The next step is to add item types (the class of item) and data to it. It will automatically select the matched item for the data element.

```java
ItemPool items = new ItemPool();
items.addType(TestItem.class);
items.addType(TestItem2.class);

items.add(new Header());
items.add("A");
items.add("B");

items.attachTo(recyclerView);
```

When data changes, you can call the `notify*` functions just like adapter's. Such as:

```java
items.notifyDataSetChanged();
```

If you want to handler the item's events such as itemView click / long click, or childView's event. You can setup an `ItemEventHandler` for the `ItemPool`:

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

However, it will auto trigger the `ItemEvent.ITEM_CLICK` and `ItemEvent.ITEM_LONGCLICK` events. But you should trigger childView's event manually. For example:

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

## Note that

It can help you reduce a lot of code but it lose the flexibility of the recyclerview's adapter. In some cases you still need to create an adapter.

By the way, you can also try the [AdapterDelegates](https://github.com/sockeqwe/AdapterDelegates) or [FastAdapter](https://github.com/mikepenz/FastAdapter).
