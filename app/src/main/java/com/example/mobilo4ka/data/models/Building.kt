package com.example.mobilo4ka.data.models

data class Building(val id: Int, val pixels: List<Pair<Int, Int>>, val name: String? = null, val entrances: List<Pair<Int, Int>>? = null)
