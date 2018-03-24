package cn.nekocode.items.example.test;

import android.support.annotation.NonNull;

import cn.nekocode.items.annotation.ItemViewId;
import cn.nekocode.items.view.ItemViewSelector;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public abstract class TestItemViewSelector implements ItemViewSelector<TestData2> {

    @ItemViewId(TestItemViewA.class)
    abstract int TestItemViewA();

    @ItemViewId(TestItemViewB.class)
    abstract int TestItemViewB();

    @Override
    public int select(@NonNull TestData2 data) {
        return data.useA ?
                TestItemViewA() : TestItemViewB();
    }
}
