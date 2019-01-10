package cn.nekocode.items.example.kotlin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.nekocode.items.Item
import cn.nekocode.items.ItemAdapter
import cn.nekocode.items.example.R
import cn.nekocode.items.example.java.HeaderOrFooterData
import kotlinx.android.synthetic.main.item_footer.view.*

class KtFooterItemView(adapter: ItemAdapter, viewType: Int) :
    Item<HeaderOrFooterData, KtFooterItemView.Holder, KtFooterItemView.Callback>(adapter, viewType) {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        val itemView = inflater.inflate(R.layout.item_footer, parent, false)
        val holder = Holder(itemView)
        itemView.checkBox.setOnCheckedChangeListener { _, b ->
            holder.data?.also {
                it.isChecked = b
                callback?.onCheckedChanged(it)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int, data: HeaderOrFooterData) {
        holder.data = data
        holder.itemView.textView.text = data.text
        holder.itemView.checkBox.isChecked = data.isChecked
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var data: HeaderOrFooterData? = null
    }

    open class Callback {
        open fun onCheckedChanged(data: HeaderOrFooterData) {
            // do nothing
        }

        open fun otherMethod() {
            // do nothing
        }
    }
}