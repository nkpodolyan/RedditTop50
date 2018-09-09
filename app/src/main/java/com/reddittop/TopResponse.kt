package com.reddittop

data class TopResponse(val data: ResponseData? = null)

data class ResponseData(val children: List<Item>?, val after: String? = null, val before: String? = null)

data class Item(val kind: String? = null, val data: ItemData? = null)

data class ItemData(val id: String? = null,
                    val title: String? = null,
                    val author: String? = null,
                    val createdUtc: Long? = null,
                    val numComments: Long? = null,
                    val thumbnail: String? = null,
                    val preview: Preview? = null)

data class Preview(val images: List<Image>? = null, val enabled: Boolean? = null)

data class Image(val source: ImageInfo? = null, val resolutions: List<ImageInfo>? = null, val id: String? = null)

data class ImageInfo(val url: String? = null, val width: Int?, val height: Int? = null)