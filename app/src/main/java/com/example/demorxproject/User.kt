package com.example.demorxproject

data class User(
    val id: String,
    var name: String,
    val surname: String,
    val age: Int,
    var additionalData: String = "",
    val queue: Int
) {

    val isAdult: Boolean
        get() = age > 18
}