package com.reddittop

data class TopResponse(val data: ResponseData?)

data class ResponseData(val children: List<Item>?, val after: String?, val before: String?)

data class Item(val kind: String?, val data: ItemData?)

data class ItemData(val id: String?,
                    val title: String?,
                    val author: String?,
                    val createdUtc: Long?,
                    val numComments: Long?,
                    val thumbnail: String?,
                    val preview: Preview?)

data class Preview(val images: List<Image>?, val enabled: Boolean?)

data class Image(val source: ImageInfo?, val resolutions: List<ImageInfo>?, val id: String?)

data class ImageInfo(val url: String?, val width: Int?, val height: Int?)