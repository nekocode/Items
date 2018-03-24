package cn.nekocode.items.example.test;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import cn.nekocode.items.data.ItemData;
import cn.nekocode.items.data.ItemDataArrayList;
import cn.nekocode.items.view.ItemEvent;
import cn.nekocode.items.view.ItemEventHandler;
import cn.nekocode.items.view.RecyclerViewItemView;
import java.lang.Object;
import java.lang.Override;
import java.util.ArrayList;

public final class TestItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ItemEventHandler<TestData> mTestItemViewEventHandler = new ItemEventHandler<TestData>() {
        @Override
        public void sendEvent(@NonNull ItemEvent<TestData> event) {
            for (EventListener listener : mEventListeners) {
                listener.onTestItemViewEvent(event);
            }
        }
    };

    private final ItemEventHandler<TestData2> mTestItemViewAEventHandler = new ItemEventHandler<TestData2>() {
        @Override
        public void sendEvent(@NonNull ItemEvent<TestData2> event) {
            for (EventListener listener : mEventListeners) {
                listener.onTestItemViewAEvent(event);
            }
        }
    };

    private final ItemEventHandler<TestData2> mTestItemViewBEventHandler = new ItemEventHandler<TestData2>() {
        @Override
        public void sendEvent(@NonNull ItemEvent<TestData2> event) {
            for (EventListener listener : mEventListeners) {
                listener.onTestItemViewBEvent(event);
            }
        }
    };

    private final ArrayList<EventListener> mEventListeners = new ArrayList<>();

    private final TestItemViewSelector mTestItemViewSelector = new TestItemViewSelector() {
        @Override
        public int TestItemViewA() {
            return Id.View.TestItemViewA;
        }

        @Override
        public int TestItemViewB() {
            return Id.View.TestItemViewB;
        }
    };

    private final ItemDataArrayList mDataCollection = new ItemDataArrayList();

    public void addEventListener(@Nullable EventListener listener) {
        if (listener != null) {
            mEventListeners.add(listener);;
        }
    }

    public void removeEventListener(@Nullable EventListener listener) {
        if (listener != null) {
            mEventListeners.remove(listener);;
        }
    }

    public TestItemViewSelector getTestItemViewSelector() {
        return mTestItemViewSelector;
    }

    public @NonNull ArrayList<ItemData> getDataCollection() {
        return mDataCollection.getCollection();
    }

    public @NonNull ItemData<TestData> TestData(@NonNull TestData data) {
        return new ItemData<>(data, Id.Data.TestData);
    }

    public @NonNull ItemData<TestData2> TestData2(@NonNull TestData2 data) {
        return new ItemData<>(data, Id.Data.TestData2);
    }

    @Override
    public int getItemCount() {
        return mDataCollection.getSize();
    }

    @Override
    public int getItemViewType(int position) {
        final ItemData data = mDataCollection.getData(position);
        switch (data.getDataType()) {
            case Id.Data.TestData: {
                return Id.View.TestItemView;
            }
            case Id.Data.TestData2: {
                return mTestItemViewSelector.select((TestData2) data.getData());
            }
        }
        throw new RuntimeException("Unregistered data type.");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ItemData data = mDataCollection.getData(position);
        final RecyclerViewItemView view = ((RecyclerViewItemView.InnerViewHolder) holder).outter();
        view._onBindData(data.getData());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final RecyclerViewItemView.InnerViewHolder holder;
        switch (viewType) {
            case Id.View.TestItemView: {
                holder = new TestItemView().onCreateViewHolder(this, mTestItemViewEventHandler, parent);
                break;
            }
            case Id.View.TestItemViewA: {
                holder = new TestItemViewA().onCreateViewHolder(this, mTestItemViewAEventHandler, parent);
                break;
            }
            case Id.View.TestItemViewB: {
                holder = new TestItemViewB().onCreateViewHolder(this, mTestItemViewBEventHandler, parent);
                break;
            }
            default: {
                throw new RuntimeException("Unsupported view type.");
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (EventListener listener : mEventListeners) {
                    listener.onItemClick(holder.getAdapterPosition(), holder.outter().getData());
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                boolean consumed = false;
                for (EventListener listener : mEventListeners) {
                    consumed |= listener.onItemLongClick(holder.getAdapterPosition(), holder.outter().getData());
                }
                return consumed;
            }
        });return holder;
    }

    private static final class Id {
        static final class Data {
            static final int TestData = 1;

            static final int TestData2 = 2;
        }

        static final class View {
            static final int TestItemView = 1;

            static final int TestItemViewA = 2;

            static final int TestItemViewB = 3;
        }
    }

    public static class EventListener {
        public void onItemClick(int position, Object data) {
        }

        public boolean onItemLongClick(int position, Object data) {
            return false;
        }

        public void onTestItemViewEvent(@NonNull ItemEvent<TestData> event) {
        }

        public void onTestItemViewAEvent(@NonNull ItemEvent<TestData2> event) {
        }

        public void onTestItemViewBEvent(@NonNull ItemEvent<TestData2> event) {
        }
    }
}
