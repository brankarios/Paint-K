data class Point2D(var x: Double, var y: Double)

class Bezier(
    var controlPoints: MutableList<Point2D>,
    borderColor: Color
) : Shape(borderColor) {
    
    var isFinalized: Boolean = false

    override fun draw(engine: Engine2D) {
        if (controlPoints.isEmpty()) return

        if (!isFinalized) {
            // Líneas guía entre puntos de control
            if (controlPoints.size > 1) {
                val polygonColor = Color(0.5f, 0.5f, 0.5f) 
                for (i in 0 until controlPoints.size - 1) {
                    engine.drawLine(controlPoints[i].x.toInt(), controlPoints[i].y.toInt(), 
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

            engine.drawLine(prevX, prevY, currX, currY, borderColor)
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


    override fun getBounds(): BoundingBox {
        if (controlPoints.isEmpty()) return BoundingBox(0.0, 0.0, 0.0, 0.0)
        var minX = controlPoints[0].x
        var maxX = controlPoints[0].x
        var minY = controlPoints[0].y
        var maxY = controlPoints[0].y
        for (p in controlPoints) {
            if (p.x < minX) minX = p.x
            if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y
            if (p.y > maxY) maxY = p.y
        }
        return BoundingBox(minX, minY, maxX - minX, maxY - minY)
    }

    override fun getShapePoints(): List<Point2D> {
        return controlPoints
    }

    override fun setControlPoint(index: Int, x: Double, y: Double) {
        if (index in controlPoints.indices) {
            controlPoints[index].x = x
            controlPoints[index].y = y
        }
    }

    override fun getCenter(): Point2D {
        val bounds = getBounds()
        return Point2D(bounds.x + bounds.width / 2.0, bounds.y + bounds.height / 2.0)
    }

    override fun translate(dx: Double, dy: Double) {
        for (p in controlPoints) {
            p.x += dx
            p.y += dy
        }
    }

    override fun setColor(newBorder: Color, newFill: Color?) {
        borderColor = newBorder
    }

    override fun clone(): Shape {
        val newPoints = mutableListOf<Point2D>()
        for (p in controlPoints) {
            newPoints.add(Point2D(p.x, p.y))
        }
        val b = Bezier(newPoints, Color(borderColor.r, borderColor.g, borderColor.b))
        b.isFinalized = this.isFinalized
        b.zIndex = this.zIndex
        return b
    }

    override fun serialize(): String {
        val pts = controlPoints.joinToString(",") { "${it.x}|${it.y}" }
        return "BEZIER;$pts;${borderColor.serialize()};$isFinalized"
    }
}