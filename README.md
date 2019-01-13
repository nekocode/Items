[![Build Status](https://travis-ci.com/nekocode/Items.svg?branch=master)](https://travis-ci.com/nekocode/Items) [![codecov](https://codecov.io/gh/nekocode/Items/branch/master/graph/badge.svg)](https://codecov.io/gh/nekocode/Items)

该 Annotation Processor 可以为 Android 的 `RecyclerView` 生成基于 **数据-视图-绑定** 的 `Adapter`。

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

使用 `BaseItem` 能够帮助你把 `ViewHolder` 的创建和绑定从 `Adapter` 中提取出来，并且与特定的数据类型绑定。例如你可以为 `String` 类型的数据创建一个 Item：

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

    @NonNull
    @ItemMethod
    public abstract StringItem stringItem();
}
```

Annotation Processor 会为以上 Adapter 创建一个实现类 `TestAdapterImpl`，你可以通过以下例子来使用该 Adapter：

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
