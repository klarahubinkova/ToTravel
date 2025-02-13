package cz.cuni.mff.hubinkok.totravel.data

/**
 * Represents a geographical point with coordinates, name, and additional attributes.
 *
 * @property id Unique identifier for the point.
 * @property latitude Latitude coordinate of the point.
 * @property longitude Longitude coordinate of the point.
 * @property name Name of the point.
 * @property note Optional note associated with the point.
 * @property tag Categorization of the point (e.g., VISIT, PLANNED, etc.).
 */
data class Point (
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    var name: String,
    var note: String = "",
    var tag: PointTag = PointTag.VISIT
)
