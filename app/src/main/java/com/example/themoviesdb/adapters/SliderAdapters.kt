package com.example.themoviesdb.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.themoviesdb.domain.SliderItems
import com.example.themoviesdb.R

class SliderAdapters(
    private var sliderItems: List<SliderItems>,
    private val viewPager2: ViewPager2
) : RecyclerView.Adapter<SliderAdapters.SliderViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType : Int): SliderViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.slider_item_container, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.setImage(sliderItems[position])
        holder.setTitle(sliderItems[position].title)
        if (position == sliderItems.size - 2) {
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int = sliderItems.size

    inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageSlide)
        private val titleView: TextView = itemView.findViewById(R.id.titleSlide)

        fun setImage(sliderItems: SliderItems) {
            val requestOptions = RequestOptions()
                .transforms(CenterCrop(), RoundedCorners(60))

            Glide.with(context)
                .load(sliderItems.image)
                .apply(requestOptions)
                .into(imageView)
        }

        fun setTitle(title: String) {
            titleView.text = title
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private val runnable = Runnable {
        if (sliderItems.isNotEmpty()) {
            sliderItems += sliderItems
            notifyDataSetChanged()
        }
    }
}