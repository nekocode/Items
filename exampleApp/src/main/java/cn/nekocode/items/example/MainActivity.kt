package cn.nekocode.items.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import cn.nekocode.items.Items
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = Items.create(TestAdapter::class.java)

        // Add data
        adapter.list().add(HeaderOrFooterData("Header"))
        for (i in 0..10) {
            adapter.list().add("Item$i")
        }
        adapter.list().add(HeaderOrFooterData("Footer", false))

        // Setup callback for views
        adapter.stringView().setCallback { data ->
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
        }
        adapter.footerView().setCallback { data ->
            Toast.makeText(this, data.isChecked.toString(), Toast.LENGTH_SHORT).show()
        }

        // Setup the recycler view
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
