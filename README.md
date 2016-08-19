# ItemPool
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://img.shields.io/github/release/nekocode/ItemPool.svg?label=Jitpack)](https://jitpack.io/#nekocode/ItemPool)

Decouple the item(/nested viewholder) from recyclerview's adapter. No more need to write an adapter again.

![description](art/description.png)

### Using with gradle
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

### Usage

Make reusable items for recyclerview like the following:

```java
public class TestItem extends ItemPool.Item<String> {
    TextView textView;

    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View itemView = inflater.inflate(R.layout.item_test, parent, false);
        textView = (TextView) itemView.findViewById(R.id.textView);
        return itemView;
    }

    @Override
    public void onBindViewHolder(@NonNull String s, ItemEventHandler eventHandler) {
        textView.setText(s);
    }
}
```

No more need adpater and data list. You just need an `ItemPool`. Add itemtypes and data to the ItemPool. It will help the recyclerview automatically select the Item to show.

```java
ItemPool items = new ItemPool();
items.addType(TestItem.class);
items.addType(TestItem2.class);
items.onEvent(this);

items.add(new Header());
items.add("A");
items.add("B");

recyclerView.setAdapter(items.getAdapter());
```

