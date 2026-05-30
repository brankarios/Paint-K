data class Point2D(var x: Double, var y: Double)

class Bezier(
    var controlPoints: MutableList<Point2D>,
    borderColor: Color
) : Shape(borderColor) {

    override fun draw(engine: Engine2D) {
        if (controlPoints.isEmpty()) return

        // Líneas guía entre puntos de control
        if (controlPoints.size > 1) {
            val polygonColor = Color(0.5f, 0.5f, 0.5f) 
            for (i in 0 until controlPoints.size - 1) {
                drawLine(engine, controlPoints[i].x.toInt(), controlPoints[i].y.toInt(), 
                         controlPoints[i+1].x.toInt(), controlPoints[i+1].y.toInt(), polygonColor)
            }
        }

        // Puntos de control (son unos cuadraditos de 5x5)
        val pointColor = Color(0.0f, 0.0f, 1.0f)
        for (p in controlPoints) {
            for (dx in -2..2) {
                for (dy in -2..2) {
                    engine.putPixel(p.x.toInt() + dx, p.y.toInt() + dy, pointColor)
                }
            }
        }

        if (controlPoints.size < 2) return

        val steps = 100
        var prevX = controlPoints[0].x.toInt()
        var prevY = controlPoints[0].y.toInt()

        for (step in 1..steps) {
            val t = step.toDouble() / steps
            val point = deCasteljau(t)
            val currX = point.x.toInt()
            val currY = point.y.toInt()

            drawLine(engine, prevX, prevY, currX, currY, borderColor)
            prevX = currX
            prevY = currY
        }
    }

    private fun deCasteljau(t: Double): Point2D {
        val n = controlPoints.size
        val b = Array(n) { Point2D(controlPoints[it].x, controlPoints[it].y) }

        // Se ejecuta n-1 veces hasta reducir a un solo punto, el final de la curva para el t dado
        for (r in 1 until n) {
            for (i in 0 until n - r) {
                b[i].x = (1 - t) * b[i].x + t * b[i+1].x // Interpolación lineal entre b[i] y b[i+1]
                b[i].y = (1 - t) * b[i].y + t * b[i+1].y
            }
        }
        return b[0]
    }

    fun elevateDegree() {
        val n = controlPoints.size - 1 
        if (n < 0) return

        val newPoints = mutableListOf<Point2D>()
        // El primer punto se mantiene igual
        newPoints.add(Point2D(controlPoints[0].x, controlPoints[0].y))

        for (j in 1..n) {
            val factor1 = j.toDouble() / (n + 1)
            val factor2 = 1.0 - factor1
            val nx = factor1 * controlPoints[j - 1].x + factor2 * controlPoints[j].x
            val ny = factor1 * controlPoints[j - 1].y + factor2 * controlPoints[j].y
            newPoints.add(Point2D(nx, ny))
        }

        // El último punto se mantiene igual
        newPoints.add(Point2D(controlPoints[n].x, controlPoints[n].y))
        
        controlPoints = newPoints
    }

    private fun drawLine(engine: Engine2D, startX: Int, startY: Int, endX: Int, endY: Int, color: Color) {
        var x = startX
        var y = startY
        val dx = kotlin.math.abs(endX - startX)
        val dy = kotlin.math.abs(endY - startY)
        val sx = if (startX < endX) 1 else -1
        val sy = if (startY < endY) 1 else -1
        var err = (if (dx > dy) dx else -dy) / 2

        while (true) {
            engine.putPixel(x, y, color)
            if (x == endX && y == endY) break
            val e2 = err
            if (e2 > -dx) { err -= dy; x += sx }
            if (e2 < dy) { err += dx; y += sy }
        }
    }
}