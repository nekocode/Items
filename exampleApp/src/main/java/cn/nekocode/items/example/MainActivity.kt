package cn.nekocode.items.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import cn.nekocode.items.Items
import cn.nekocode.items.example.java.HeaderOrFooterData
import cn.nekocode.items.example.kotlin.KtFooterItemView
import cn.nekocode.items.example.kotlin.KtStringItemView
import cn.nekocode.items.example.kotlin.KtTestAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = Items.create(KtTestAdapter::class.java)

        // Add data
        adapter.list.add(HeaderOrFooterData("Header"))
        for (i in 0..10) {
            adapter.list.add("Item$i")
        }
        adapter.list.add(HeaderOrFooterData("Footer", false))

        // Setup callback for views
        adapter.stringView().setCallback(object : KtStringItemView.Callback {
            override fun onButtonClick(data: String) {
                Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
            }
        })
        adapter.footerView().setCallback(object : KtFooterItemView.Callback {
            override fun onCheckedChanged(data: HeaderOrFooterData) {
                Toast.makeText(this@MainActivity, data.isChecked.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        // Setup the recycler view
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
