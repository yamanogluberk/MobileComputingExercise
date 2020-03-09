package com.yamanoglu.mobilecomputingexercise

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.item_list_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter(context: Context, private val list: List<TableReminder>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    @SuppressLint("SimpleDateFormat")
    override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {
        val view = inflater.inflate(R.layout.item_list_view, p2, false)
        view.tvMessage.text = list[position].message

        if (list[position].time != null) {
            val sdf = SimpleDateFormat("HH:mm dd.MM.yyyy")
            sdf.timeZone = TimeZone.getDefault()

            val time = list[position].time
            val readableTime = sdf.format(time)
            view.tvTrigger.text = readableTime

        } else if (list[position].location != null) {
            view.tvTrigger.text = list[position].location
        }

        return view
    }

    override fun getItem(p0: Int): Any {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}