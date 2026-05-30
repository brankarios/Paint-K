import kotlin.math.abs

class Triangle(
    var x0: Int, 
    var y0: Int,
    var x1: Int, 
    var y1: Int,
    var x2: Int, 
    var y2: Int,
    borderColor: Color,
    var fillColor: Color? = null
) : Shape(borderColor) {

    override fun draw(engine: Engine2D) {
        if (fillColor != null) {
            fillScanLine(engine, fillColor!!)
        }

        drawLine(engine, x0, y0, x1, y1)
        drawLine(engine, x1, y1, x2, y2)
        drawLine(engine, x2, y2, x0, y0)
    }

    private fun fillScanLine(engine: Engine2D, color: Color) {
        var p0x = x0; var p0y = y0
        var p1x = x1; var p1y = y1
        var p2x = x2; var p2y = y2

        // Bubble sort 
        // La idea es que luego de esto p0 sea el más alto
        // p1 el del medio y p2 el más bajo

        if (p1y < p0y) {
            val tx = p0x; p0x = p1x; p1x = tx
            val ty = p0y; p0y = p1y; p1y = ty
        }
        if (p2y < p0y) {
            val tx = p0x; p0x = p2x; p2x = tx
            val ty = p0y; p0y = p2y; p2y = ty
        }
        if (p2y < p1y) {
            val tx = p1x; p1x = p2x; p2x = tx
            val ty = p1y; p1y = p2y; p2y = ty
        }

        // Caso degenerado 
        if (p0y == p2y) return

        // Bresenham modificado para rellenar
        class Edge(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) {
            var x = xStart
            private val dy = yEnd - yStart
            private val absDx = kotlin.math.abs(xEnd - xStart)
            private val sx = if (xEnd > xStart) 1 else -1
            private val stepX = if (dy != 0) absDx / dy else 0
            private val stepErr = if (dy != 0) absDx % dy else 0
            private var err = if (dy != 0) dy / 2 else 0

            fun stepY() {
                if (dy == 0) return
                x += sx * stepX
                err += stepErr
                if (err >= dy) {
                    x += sx
                    err -= dy
                }
            }
        }

        val longEdge = Edge(p0x, p0y, p2x, p2y)
        var shortEdge = Edge(p0x, p0y, p1x, p1y)

        for (y in p0y until p1y) {
            val startX = minOf(longEdge.x, shortEdge.x)
            val endX = maxOf(longEdge.x, shortEdge.x)
            
            for (x in startX..endX) {
                engine.putPixel(x, y, color)
            }
            longEdge.stepY()
            shortEdge.stepY()
        }

        shortEdge = Edge(p1x, p1y, p2x, p2y)
        
        for (y in p1y..p2y) {
            val startX = minOf(longEdge.x, shortEdge.x)
            val endX = maxOf(longEdge.x, shortEdge.x)
            
            for (x in startX..endX) {
                engine.putPixel(x, y, color)
            }
            longEdge.stepY()
            shortEdge.stepY()
        }
    }

    private fun drawLine(engine: Engine2D, startX: Int, startY: Int, endX: Int, endY: Int) {
        var x = startX
        var y = startY
        val dx = abs(endX - startX)
        val dy = abs(endY - startY)
        val sx = if (startX < endX) 1 else -1
        val sy = if (startY < endY) 1 else -1
        var err = (if (dx > dy) dx else -dy) / 2

        while (true) {
            engine.putPixel(x, y, borderColor)
            if (x == endX && y == endY) break
            val e2 = err
            if (e2 > -dx) { err -= dy; x += sx }
            if (e2 < dy) { err += dx; y += sy }
        }
    }
}