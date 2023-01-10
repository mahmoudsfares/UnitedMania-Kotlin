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
    private lateinit var newsLayoutManager: LinearLayoutManager

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNewsBinding.bind(view)

        viewModel.fetchNews()

        newsLayoutManager = LinearLayoutManager(context)

        val newsAdapterPaginated = NewsAdapterPaginated(
            onArticleClicked = {
                val toDetailsIntent = Intent(Intent.ACTION_VIEW)
                toDetailsIntent.data = Uri.parse(it.url)
                startActivity(toDetailsIntent)
            }
        )

        newsAdapterPaginated.addLoadStateListener { loadState ->
            binding.apply {
                loadingSpinner.isVisible = loadState.source.refresh is LoadState.Loading
                errorTv.isVisible = loadState.source.refresh is LoadState.Error
                retryBtn.isVisible = loadState.source.refresh is LoadState.Error
                retryBtn.setOnClickListener { viewModel.fetchNews() }
                list.isVisible = loadState.source.refresh !is LoadState.Error && loadState.source.refresh is LoadState.NotLoading
                if (loadState.source.refresh is LoadState.Error) {
                    val error = (loadState.source.refresh as LoadState.Error).error
                    if (error is UnknownHostException){
                        errorTv.text = "No internet connection."
                    }
                    else {
                        errorTv.text = error.message
                    }
                }
            }
        }

        binding.list.apply {
            layoutManager = newsLayoutManager
            setHasFixedSize(true)
            adapter = newsAdapterPaginated.withLoadStateHeaderAndFooter(
                header = PagingLoadStateAdapter { newsAdapterPaginated.retry() },
                footer = PagingLoadStateAdapter { newsAdapterPaginated.retry() }
            )
        }

        lifecycleScope.launchWhenStarted {
            viewModel.news.collect {
                newsAdapterPaginated.submitData(viewLifecycleOwner.lifecycle, it!!)
                newsLayoutManager.scrollToPosition(0)
            }
        }

        binding.refresher.setColorSchemeResources(R.color.background)
        binding.refresher.setOnRefreshListener {
            Timer().schedule(1000) {
                viewModel.fetchNews()
                binding.refresher.isRefreshing = false
            }
        }
    }
}
