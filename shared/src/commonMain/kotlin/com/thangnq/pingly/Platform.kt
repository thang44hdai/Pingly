package com.thangnq.pingly

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform