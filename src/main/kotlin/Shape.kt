abstract class Shape(var borderColor: Color, var zIndex: Int = 0) {
    abstract fun draw(engine: Engine2D)
}
