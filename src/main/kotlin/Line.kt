import kotlin.math.abs

class Line(var x0: Int, var y0: Int, var x1: Int, var y1: Int, borderColor: Color) : Shape(borderColor) {
    override fun draw(engine: Engine2D) {
        var x = x0
        var y = y0
        
        val dx = abs(x1 - x0) // Distancia total que se debe recorrer en cada eje
        val dy = abs(y1 - y0)
        
        val sx = if (x0 < x1) 1 else -1 // Dirección de movimiento en los ejes X e Y (1 o -1)
        val sy = if (y0 < y1) 1 else -1
        
        var isSwapped = false 
        var currentDx = dx
        var currentDy = dy

        // Si la línea es más alta que ancha, intercambiamos los ejes para aplicar Bresenham
        if (dy > dx) {
            isSwapped = true
            currentDx = dy
            currentDy = dx
        }

        var d = currentDx - 2 * currentDy // Decisión, punto medio
        
        val incE = -2 * currentDy
        val incNE = 2 * (currentDx - currentDy)

        engine.putPixel(x, y, borderColor)


        // Algoritmo de Breseham puro y duro
        for (i in 0 until currentDx) {
            if (d <= 0) {
                d += incNE
                if (isSwapped) x += sx else y += sy
                if (isSwapped) y += sy else x += sx
            } else {
                d += incE
                if (isSwapped) y += sy else x += sx
            }
            engine.putPixel(x, y, borderColor)
        }
    }

    override fun getBounds(): BoundingBox {
        val minX = kotlin.math.min(x0, x1).toDouble()
        val maxX = kotlin.math.max(x0, x1).toDouble()
        val minY = kotlin.math.min(y0, y1).toDouble()
        val maxY = kotlin.math.max(y0, y1).toDouble()
        return BoundingBox(minX, minY, maxX - minX, maxY - minY)
    }

    override fun getShapePoints(): List<Point2D> {
        return listOf(Point2D(x0.toDouble(), y0.toDouble()), Point2D(x1.toDouble(), y1.toDouble()))
    }

    override fun setControlPoint(index: Int, x: Double, y: Double) {
        when (index) {
            0 -> { x0 = x.toInt(); y0 = y.toInt() }
            1 -> { x1 = x.toInt(); y1 = y.toInt() }
        }
    }

    override fun getCenter(): Point2D {
        return Point2D((x0 + x1) / 2.0, (y0 + y1) / 2.0)
    }

    override fun translate(dx: Double, dy: Double) {
        x0 += dx.toInt()
        y0 += dy.toInt()
        x1 += dx.toInt()
        y1 += dy.toInt()
    }

    override fun setColor(newBorder: Color, newFill: Color?) {
        borderColor = newBorder
    }
}