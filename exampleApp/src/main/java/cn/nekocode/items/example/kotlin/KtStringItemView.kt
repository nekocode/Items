package cn.nekocode.items.example.kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.nekocode.items.ItemView
import cn.nekocode.items.ItemViewDelegate
import cn.nekocode.items.annotation.ViewDelegateOf
import cn.nekocode.items.example.R
import kotlinx.android.synthetic.main.item_string.view.*

class KtStringItemView : ItemView<String, KtStringItemView.Callback>() {

    override fun onCreateItemView(inflater: LayoutInflater, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.item_string, parent, false)
        itemView.button.setOnClickListener {
            callback?.onButtonClick(data)
        }
        return itemView
    }

    override fun onBindData(data: String) {
        val itemView = view ?: return
        itemView.textView.text = data
    }

    @ViewDelegateOf(KtStringItemView::class)
    interface Delegate : ItemViewDelegate<Callback>

    interface Callback {
        fun onButtonClick(data: String)
    }
}