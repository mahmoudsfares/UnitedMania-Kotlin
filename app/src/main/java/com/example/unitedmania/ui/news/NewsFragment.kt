package com.example.unitedmania.ui.news

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unitedmania.R
import com.example.unitedmania.databinding.FragmentNewsBinding
import com.example.unitedmania.ui.news.paging.NewsAdapterPaginated
import com.example.unitedmania.ui.news.paging.PagingLoadStateAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.net.UnknownHostException
import java.util.*
import kotlin.concurrent.schedule


@AndroidEntryPoint
class NewsFragment : Fragment(R.layout.fragment_news) {

    private val viewModel: NewsViewModel by viewModels()

    private lateinit var binding: FragmentNewsBinding
    private lateinit var newsAdapterPaginated: NewsAdapterPaginated
    private lateinit var newsLayoutManager: LinearLayoutManager

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewsBinding.bind(view)

        newsLayoutManager = LinearLayoutManager(context)

        newsAdapterPaginated = NewsAdapterPaginated(
            onArticleClicked = { article ->
                goToDetails(article.url)
            }
        )
        newsAdapterPaginated.addLoadStateListener { loadState ->
            val state = loadState.source.refresh
            setUiComponents(state)
        }

        viewModel.fetchNews()
        lifecycleScope.launchWhenStarted {
            viewModel.news.collect {
                newsAdapterPaginated.submitData(viewLifecycleOwner.lifecycle, it!!)
                newsLayoutManager.scrollToPosition(0)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUiComponents(state: LoadState){
        binding.apply {
            loadingSpinner.isVisible = state is LoadState.Loading

            errorTv.isVisible = state is LoadState.Error
            retryBtn.isVisible = state is LoadState.Error
            retryBtn.setOnClickListener { viewModel.fetchNews() }
            if (state is LoadState.Error) {
                val error = state.error
                if (error is UnknownHostException) {
                    errorTv.text = "No internet connection."
                } else {
                    errorTv.text = error.message
                }
            }

            newsRecyclerView.isVisible = state !is LoadState.Error && state is LoadState.NotLoading
            newsRecyclerView.apply {
                layoutManager = newsLayoutManager
                setHasFixedSize(true)
                adapter = newsAdapterPaginated.withLoadStateHeaderAndFooter(
                    header = PagingLoadStateAdapter { newsAdapterPaginated.retry() },
                    footer = PagingLoadStateAdapter { newsAdapterPaginated.retry() }
                )
            }
            newsRefresher.setColorSchemeResources(R.color.background)
            newsRefresher.setOnRefreshListener {
                Timer().schedule(1000) {
                    viewModel.fetchNews()
                    binding.newsRefresher.isRefreshing = false
                }
            }
        }
    }

    private fun goToDetails(url: String) {
        val toDetailsIntent = Intent(Intent.ACTION_VIEW)
        toDetailsIntent.data = Uri.parse(url)
        startActivity(toDetailsIntent)
    }
}
