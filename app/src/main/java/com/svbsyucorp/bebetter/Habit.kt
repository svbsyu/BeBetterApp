package com.svbsyucorp.bebetter

data class Habit(
    val id: String = "",
    val title: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val days: List<String> = emptyList()
)