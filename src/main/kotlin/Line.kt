import kotlin.math.abs

class Line(var x0: Int, var y0: Int, var x1: Int, var y1: Int, borderColor: Color) : Shape(borderColor) {
    override fun draw(engine: Engine2D) {
        engine.drawLine(x0, y0, x1, y1, borderColor)
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

    override fun scale(factor: Double) {
        val center = getCenter()
        x0 = (center.x + (x0 - center.x) * factor).toInt()
        y0 = (center.y + (y0 - center.y) * factor).toInt()
        x1 = (center.x + (x1 - center.x) * factor).toInt()
        y1 = (center.y + (y1 - center.y) * factor).toInt()
    }

    override fun setColor(newBorder: Color, newFill: Color?) {
        borderColor = newBorder
    }

    override fun clone(): Shape {
        return Line(x0, y0, x1, y1, Color(borderColor.r, borderColor.g, borderColor.b)).also {
            it.zIndex = this.zIndex
        }
    }

    override fun serialize(): String {
        return "LINE;$x0;$y0;$x1;$y1;${borderColor.serialize()}"
    }
}