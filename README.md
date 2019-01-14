# Items

[![Build Status](https://travis-ci.com/nekocode/Items.svg?branch=master)](https://travis-ci.com/nekocode/Items) [![codecov](https://codecov.io/gh/nekocode/Items/branch/master/graph/badge.svg)](https://codecov.io/gh/nekocode/Items)

这个库可以为 Android 的 `RecyclerView` 生成基于 **Data-View-Binding** 的 Adapter。

对比其他一些类似的开源库，它有以下的一些优势：
* 更好的拓展性。这个库不需要你继承特定的 Adapter 或 ViewHolder 类，你可以继承任何第三方提供的基类；
* 更好的性能。使用 Annotation Processor 意味着实现 Binding 时无需使用反射；
* 更低的侵入性。和传统的 Adapter 写法类似，可以快速从旧的 Codebase 迁移到新的写法；
* 更可靠的代码。提供了单元测试覆盖大部分的 Case。

## 集入

替换以下代码中的 `${lastest-version}` 为最新版本号 [![](https://jitpack.io/v/nekocode/Items.svg)](https://jitpack.io/#nekocode/Items)，并复制到 Android 工程中的 build.gradle 脚本:

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
dependencies {
    implementation "com.github.nekocode.Items:itemsLib:${lastest-version}"
    annotationProcessor "com.github.nekocode.Items:itemsProcessor:${lastest-version}"
}
```

注意，在 Kotlin 工程中，需要使用 `kapt` 关键字代替 `annotationProcessor` 关键字。

## 使用

使用 `BaseItem` 能够帮助你把 `ViewHolder` 的创建和绑定从 Adapter 中提取出来，并且与特定的数据类型绑定。例如你可以为 `String` 类型的数据创建一个 Item：

```java
public class StringItem extends BaseItem<String, StringItem.Holder, StringItem.Callback> {

    public StringItem(ItemAdapter adapter, int viewType) {
        super(adapter, viewType);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        final View itemView = inflater.inflate(R.layout.item_string, parent, false);
        final Holder holder = new Holder(itemView);
        holder.button.setOnClickListener(v -> {
            if (getCallback() != null) {
                getCallback().onButtonClick(holder.data);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position, @NonNull String data) {
        holder.data = data;
        holder.textView.setText(data);
    }

    static class Holder extends RecyclerView.ViewHolder {
        private TextView textView;
        private Button button;
        private String data;

        Holder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            button = itemView.findViewById(R.id.button);
        }
    }

    public interface Callback {
        void onButtonClick(@NonNull String data);
    }
}
```

Item 提供了很好的拓展能力：
* 可以使用任意类型的 `ViewHolder`；
* 可以通过 `Callback` 为 `ViewHolder` 设置 UI 事件回调。

接下来你需要创建一个 Adapter 来装载你的所有 Item：

```java
@AdapterClass
public abstract class TestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemAdapter {
    private final LinkedList mList = new LinkedList();

    @NonNull
    public LinkedList list() {
        return mList;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @NonNull
    @Override
    public <T> T getData(int position) {
        return (T) mList.get(position);
    }

    /**
     * 定义一个任意名称的方法来返回你想装载的 Item
     */
    @NonNull
    @ItemMethod
    public abstract StringItem stringItem();
}
```

这个 Adapter 必须实现 `ItemAdapter` 接口的 `getItemCount()` 和 `getData()` 方法。你可以使用任意类型的 Collection（例如上面的 `LinkedList`）来装载你的数据，这个 Adapter 会通过这两个方法来访问你的数据，然后根据数据的类型来选择对应的 Item 来创建 `ViewHolder`。

在编译期间，Annotation Processor 会为这个 Adapter 生成一个实现类 `TestAdapterImpl`，你可以通过以下例子来使用这个 Adapter：

```java
// 创建 Adapter 实例
TestAdapter adapter = new TestAdapterImpl();

// 给 Adapter 插入数据
adapter.list().add("Item1");
adapter.list().add("Item2");

// 给 Item 设置 Callback
adapter.stringItem().setCallback(data -> {
    // Button 点击时
});

// 为 RecyclerView 设置 Adapter
recyclerView.setAdapter(adapter);
```

以上就是这个工具的基础使用。

此外，你还可以让你的单个数据类型绑定多个 Item：

```java
@Adapter
public abstract class TestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemAdapter {
    // ...

    @NonNull
    @ItemMethod
    public abstract StringItem stringItem();

    @NonNull
    @ItemMethod
    public abstract StringItem2 stringItem2();

    /**
     * 定义一个任意名称的方法来帮助 Adapter 选择绑定了同一数据类型的 Item
     */
    @SelectorMethod
    public int itemForString(int position, @NonNull String data) {
        if (!data.endsWith(2)) {
            return stringItem().getViewType();
        } else {
            return stringItem2().getViewType();
        }
    }
}
```

最后，还有一个小的 Tip。你可以定义一些 BaseAdapter 来简化你 Adapter 的代码，例如把对集合的操作封装起来，举个例子：

```java
public abstract class BaseArrayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemAdapter {
    private final ArrayList mList = new ArrayList();

    public ArrayList getList() {
        return mList;
    }

    @NonNull
    @Override
    public <T> T getData(int position) {
        return (T) mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}

@Adapter
public abstract class TestAdapter extends BaseArrayListAdapter {
    // ...
}
```

更详细的应用可以参考这个仓库中的 [exampleApp](exampleApp) 模块。
