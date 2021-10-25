package com.example.unitedmania.ui.news.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.unitedmania.data.Article
import com.example.unitedmania.retrofit.RetrofitInterface
import retrofit2.HttpException

private const val STARTING_PAGE_INDEX = 1

class NewsPagingSource(
    private val service: RetrofitInterface,
) : PagingSource<Int, Article>() {
    // responsible for loading data from API using Retrofit
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        // key will be null at the very beginning, so we set it to 1
        val currentPage = params.key ?: STARTING_PAGE_INDEX
        return try {
            val articles = service.getArticles(currentPage).articles!!
            LoadResult.Page(
                data = articles,
                prevKey = if (currentPage == STARTING_PAGE_INDEX) null else currentPage - 1,
                nextKey = currentPage + 1
            )
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 404 && currentPage > 1) {
                // articles finished
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (currentPage == STARTING_PAGE_INDEX) null else currentPage - 1,
                    nextKey = null
                )
            }
            else
                LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}