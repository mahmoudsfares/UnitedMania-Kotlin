package com.example.unitedmania.data

import com.google.gson.annotations.SerializedName

data class News(
    @SerializedName("articles") val articles: List<Article>? = null
)

data class ArticleSource(
    @SerializedName("name") val sourceName: String? = null
)

data class Article(
    @SerializedName("source") val source: ArticleSource,
    @SerializedName("title") val title: String,
    @SerializedName("content") val details: String,
    @SerializedName("url") val url: String,
    @SerializedName("urlToImage") val imageUrl: String
)