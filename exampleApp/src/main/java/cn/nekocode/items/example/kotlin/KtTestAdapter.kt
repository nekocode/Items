package cn.nekocode.items.example.kotlin

import cn.nekocode.items.ItemAdapter
import cn.nekocode.items.annotation.Adapter
import cn.nekocode.items.annotation.ViewDelegate
import cn.nekocode.items.annotation.ViewSelector
import cn.nekocode.items.example.java.HeaderOrFooterData

@Adapter
abstract class KtTestAdapter : ItemAdapter() {
    val list = ArrayList<Any>()

    override fun <T> getData(position: Int) = list[position] as T

    override fun getItemCount() = list.size

    @ViewDelegate
    abstract fun headerView(): KtHeaderItemView.Delegate

    @ViewDelegate
    abstract fun stringView(): KtStringItemView.Delegate

    @ViewDelegate
    abstract fun footerView(): KtFooterItemView.Delegate

    @ViewSelector
    fun viewForHeaderOrFooter(position: Int, data: HeaderOrFooterData) = if (data.isHeader) {
        headerView().viewType()
    } else {
        footerView().viewType()
    }
}