data class Color(var r: Float, var g: Float, var b: Float) {
    companion object {
        val WHITE = Color(1.0f, 1.0f, 1.0f)
        val BLACK = Color(0.0f, 0.0f, 0.0f)
        val RED = Color(1.0f, 0.0f, 0.0f)
        val GREEN = Color(0.0f, 1.0f, 0.0f)
        val BLUE = Color(0.0f, 0.0f, 1.0f)
        val BACKGROUND = Color(0.1f, 0.1f, 0.15f)
    }
}