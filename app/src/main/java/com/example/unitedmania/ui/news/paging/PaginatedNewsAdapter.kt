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

class NewsAdapterPaginated(private val onArticleClicked: (Article) -> Unit) :
    PagingDataAdapter<Article, NewsAdapterPaginated.NewsViewHolder>(NEWS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = NewsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding, onArticleClicked)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { article -> holder.setUi(article) }
    }

    inner class NewsViewHolder(
        private val binding: NewsListItemBinding,
        private val onArticleClicked: (Article) -> Unit,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val clickedPosition = bindingAdapterPosition
                // to avoid a crash if an item was clicked while it is animating off the screen
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    val clickedNews = getItem(bindingAdapterPosition)
                    if (clickedNews != null) {
                        onArticleClicked(clickedNews)
                    }
                }
            }
        }

        fun setUi(article: Article) {
            Picasso.get()
                .load(article.imageUrl)
                .fit()
                .placeholder(R.drawable.placeholder)
                .into(binding.image)
            binding.source.text = article.source.sourceName
            binding.title.text = article.title
            binding.details.text = filterDetails(article.details)
        }

        private fun filterDetails(unfilteredDetails: String): String {
            // news details field always ends with these characters
            // that indicate how many more characters are remaining
            val detailsStopPosition: Int = unfilteredDetails.indexOf("[+")
            return if (detailsStopPosition != -1) {
                val details = unfilteredDetails.substring(0, detailsStopPosition)
                details
            } else {
                "Click to see more.."
            }
        }
    }

    companion object {
        private val NEWS_COMPARATOR = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean = oldItem.title == newItem.title
            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean = oldItem == newItem
        }
    }


}