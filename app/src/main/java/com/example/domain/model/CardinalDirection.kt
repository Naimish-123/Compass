package com.example.domain.model

enum class CardinalDirection(
    val shortName: String,
    val fullName: String,
    val centerDegrees: Float
) {
    NORTH("N", "North", 0f),
    NORTH_EAST("NE", "North East", 45f),
    EAST("E", "East", 90f),
    SOUTH_EAST("SE", "South East", 135f),
    SOUTH("S", "South", 180f),
    SOUTH_WEST("SW", "South West", 225f),
    WEST("W", "West", 270f),
    NORTH_WEST("NW", "North West", 315f);

    companion object {
        /**
         * Resolves the nearest 8-point cardinal direction for a given azimuth angle.
         */
        fun fromDegrees(azimuth: Float): CardinalDirection {
            val normalized = ((azimuth % 360f) + 360f) % 360f
            return when {
                normalized >= 337.5f || normalized < 22.5f -> NORTH
                normalized < 67.5f -> NORTH_EAST
                normalized < 112.5f -> EAST
                normalized < 157.5f -> SOUTH_EAST
                normalized < 202.5f -> SOUTH
                normalized < 247.5f -> SOUTH_WEST
                normalized < 292.5f -> WEST
                else -> NORTH_WEST
            }
        }

        /**
         * Returns 16-point cardinal compass text (e.g. N, NNE, NE, ENE, E, etc.)
         */
        fun to16Point(azimuth: Float): String {
            val directions = arrayOf(
                "N", "NNE", "NE", "ENE",
                "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW",
                "W", "WNW", "NW", "NNW"
            )
            val normalized = ((azimuth % 360f) + 360f) % 360f
            val index = ((normalized + 11.25f) / 22.5f).toInt() % 16
            return directions[index]
        }

        /**
         * Checks if the heading is within tolerance of a primary cardinal direction (N, E, S, W).
         */
        fun isPrimaryCardinal(azimuth: Float, tolerance: Float = 1.0f): Boolean {
            val normalized = ((azimuth % 360f) + 360f) % 360f
            val cardinals = floatArrayOf(0f, 90f, 180f, 270f)
            return cardinals.any { target ->
                val diff = Math.abs(normalized - target)
                val circularDiff = Math.min(diff, 360f - diff)
                circularDiff <= tolerance
            }
        }
    }
}
