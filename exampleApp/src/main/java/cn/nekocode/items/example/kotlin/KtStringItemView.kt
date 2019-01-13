package cn.nekocode.items.example.kotlin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.nekocode.items.BaseItem
import cn.nekocode.items.ItemAdapter
import cn.nekocode.items.example.R
import kotlinx.android.synthetic.main.item_string.view.*

class KtStringItemView(adapter: ItemAdapter, viewType: Int) :
    BaseItem<String, KtStringItemView.Holder, KtStringItemView.Callback>(adapter, viewType) {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        val itemView = inflater.inflate(R.layout.item_string, parent, false)
        val holder = Holder(itemView)
        itemView.button.setOnClickListener {
            callback?.onButtonClick(holder.data)
        }
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int, data: String) {
        holder.data = data
        holder.itemView.textView.text = data
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var data: String? = null
    }

    interface Callback {
        fun onButtonClick(data: String?)
    }
}