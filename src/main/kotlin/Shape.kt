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
    
    // Lista de puntos que definen la figura para poder mostrarlos y editarlos
    abstract fun getShapePoints(): List<Point2D>
    
    // Actualiza las coordenadas del punto de control en el índice dado
    abstract fun setControlPoint(index: Int, x: Double, y: Double)
    
    // Obtiene el centroide de la figura (para arrastre completo)
    abstract fun getCenter(): Point2D
    
    // Mueve la figura completa sumando dx y dy a todos sus puntos
    abstract fun translate(dx: Double, dy: Double)
    
    // Cambia el color de borde (y relleno si la figura lo soporta)
    abstract fun setColor(newBorder: Color, newFill: Color? = null)
    
    // Devuelve una copia independiente (Deep Copy) de la figura para el historial
    abstract fun clone(): Shape
    
    // Devuelve una representación en texto plano de la figura para guardado
    abstract fun serialize(): String
}
