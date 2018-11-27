# Items
[![Build Status](https://travis-ci.com/nekocode/Items.svg?branch=master)](https://travis-ci.com/nekocode/Items) [![codecov](https://codecov.io/gh/nekocode/Items/branch/master/graph/badge.svg)](https://codecov.io/gh/nekocode/Items)

A library to generate data-view-binding adapters of android recycler view.

## Integration

The `${lastest-version}` of this plugin is [![](https://jitpack.io/v/nekocode/Items.svg)](https://jitpack.io/#nekocode/Items). Copy below code to the build.gradle of your android project:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation "com.github.nekocode.Items:itemsLib:${lastest-version}"
    annotationProcessor "com.github.nekocode.Items:itemsProcessor:${lastest-version}"
}
```

## Usage

### 1. Define item view separately.

Take attention to the parameters of class's generic type. The first parameter is the type of data and the second is the type of this view's event callback (can be interface or class).

```java
public class StringItemView extends ItemView<String, StringItemView.Callback> {
    private TextView textView;
    private Button button;

    @NonNull
    @Override
    public View onCreateItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        final View itemView = inflater.inflate(R.layout.item_string, parent, false);
        textView = itemView.findViewById(R.id.textView);
        button = itemView.findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (getCallback() != null) {
                getCallback().onButtonClick(getData());
            }
        });
        return itemView;
    }

    @Override
    public void onBindData(@NonNull String data) {
        textView.setText(data);
    }

    @ViewDelegateOf(StringItemView.class)
    public interface Delegate extends ItemViewDelegate<Callback> {
    }

    public interface Callback {
        void onButtonClick(@NonNull String data);
    }
}
```

One important thing is that we need to define an empty interface extending `ItemViewDelegate` for every item view, such as the `Delegate` interface in above code. And it should be annotated with `@ViewDelegateOf` whose parameter should be the class of target item view.

The `ItemViewDelegate` is provided by this library. It's the bridge between adapter and item view, and it only has two methods:

```java
public interface ItemViewDelegate<C> {
    int viewType();
    void setCallback(@Nullable C callback);
}
```

The `viewType()` method returns a view type id (generated in *build-time*) of this item view. And you can use it to select item view type for one specified data type (see the chapter [「One to many data-view-binding」](#one-to-many-data-view-binding) for more details).

The `setCallback()` method can set an event callback for this item view. You can call `getCallback()` in the item view to get callback, and then invoke methods of the callback to tell caller that some view event are triggered.

### 2. Define adapter.

Now we need to define a `ItemsAdapter` to assemble the item views to `RecyclerView`. It must be abstract and annotated with `@Adapter`:

```java
@Adapter
public abstract class TestAdapter extends ItemAdapter {
    private final ArrayList list = new ArrayList();

    public ArrayList list() {
        return list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public <T> T getData(int position) {
        return (T) list.get(position);
    }

    @NonNull
    @ViewDelegate
    public abstract StringItemView.Delegate stringView();
}
```

Take attention that this adapter must override `getItemCount()` and `getData()` methods and has no constructors having parameters. The `getItemCount()` and `getData()` describe how many data (one data corresponds to one view) in the recycler view and which data is in the specified position.

And then for adding our item views to the recycler view, we need to define corresponding delegate methods in this adapter. These methods must return corresponding view delegate, be annotated with `@ViewDelegate`, and must be abstract and no-parameters. But you can name them anything you like.

When you build this project, the annotation processor of this library will generate an implementation of this abstract class.

### 3. Create and use adapter.

Now, you can create an instance of this adapter by using `Items.create()` method. Add data to it's data collection and set view event callback for view:

```java
// Create instance
TestAdapter adapter = Items.create(TestAdapter.class);

// Obtain data collection of this adapter, and put data into it
adapter.list().add("Item1");
adapter.list().add("Item2");

// Obtain view delegate of the StringItemView, and set view event callback for it
adapter.stringView().setCallback(data -> {
    // Do some reactions
});

// Setup recycler view
recyclerView.setAdapter(adapter);
```

## One to many data-view-binding

Above simple usage shows one to one data-view-binding of `ItemsAdapter`. In the same time it also supports one to many data-view-binding. You just need to define a view-type selector method for specified data-type in the adapter.

```java
@Adapter
public abstract class TestAdapter extends ItemAdapter {
    // ...

    @NonNull
    @ViewDelegate
    public abstract StringItemView.Delegate stringView();

    @NonNull
    @ViewDelegate
    public abstract StringItemView2.Delegate stringView2();
    
    @ViewSelector
    public int viewForString(int position, @NonNull String data) {
        if (!data.endsWith(2)) {
            return stringView().viewType();
        } else {
            return stringView2().viewType();
        }
    }
}
```

Take above code for example, there are two item views correspond to a same data type, so we need to define a selector method to tell the adapter which item view should be used for this data type in run-time.

The selector method must have two parameters, the first is the position of data in adapter's data collection, and the second is the data itself. The return value of the method is the type id of corresponding view. At last, the method must be annotated with `@ViewSelector`.
