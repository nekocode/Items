package cn.nekocode.items.example.test;

import cn.nekocode.items.annotation.ItemBinding;
import cn.nekocode.items.annotation.Items;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
@Items(
        value = {
                @ItemBinding(
                        data = @ItemBinding.Data(TestData.class),
                        view = @ItemBinding.View(TestItemView.class)
                ),
                @ItemBinding(
                        data = @ItemBinding.Data(TestData2.class),
                        view = @ItemBinding.View(selector = TestItemViewSelector.class)
                ),
        }
)
public interface TestItems {

}
