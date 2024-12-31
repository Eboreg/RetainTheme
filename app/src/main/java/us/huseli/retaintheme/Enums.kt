package us.huseli.retaintheme

enum class Difference {
    None,
    Small,
    Significant;

    operator fun plus(other: Difference): Difference {
        if (this == Significant || other == Significant) return Significant
        if (this == Small || other == Small) return Small
        return None
    }
}
