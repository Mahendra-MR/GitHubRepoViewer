package com.example.githubrepoviewer.network

data class RateLimitResponse(
    val rate: RateInfo
)

data class RateInfo(
    val limit: Int,
    val remaining: Int,
    val reset: Long // epoch time in seconds
)
