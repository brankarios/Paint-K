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
}