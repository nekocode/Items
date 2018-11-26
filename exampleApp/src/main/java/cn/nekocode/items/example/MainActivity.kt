package cn.nekocode.items.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import cn.nekocode.items.Items
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = Items.create(TestAdapter::class.java)
        adapter.list().add(HeaderOrFooterData().apply {
            text = "Header"
            isHeader = true
        })

        for (i in 0..10) {
            adapter.list().add("Item$i")
        }

        adapter.list().add(HeaderOrFooterData().apply {
            text = "Footer"
            isHeader = false
            isChecked = true
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
