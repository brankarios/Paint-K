data class Color(var r: Float, var g: Float, var b: Float) {
    fun serialize(): String = "$r,$g,$b"

    companion object {
        val WHITE = Color(1.0f, 1.0f, 1.0f)
        val BLACK = Color(0.0f, 0.0f, 0.0f)
        val RED = Color(1.0f, 0.0f, 0.0f)
        val GREEN = Color(0.0f, 1.0f, 0.0f)
        val BLUE = Color(0.0f, 0.0f, 1.0f)
        val YELLOW = Color(1.0f, 1.0f, 0.0f)
        val BACKGROUND = Color(0.1f, 0.1f, 0.15f)

        fun deserialize(data: String): Color {
            val parts = data.split(",")
            if (parts.size == 3) {
                return Color(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat())
            }
            return BLACK
        }
    }
}