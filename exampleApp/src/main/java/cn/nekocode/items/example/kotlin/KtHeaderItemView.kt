package cn.nekocode.items.example.kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.nekocode.items.ItemView
import cn.nekocode.items.ItemViewDelegate
import cn.nekocode.items.annotation.ViewDelegateOf
import cn.nekocode.items.example.java.HeaderOrFooterData
import cn.nekocode.items.example.R
import kotlinx.android.synthetic.main.item_header.view.*

class KtHeaderItemView : ItemView<HeaderOrFooterData, KtHeaderItemView.Callback>() {

    override fun onCreateItemView(inflater: LayoutInflater, parent: ViewGroup): View {
        return inflater.inflate(R.layout.item_header, parent, false)
    }

    override fun onBindData(data: HeaderOrFooterData) {
        val itemView = view ?: return
        itemView.textView.text = data.text
    }

    @ViewDelegateOf(KtHeaderItemView::class)
    interface Delegate : ItemViewDelegate<Callback>

    interface Callback {
    }
}