import kotlin.math.abs

class Circle(
    var x0: Int,
    var y0: Int,
    var x1: Int,
    var y1: Int,
    borderColor: Color,
    var fillColor: Color? = null
) : Shape(borderColor) {

    override fun draw(engine: Engine2D) {
        val w = abs(x1 - x0)
        val h = abs(y1 - y0)
        
        val rx = w / 2
        val ry = h / 2

        val cx = minOf(x0, x1) + rx
        val cy = minOf(y0, y1) + ry

        if (rx == 0 && ry == 0) {
            engine.putPixel(cx, cy, borderColor)
            return
        }
        if (rx == 0) {
            val startY = cy - ry
            val endY = cy + ry
            for (i in startY..endY) {
                engine.putPixel(cx, i, borderColor)
            }
            return
        }
        if (ry == 0) {
            val startX = cx - rx
            val endX = cx + rx
            drawHLine(engine, startX, endX, cy, borderColor)
            return
        }

        var x = 0
        var y = ry

        val rxSq = (rx * rx).toDouble()
        val rySq = (ry * ry).toDouble()
        var dx = 2 * rySq * x
        var dy = 2 * rxSq * y

        var d1 = rySq - (rxSq * ry) + (0.25 * rxSq) // Factor de decisión para la región 1
        
        drawEllipsePoints(engine, cx, cy, x, y)
        while (dx < dy) {
            x++
            dx += 2 * rySq
            if (d1 < 0) {
                d1 += dx + rySq
            } else {
                y--
                dy -= 2 * rxSq
                d1 += dx - dy + rySq
            }
            drawEllipsePoints(engine, cx, cy, x, y)
        }

        var d2 = rySq * (x + 0.5) * (x + 0.5) + rxSq * (y - 1) * (y - 1) - rxSq * rySq // Factor de decisión para la región 2

        while (y >= 0) {
            drawEllipsePoints(engine, cx, cy, x, y)
            y--
            dy -= 2 * rxSq
            if (d2 > 0) {
                d2 += rxSq - dy
            } else {
                x++
                dx += 2 * rySq
                d2 += dx - dy + rxSq
            }
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
        // En el círculo, x0,y0 y x1,y1 definen el bounding box que inscribe la elipse
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
        x1 = (x0 + (x1 - x0) * factor).toInt()
        y1 = (y0 + (y1 - y0) * factor).toInt()
    }

    override fun setColor(newBorder: Color, newFill: Color?) {
        borderColor = newBorder
        fillColor = newFill
    }

    override fun clone(): Shape {
        val fCol = if (fillColor != null) Color(fillColor!!.r, fillColor!!.g, fillColor!!.b) else null
        return Circle(x0, y0, x1, y1, Color(borderColor.r, borderColor.g, borderColor.b), fCol).also {
            it.zIndex = this.zIndex
        }
    }

    override fun serialize(): String {
        val fStr = if (fillColor != null) fillColor!!.serialize() else "null"
        return "CIRCLE;$x0;$y0;$x1;$y1;${borderColor.serialize()};$fStr"
    }

    private fun drawEllipsePoints(engine: Engine2D, cx: Int, cy: Int, x: Int, y: Int) {
        if (fillColor != null) {
            val fillCol = fillColor!!
            drawHLine(engine, cx - x, cx + x, cy + y, fillCol)
            drawHLine(engine, cx - x, cx + x, cy - y, fillCol)
        }


        engine.putPixel(cx + x, cy + y, borderColor)
        engine.putPixel(cx - x, cy + y, borderColor)
        engine.putPixel(cx + x, cy - y, borderColor)
        engine.putPixel(cx - x, cy - y, borderColor)
    }

    private fun drawHLine(engine: Engine2D, minX: Int, maxX: Int, y: Int, color: Color) {
        for (i in minX..maxX) {
            engine.putPixel(i, y, color)
        }
    }
}
