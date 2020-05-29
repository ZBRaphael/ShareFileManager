package com.example.listview


import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.list_view.view.*

class MyAdapter(var list: ArrayList<String>, var ctx: Context) : BaseAdapter() {
    private var fileList = list
    override fun getCount(): Int {
        return fileList.size
    }
    override fun getItemId(position: Int): Long {
        return position.toLong();
    }
    override fun getItem(position: Int): Any {

        return fileList.get(position);
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var viewHolder: ViewHolder? = null
        var view: View
        if (convertView == null) {
            view = View.inflate(ctx, R.layout.list_view, null);
            viewHolder = ViewHolder(view)
            view.tag = viewHolder;
        } else {
            view = convertView;
            viewHolder = view.tag as ViewHolder
        }
        val item = getItem(position)
        if (item is String) {
            /**
             *直接通过view.text设置文本信息
             */
            viewHolder.tv.text = item;
        }

        return view!!
    }
    open fun refresh(list: ArrayList<String>) {
        fileList = list
        println(fileList)
        notifyDataSetChanged()
    }


}

class ViewHolder(var viewItem: View) {

    var tv: TextView = viewItem.label as TextView
}