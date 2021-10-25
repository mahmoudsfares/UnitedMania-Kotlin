package com.example.unitedmania.ui.news.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.unitedmania.R
import com.example.unitedmania.data.Article
import com.example.unitedmania.databinding.NewsListItemBinding
import com.squareup.picasso.Picasso

/**
 * displayed data is provided in type PagingData<T>, T being the type of the object that populates one list item, not a list of that type
 * data is provided to its final function submitData(lifecycle, PagingData<T>)
 */
class NewsAdapterPaginated(private val onArticleClicked: (Article) -> Unit) :
    PagingDataAdapter<Article, NewsAdapterPaginated.NewsViewHolder>(NEWS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = NewsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding, onArticleClicked)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(it) }
    }

    inner class NewsViewHolder(
        private val binding: NewsListItemBinding,
        private val onArticleClicked: (Article) -> Unit,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                // to avoid a crash if u click on an item while it is animating off the screen
                if (position != RecyclerView.NO_POSITION) {
                    val clickedItem = getItem(bindingAdapterPosition)
                    if (clickedItem != null) {
                        onArticleClicked(clickedItem)
                    }
                }
            }
        }

        fun bind(article: Article) {
            Picasso.get()
                .load(article.imageUrl)
                .fit()
                .placeholder(R.drawable.placeholder)
                .into(binding.image)
            binding.source.text = article.source.source
            binding.title.text = article.title

            var details = article.details
            val detailsStopPosition: Int = article.details.indexOf("[+")
            if (detailsStopPosition != -1)
                details = article.details.substring(0, detailsStopPosition)

            binding.details.text = details
        }
    }

    companion object {
        private val NEWS_COMPARATOR = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem == newItem
        }
    }
}