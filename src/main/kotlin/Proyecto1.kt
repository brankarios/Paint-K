import javafx.fxml.FXML
import javafx.scene.control.ColorPicker
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton

enum class Tool {
    LINE,
    RECTANGLE,
    CIRCLE
}

class Proyecto1 : Engine2D() {
    // Controles de la Interfaz inyectados por JavaFX (Visor y Selector de Color)
    @FXML private lateinit var viewport: ImageView
    @FXML private lateinit var fxColorPicker: ColorPicker
    
    // Variables de estado del Proyecto
    private var currentColor = Color.RED
    private var isDrawing = false
    private var currentTool = Tool.LINE 
    private var isFilled = false 
    
    private val shapes = mutableListOf<Shape>()
    
    // Variable temporal para guardar la figura que estamos trazando actualmente con el mouse
    private var currentShape: Shape? = null

    // Variables para recordar la posición real del mouse y aplicar la restricción si se pulsa Ctrl sin moverlo
    private var lastMouseX = 0
    private var lastMouseY = 0

    @FXML
    fun initialize() {
        // Inicializamos el motor gráfico al tamaño de la ventana (1024x600)
        bindEngine(viewport, 1024, 600)
        
        // Configuramos el selector de color para que cambie nuestro "currentColor"
        fxColorPicker.setOnAction {
            val jfxColor = fxColorPicker.value
            currentColor = Color(jfxColor.red.toFloat(), jfxColor.green.toFloat(), jfxColor.blue.toFloat())
        }
    }

    override fun setup() {
        // Setup is called once. Clearing is now handled in update() per frame.
    }

    // Este método es el Bucle de Juego (Game Loop), corre automáticamente 60 veces por segundo
    override fun update(deltaTime: Float) {
        // 1. Limpiamos toda la pantalla pintándola de negro
        clear(Color.BACKGROUND)
        
        // 2. Dibujamos TODAS las figuras que ya están guardadas en nuestra lista de memoria
        for (shape in shapes) {
            shape.draw(this)
        }
        
        // 3. Dibujamos la figura que estamos arrastrando actualmente (Efecto PREVIEW)
        currentShape?.draw(this)
    }

    private fun updateCurrentShape() {
        currentShape?.let { shape ->
            var targetX = lastMouseX
            var targetY = lastMouseY

            if (isKeyPressed(KeyCode.CONTROL)) {
                when (shape) {
                    is Rectangle -> {
                        val dx = kotlin.math.abs(lastMouseX - shape.x0)
                        val dy = kotlin.math.abs(lastMouseY - shape.y0)
                        val maxD = maxOf(dx, dy)
                        targetX = shape.x0 + if (lastMouseX > shape.x0) maxD else -maxD
                        targetY = shape.y0 + if (lastMouseY > shape.y0) maxD else -maxD
                    }
                    is Circle -> {
                        val dx = kotlin.math.abs(lastMouseX - shape.x0)
                        val dy = kotlin.math.abs(lastMouseY - shape.y0)
                        val maxD = maxOf(dx, dy)
                        targetX = shape.x0 + if (lastMouseX > shape.x0) maxD else -maxD
                        targetY = shape.y0 + if (lastMouseY > shape.y0) maxD else -maxD
                    }
                }
            }

            when (shape) {
                is Line -> {
                    shape.x1 = targetX
                    shape.y1 = targetY
                }
                is Rectangle -> {
                    shape.x1 = targetX
                    shape.y1 = targetY
                }
                is Circle -> {
                    shape.x1 = targetX
                    shape.y1 = targetY
                }
            }
        }
    }

    // Manejo de teclado para cambiar de herramienta y utilidades
    override fun onKeyDown(key: KeyCode) {
        when (key) {
            KeyCode.SPACE -> {
                // Barra ESPACIADORA: borramos el lienzo
                shapes.clear()
                currentShape = null
            }
            KeyCode.L -> {
                currentTool = Tool.LINE
                println("Herramienta seleccionada: LiNEA")
            }
            KeyCode.R -> {
                currentTool = Tool.RECTANGLE
                println("Herramienta seleccionada: RECTANGULO")
            }
            KeyCode.C -> {
                currentTool = Tool.CIRCLE
                println("Herramienta seleccionada: CIRCULO (ELIPSE)")
            }
            KeyCode.F -> {
                isFilled = !isFilled
                println("Modo Relleno alternado a: ${if (isFilled) "ACTIVADO" else "DESACTIVADO"}")
            }
            KeyCode.CONTROL -> {
                if (isDrawing) updateCurrentShape()
            }
            else -> {}
        }
    }

    override fun onKeyUp(key: KeyCode) {
        if (key == KeyCode.CONTROL && isDrawing) {
            // Si suelta Ctrl mientras dibuja, vuelve a la forma libre
            updateCurrentShape()
        }
    }

    // Cuando el usuario hace clic (presiona el botón)
    override fun onMouseButtonDown(button: MouseButton, x: Double, y: Double) {
        if (button == MouseButton.PRIMARY) {
            isDrawing = true
            lastMouseX = x.toInt()
            lastMouseY = y.toInt()
            
            val fillCol = if (isFilled) {
                Color(currentColor.r * 0.4f, currentColor.g * 0.4f, currentColor.b * 0.4f)
            } else {
                null
            }

            currentShape = when (currentTool) {
                Tool.LINE -> Line(lastMouseX, lastMouseY, lastMouseX, lastMouseY, currentColor)
                Tool.RECTANGLE -> Rectangle(lastMouseX, lastMouseY, lastMouseX, lastMouseY, currentColor, fillCol)
                Tool.CIRCLE -> Circle(lastMouseX, lastMouseY, lastMouseX, lastMouseY, currentColor, fillCol)
            }
        }
    }

    // Cuando el usuario suelta el clic
    override fun onMouseButtonUp(button: MouseButton, x: Double, y: Double) {
        if (button == MouseButton.PRIMARY) {
            lastMouseX = x.toInt()
            lastMouseY = y.toInt()
            // Hacemos una última actualización (que aplicará el Ctrl si está presionado)
            updateCurrentShape()

            currentShape?.let { shape ->
                shapes.add(shape)
            }
            currentShape = null
            isDrawing = false
        }
    }

    // Cuando el usuario mueve el ratón
    override fun onMouseMove(x: Double, y: Double) {
        lastMouseX = x.toInt()
        lastMouseY = y.toInt()
        if (isDrawing) {
            updateCurrentShape()
        }
    }
}