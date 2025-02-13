package cz.cuni.mff.hubinkok.totravel.data

data class Point (
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    var name: String,
    var note: String = "",
    var tag: PointTag = PointTag.VISIT
)
