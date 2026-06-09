class QuadTree(val boundary: BoundingBox, val capacity: Int = 4, val level: Int = 0) {
    private val shapes = mutableListOf<Shape>()
    private var divided = false

    private var nw: QuadTree? = null
    private var ne: QuadTree? = null
    private var sw: QuadTree? = null
    private var se: QuadTree? = null

    fun insert(shape: Shape): Boolean {
        if (!boundary.intersects(shape.getBounds())) {
            return false
        }

        // Si hay espacio en este cuadrante, o llegamos al límite máximo de profundidad (5 niveles), la insertamos aquí
        if (shapes.size < capacity || level >= 5) { 
            shapes.add(shape)
            return true
        }

        if (!divided) {
            subdivide()
        }

        if (nw!!.insert(shape)) return true
        if (ne!!.insert(shape)) return true
        if (sw!!.insert(shape)) return true
        if (se!!.insert(shape)) return true

        return false
    }

    private fun subdivide() {
        val x = boundary.x
        val y = boundary.y
        val w = boundary.width / 2.0
        val h = boundary.height / 2.0

        nw = QuadTree(BoundingBox(x, y, w, h), capacity, level + 1)
        ne = QuadTree(BoundingBox(x + w, y, w, h), capacity, level + 1)
        sw = QuadTree(BoundingBox(x, y + h, w, h), capacity, level + 1)
        se = QuadTree(BoundingBox(x + w, y + h, w, h), capacity, level + 1)

        divided = true
    }

    // Busca todas las figuras cuya BoundingBox contenga el punto (x, y)
    fun retrieve(x: Double, y: Double, foundShapes: MutableList<Shape>) {
        if (!boundary.contains(x, y)) {
            return
        }

        for (shape in shapes) {
            if (shape.getBounds().contains(x, y)) {
                foundShapes.add(shape)
            }
        }

        if (divided) {
            nw!!.retrieve(x, y, foundShapes)
            ne!!.retrieve(x, y, foundShapes)
            sw!!.retrieve(x, y, foundShapes)
            se!!.retrieve(x, y, foundShapes)
        }
    }

    fun clear() {
        shapes.clear()
        if (divided) {
            nw!!.clear()
            ne!!.clear()
            sw!!.clear()
            se!!.clear()
            nw = null
            ne = null
            sw = null
            se = null
            divided = false
        }
    }

    fun draw(engine: Engine2D) {
        val gridColor = Color(0.0f, 0.4f, 0.0f) // Verde oscuro tenue
        val minX = boundary.x.toInt()
        val maxX = (boundary.x + boundary.width).toInt()
        val minY = boundary.y.toInt()
        val maxY = (boundary.y + boundary.height).toInt()

        // Dibujar borde del cuadrante usando putPixel directo
        for (i in minX..maxX) {
            engine.putPixel(i, minY, gridColor)
            engine.putPixel(i, maxY, gridColor)
        }
        for (i in minY..maxY) {
            engine.putPixel(minX, i, gridColor)
            engine.putPixel(maxX, i, gridColor)
        }

        if (divided) {
            nw!!.draw(engine)
            ne!!.draw(engine)
            sw!!.draw(engine)
            se!!.draw(engine)
        }
    }
}
