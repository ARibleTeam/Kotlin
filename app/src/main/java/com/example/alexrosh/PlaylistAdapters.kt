package com.example.alexrosh

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.File

class PlaylistGridAdapter(
    private val onClick: (PlaylistEntity) -> Unit
) : RecyclerView.Adapter<PlaylistGridAdapter.PlaylistGridViewHolder>() {

    private val playlists = mutableListOf<PlaylistEntity>()

    fun setItems(items: List<PlaylistEntity>) {
        playlists.clear()
        playlists.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistGridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_grid_item, parent, false)
        return PlaylistGridViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistGridViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener { onClick(playlist) }
    }

    override fun getItemCount(): Int = playlists.size

    class PlaylistGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cover = itemView.findViewById<ImageView>(R.id.playlist_cover)
        private val name = itemView.findViewById<TextView>(R.id.playlist_name)
        private val count = itemView.findViewById<TextView>(R.id.playlist_track_count)

        fun bind(playlist: PlaylistEntity) {
            name.text = playlist.name
            count.text = itemView.context.getTracksCountText(playlist.tracksCount)
            val source = playlist.coverPath?.let(::File) ?: R.drawable.placeholder
            Glide.with(itemView)
                .load(source)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .centerCrop()
                .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.track_artwork_corner_radius)))
                .into(cover)
        }
    }
}

class PlaylistBottomSheetAdapter(
    private val onClick: (PlaylistEntity) -> Unit
) : RecyclerView.Adapter<PlaylistBottomSheetAdapter.PlaylistBottomSheetViewHolder>() {

    private val playlists = mutableListOf<PlaylistEntity>()

    fun setItems(items: List<PlaylistEntity>) {
        playlists.clear()
        playlists.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistBottomSheetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_bottom_sheet_item, parent, false)
        return PlaylistBottomSheetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistBottomSheetViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener { onClick(playlist) }
    }

    override fun getItemCount(): Int = playlists.size

    class PlaylistBottomSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cover = itemView.findViewById<ImageView>(R.id.playlist_cover)
        private val name = itemView.findViewById<TextView>(R.id.playlist_name)
        private val count = itemView.findViewById<TextView>(R.id.playlist_track_count)

        fun bind(playlist: PlaylistEntity) {
            name.text = playlist.name
            count.text = itemView.context.getTracksCountText(playlist.tracksCount)
            val source = playlist.coverPath?.let(::File) ?: R.drawable.placeholder
            Glide.with(itemView)
                .load(source)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .centerCrop()
                .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.track_artwork_corner_radius)))
                .into(cover)
        }
    }
}
