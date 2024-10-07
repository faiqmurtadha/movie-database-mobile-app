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
import com.example.themoviesdb.domain.ListActor

class ActorListAdapter(private val items: ListActor) : RecyclerView.Adapter<ActorListAdapter.ViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_actor, parent, false)
        return ViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val credit = items.data?.get(position)

        holder.name.text = credit?.name
        holder.role.text = credit?.role

        val photoUrl = "https://image.tmdb.org/t/p/w500" + credit?.photo

        val requestOptions = RequestOptions()
            .transform(CenterCrop(), RoundedCorners(30))

        Glide.with(context)
            .load(photoUrl)
            .apply(requestOptions)
            .into(holder.photo)
    }

    override fun getItemCount(): Int {
        return items.data?.size?.coerceAtMost(10) ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.actorName)
        val photo: ImageView = itemView.findViewById(R.id.actorImage)
        val role: TextView = itemView.findViewById(R.id.actorRole)
    }
}