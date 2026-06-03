class ShapeFactory {
    companion object {
        fun deserialize(data: String): Shape? {
            try {
                val parts = data.split(";")
                val type = parts[0]
                
                when (type) {
                    "LINE" -> {
                        val x0 = parts[1].toInt()
                        val y0 = parts[2].toInt()
                        val x1 = parts[3].toInt()
                        val y1 = parts[4].toInt()
                        val border = Color.deserialize(parts[5])
                        return Line(x0, y0, x1, y1, border)
                    }
                    "RECTANGLE" -> {
                        val x0 = parts[1].toInt()
                        val y0 = parts[2].toInt()
                        val x1 = parts[3].toInt()
                        val y1 = parts[4].toInt()
                        val border = Color.deserialize(parts[5])
                        val fillStr = parts[6]
                        val fill = if (fillStr == "null") null else Color.deserialize(fillStr)
                        return Rectangle(x0, y0, x1, y1, border, fill)
                    }
                    "CIRCLE" -> {
                        val x0 = parts[1].toInt()
                        val y0 = parts[2].toInt()
                        val x1 = parts[3].toInt()
                        val y1 = parts[4].toInt()
                        val border = Color.deserialize(parts[5])
                        val fillStr = parts[6]
                        val fill = if (fillStr == "null") null else Color.deserialize(fillStr)
                        return Circle(x0, y0, x1, y1, border, fill)
                    }
                    "TRIANGLE" -> {
                        val x0 = parts[1].toInt()
                        val y0 = parts[2].toInt()
                        val x1 = parts[3].toInt()
                        val y1 = parts[4].toInt()
                        val x2 = parts[5].toInt()
                        val y2 = parts[6].toInt()
                        val border = Color.deserialize(parts[7])
                        val fillStr = parts[8]
                        val fill = if (fillStr == "null") null else Color.deserialize(fillStr)
                        return Triangle(x0, y0, x1, y1, x2, y2, border, fill)
                    }
                    "BEZIER" -> {
                        val ptsStr = parts[1].split(",")
                        val points = mutableListOf<Point2D>()
                        for (pt in ptsStr) {
                            if (pt.isNotBlank()) {
                                val coords = pt.split("|")
                                points.add(Point2D(coords[0].toDouble(), coords[1].toDouble()))
                            }
                        }
                        val border = Color.deserialize(parts[2])
                        val isFinalized = parts[3].toBoolean()
                        val b = Bezier(points, border)
                        b.isFinalized = isFinalized
                        return b
                    }
                }
            } catch (e: Exception) {
                println("Error deserializando figura: " + e.message)
            }
            return null
        }
    }
}
