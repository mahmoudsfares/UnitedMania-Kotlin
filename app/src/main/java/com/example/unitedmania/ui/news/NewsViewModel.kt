package com.example.unitedmania.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.unitedmania.data_sources.RetrofitInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 5
private const val MAX_SIZE = 15

@HiltViewModel
class NewsViewModel @Inject constructor(private val retrofitInterface: RetrofitInterface) : ViewModel(){

    private val newsChannel = Channel<Boolean>()
    private val newsFlow = newsChannel.receiveAsFlow()

    fun fetchNews() {
        viewModelScope.launch {
            newsChannel.send(true)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val news = newsFlow.flatMapLatest {
        Pager(
            config = PagingConfig(PAGE_SIZE, MAX_SIZE, false),
            pagingSourceFactory = { NewsRepo(retrofitInterface) }
        ).flow.cachedIn(viewModelScope)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)
}
