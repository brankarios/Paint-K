class Rectangle(
    var x0: Int,
    var y0: Int,
    var x1: Int,
    var y1: Int,
    borderColor: Color,
    var fillColor: Color? = null 
) : Shape(borderColor) {

    override fun draw(engine: Engine2D) {
        val xMin = minOf(x0, x1)
        val xMax = maxOf(x0, x1)
        val yMin = minOf(y0, y1)
        val yMax = maxOf(y0, y1)

        drawHLine(engine, xMin, xMax, yMin, borderColor)

        if (yMin != yMax) {
            drawHLine(engine, xMin, xMax, yMax, borderColor) 
        }

        for (y in yMin + 1 until yMax) {
            engine.putPixel(xMin, y, borderColor)
            
            if (xMin != xMax) {
                engine.putPixel(xMax, y, borderColor)
            }

            fillColor?.let { fillCol ->
                if (xMin + 1 < xMax) {
                    drawHLine(engine, xMin + 1, xMax - 1, y, fillCol)
                }
            }
        }
    }

    private fun drawHLine(engine: Engine2D, minX: Int, maxX: Int, y: Int, color: Color) {
        for (x in minX..maxX) {
            engine.putPixel(x, y, color)
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
        // En un rectángulo usamos las 4 esquinas como puntos de control para mayor naturalidad, o solo 2 (opuestas).
        // Vamos a usar 2 puntos (las esquinas definidoras x0,y0 y x1,y1) para mantenerlo simple.
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

    override fun scale(factor: Double) {
        val center = getCenter()
        x0 = (center.x + (x0 - center.x) * factor).toInt()
        y0 = (center.y + (y0 - center.y) * factor).toInt()
        x1 = (center.x + (x1 - center.x) * factor).toInt()
        y1 = (center.y + (y1 - center.y) * factor).toInt()
    }

    override fun setColor(newBorder: Color, newFill: Color?) {
        borderColor = newBorder
        fillColor = newFill
    }

    override fun clone(): Shape {
        val fCol = if (fillColor != null) Color(fillColor!!.r, fillColor!!.g, fillColor!!.b) else null
        return Rectangle(x0, y0, x1, y1, Color(borderColor.r, borderColor.g, borderColor.b), fCol).also {
            it.zIndex = this.zIndex
        }
    }

    override fun serialize(): String {
        val fStr = if (fillColor != null) fillColor!!.serialize() else "null"
        return "RECTANGLE;$x0;$y0;$x1;$y1;${borderColor.serialize()};$fStr"
    }
}