package com.bapercoding.simplecrud

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.fasterxml.jackson.databind.util.ClassUtil.getPackageName
import kotlinx.android.synthetic.main.activity_detail_film.*
import kotlinx.android.synthetic.main.list_photo.view.*
import kotlinx.android.synthetic.main.recycle_layout.view.*

class RVPagerAdapter(private val context: Context, val colorList: List<Int>) : RecyclerView.Adapter<SampleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        return SampleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycle_layout, parent, false))
    }

    override fun getItemCount() = colorList.size

    override fun onBindViewHolder(holder: SampleViewHolder, position: Int) {
        val listPhoto = ArrayList<Photo>()
        listPhoto.add(Photo(R.drawable.comingsoon))

        holder.itemView.mRecyclerViewH.setHasFixedSize(true)
        holder.itemView.mRecyclerViewH.layoutManager = LinearLayoutManager(context)
        if(position == 1 && position == 2){
            val adapter = PhotoFilmAdapter(context,listPhoto)
            adapter.notifyDataSetChanged()
            holder.itemView.mRecyclerViewH.adapter = adapter
        }
        else if (position == 3){
            
        }
    }
}

class SampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}