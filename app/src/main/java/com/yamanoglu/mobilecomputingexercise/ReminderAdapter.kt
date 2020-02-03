package com.yamanoglu.mobilecomputingexercise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.item_list_view.view.*

class ReminderAdapter(context: Context, private val list: List<String>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = inflater.inflate(R.layout.item_list_view, p2, false)
        view.tvMessage.text = list[p0]
        view.tvTrigger.text = "Hello Trigger"
        return view
    }

    override fun getItem(p0: Int): Any {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
return 0   }

    override fun getCount(): Int {
        return list.size
    }

}