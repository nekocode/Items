package cn.nekocode.items.example.kotlin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.nekocode.items.Item
import cn.nekocode.items.ItemAdapter
import cn.nekocode.items.example.R
import cn.nekocode.items.example.java.HeaderOrFooterData
import kotlinx.android.synthetic.main.item_header.view.*

class KtHeaderItemView(adapter: ItemAdapter, viewType: Int) :
    Item<HeaderOrFooterData, KtHeaderItemView.Holder, KtHeaderItemView.Callback>(adapter, viewType) {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        val itemView = inflater.inflate(R.layout.item_header, parent, false)
        return Holder(itemView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int, data: HeaderOrFooterData) {
        holder.itemView.textView.text = data.text
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Callback
}