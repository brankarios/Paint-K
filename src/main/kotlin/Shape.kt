data class BoundingBox(var x: Double, var y: Double, var width: Double, var height: Double) {
    fun contains(px: Double, py: Double): Boolean {
        return px >= x && px <= x + width && py >= y && py <= y + height
    }

    fun intersects(other: BoundingBox): Boolean {
        return x < other.x + other.width && x + width > other.x &&
               y < other.y + other.height && y + height > other.y
    }
}

abstract class Shape(var borderColor: Color, var zIndex: Int = 0) {
    abstract fun draw(engine: Engine2D)
    abstract fun getBounds(): BoundingBox
}
