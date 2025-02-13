package cz.cuni.mff.hubinkok.totravel.data

/**
 * Enum representing different categories of a point.
 *
 * VISIT - A point marked for a visit.
 * PLANNED - A planned point to be visited.
 * VISITED - A point that has already been visited.
 * FAVOURITE - A favorite location.
 * CUSTOM - A custom-defined category.
 */
enum class PointTag {
    VISIT,
    PLANNED,
    VISITED,
    FAVOURITE,
    CUSTOM
}