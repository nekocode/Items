package cn.nekocode.items.example.kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.nekocode.items.ItemView
import cn.nekocode.items.ItemViewDelegate
import cn.nekocode.items.annotation.ViewDelegateOf
import cn.nekocode.items.example.java.HeaderOrFooterData
import cn.nekocode.items.example.R
import kotlinx.android.synthetic.main.item_footer.view.*

class KtFooterItemView : ItemView<HeaderOrFooterData, KtFooterItemView.Callback>() {

    override fun onCreateItemView(inflater: LayoutInflater, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.item_footer, parent, false)
        itemView.checkBox.setOnCheckedChangeListener { _, b ->
            data.isChecked = b
            callback?.onCheckedChanged(data)
        }
        return itemView
    }

    override fun onBindData(data: HeaderOrFooterData) {
        val itemView = view ?: return
        itemView.textView.text = data.text
        itemView.checkBox.isChecked = data.isChecked
    }

    @ViewDelegateOf(KtFooterItemView::class)
    interface Delegate : ItemViewDelegate<Callback>

    open class Callback {
        open fun onCheckedChanged(data: HeaderOrFooterData) {
            // do nothing
        }
        open fun otherMethod() {
            // do nothing
        }
    }
}