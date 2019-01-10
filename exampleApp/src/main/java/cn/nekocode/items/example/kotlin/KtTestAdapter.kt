package cn.nekocode.items.example.kotlin

import cn.nekocode.items.ItemAdapter
import cn.nekocode.items.annotation.AdapterClass
import cn.nekocode.items.annotation.ItemMethod
import cn.nekocode.items.annotation.SelectorMethod
import cn.nekocode.items.example.java.HeaderOrFooterData

@AdapterClass
abstract class KtTestAdapter : ItemAdapter() {
    val list = ArrayList<Any>()

    override fun <T> getData(position: Int) = list[position] as T

    override fun getItemCount() = list.size

    @ItemMethod
    abstract fun headerItem(): KtHeaderItemView

    @ItemMethod
    abstract fun stringItem(): KtStringItemView

    @ItemMethod
    abstract fun footerItem(): KtFooterItemView

    @SelectorMethod
    fun viewForHeaderOrFooter(position: Int, data: HeaderOrFooterData) = if (data.isHeader) {
        headerItem().viewType
    } else {
        footerItem().viewType
    }
}