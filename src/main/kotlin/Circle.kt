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
