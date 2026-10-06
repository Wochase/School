package com.example.school

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform