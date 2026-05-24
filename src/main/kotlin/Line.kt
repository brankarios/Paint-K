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
}