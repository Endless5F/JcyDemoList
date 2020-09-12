package com.android.architecture.demolist.workmanager.model

/**
 * 鞋表
 */
data class ShoesBean(val shoes: List<ShoeBean>)

data class ShoeBean(val id: Int, val name: String, val description: String, val price: Float, val brand: String, val imageUrl: String)