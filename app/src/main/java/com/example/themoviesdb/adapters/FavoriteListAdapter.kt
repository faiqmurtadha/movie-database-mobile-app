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
import com.example.themoviesdb.domain.Datum
import com.example.themoviesdb.domain.ListFilm
import java.text.DecimalFormat

class FavoriteListAdapter(private val items: ListFilm, private val genreMap: Map<Int, String>, private val activity: FavoriteActivity) : RecyclerView.Adapter<FavoriteListAdapter.ViewHolder>() {
    private lateinit var context: Context

    private val mutableData: MutableList<Datum> = items.data?.toMutableList() ?: mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_favorite, parent, false)
        return ViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = mutableData[position]

        // Set movie data
        holder.titleText.text = movie?.title
        val genreNames = movie?.genres?.mapNotNull { id: Int? -> genreMap[id ?: 0] }?.joinToString(", ") ?: ""
        holder.genreText.text = genreNames
        holder.yearText.text = movie?.year?.split("-")!![0]
        movie.duration?.let {
            val hours = it / 60
            val minutes = it % 60
            val durationText = if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
            holder.durationText.text = durationText
        }
        val ratingString: String? = movie.rating
        val rating: Double = ratingString!!.toDouble()
        val decimalFormat = DecimalFormat("#.#")
        holder.ratingText.text = decimalFormat.format(rating)

        // Setup Glide to load poster
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
//            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            context.startActivity(intent)
        }

        holder.favoriteBtn.setOnClickListener {
            movie?.id?.let { movieId ->
                val response = activity.removeFavoriteMovie(movieId)

                if (response.isSuccessful) {
                    mutableData?.removeIf { it.id == movieId }
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mutableData.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleFavorite)
        val pic: ImageView = itemView.findViewById(R.id.imageFavorite)
        val genreText: TextView = itemView.findViewById(R.id.genresFavorite)
        val yearText: TextView = itemView.findViewById(R.id.yearFavorite)
        val durationText: TextView = itemView.findViewById(R.id.durationFavorite)
        val ratingText: TextView = itemView.findViewById(R.id.scoreFavorite)
        val favoriteBtn: ImageView = itemView.findViewById(R.id.loveFavorite)
    }
}