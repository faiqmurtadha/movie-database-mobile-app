package com.example.themoviesdb.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.themoviesdb.R
import com.example.themoviesdb.activities.DetailActivity
import com.example.themoviesdb.activities.FavoriteActivity
import com.example.themoviesdb.domain.ListFilm

class FilmListAdapter(private val items: ListFilm) : RecyclerView.Adapter<FilmListAdapter.ViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_film, parent, false)
        return ViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = items.data?.get(position)

        // Set movie title
        holder.titleText.text = movie?.title

        // Setup Glide to load the poster
        val posterUrl = "https://image.tmdb.org/t/p/w500" + movie?.poster

        val requestOptions = RequestOptions()
            .transform(CenterCrop(), RoundedCorners(30))

        Glide.with(context)
            .load(posterUrl)
            .apply(requestOptions)
            .into(holder.pic)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java).apply {
                putExtra("movie_id", movie?.id)
            }
//              val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return items.data?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.textFilm)
        val pic: ImageView = itemView.findViewById(R.id.imageFilm)
    }
}