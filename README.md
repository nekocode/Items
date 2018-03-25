This library uses [annotation processor](https://en.wikipedia.org/wiki/Java_annotation#Processing) to help you generate data-view-binding adapters for android list views (such as the RecyclerView).

## Why built it

In fact, it abstracts the adapter as a type matcher for data and item-view. It can display different item-views based on a list of multi-type data. There are already some libraries doing this kind of thing. Such as [MultiType](https://github.com/drakeet/MultiType), [ItemPool](https://github.com/nekocode/Items/tree/item-pool) (Ancestor of this library). However, they all have some deficiencies. Inefficient type matching, inconvenient event handling, and you can only use them on the RecyclerView (not expandable enough). So I use the annotation processor technology to redesign the ItemPool library, and finially built this new library.

## How to use

You can see the [example](example/src/main/java/cn/nekocode/items/example/test) to learn most of the usages.

Usually, we only need three steps. Firstly, we declare some ItemViews for represent the data. This library provides [RecyclerViewItemView](items/src/main/java/cn/nekocode/items/view/RecyclerViewItemView.java) for the RecyclerView. Override the `onCreateItemView` method to return a view at the time of item-view's creating. And override the `onBindData` method to refresh the view based on the new data binding to it.

```java
public class TestItemView extends RecyclerViewItemView<TestData> {
    private TextView mTextView;

    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        final View itemView = inflater.inflate(R.layout.item_test, parent, false);
        mTextView = itemView.findViewById(R.id.textView);
        return itemView;
    }

    @Override
    public void onBindData(@NonNull TestData data) {
        mTextView.setText(data.text);
    }
}

```

The second step is to create a new empty interface. Annotate it with `@Items` to configure the type of adapter, type of data collection, and the relationship of data-view-binding.

```java
@Items({
        @ItemBinding(
                data = @ItemBinding.Data(TestData.class),
                view = @ItemBinding.View(TestItemView.class)
        )
})
public interface TestItems {}
```

Now try to rebuild module in IDE or manually excute the below command in terminal.

```sh
./gradlew :example:clean :example:compileDebugJavaWithJavac
```

The processor will generate an adapter class into the same package of your empty interface. The name is `Your empty interface's name` + `Adapter`. For example, according to the above `TestItems`, the processor will name the generated adapter class `TestItemsAdapter`.

[Here](generated_adapter_example/TestItemsAdapter.java) is an example of the generated adapter class file.

## Advanced usages

Just like the MultiType library, this library can also bind one data to multiple views. It provides an interface named [`ItemViewSelector`](items/src/main/java/cn/nekocode/items/view/ItemViewSelector.java) to select different item-view to display. You can see the example [TestItemViewSelector](example/src/main/java/cn/nekocode/items/example/test/TestItemViewSelector.java). The code tell everything.

Event handling is very easy to use. In the ItemView, call `getEventHandler().sendEvent()`  ([example](example/src/main/java/cn/nekocode/items/example/test/TestItemViewB.java#L33)) to send events. And then add a listener to lisnten them by using the generated adapter's `addEventListener()` method ([example](example/src/main/java/cn/nekocode/items/example/MainActivity.java#L41-L64)).
