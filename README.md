# ItemPool
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://img.shields.io/github/release/nekocode/ItemPool.svg?label=Jitpack)](https://jitpack.io/#nekocode/ItemPool)

Decouple the item/itemview/item's viewholder from recyclerview's adapter. You can no longer make an adapter class.

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

Make reusable items for recyclerview like this:

```java
public class TestItem extends Item<String, TestItem.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.textView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent) {

        View itemView = inflater.inflate(R.layout.item_test, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            @NonNull String s,
            ItemEventHandler eventHandler) {

        holder.textView.setText(s);
    }
}
```

Just create an `ItemPool`. No more need adpater and data list. Setup itemtypes for matching data and then you can add start adding data to the ItemPool. It will automatically select the Item to show for the recyclerview.

```java
ItemPool items = new ItemPool();
items.addItemType(TestItem.class);
items.addItemType(TestItem2.class);
items.setEventHandler(this);

items.add(new Header());
items.add("A");
items.add("B");

recyclerView.setAdapter(items.getAdapter());
```

