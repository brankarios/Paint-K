import javafx.fxml.FXML
import javafx.scene.control.ColorPicker
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton

enum class Tool {
    LINE,
    RECTANGLE
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

    @FXML
    fun initialize() {
        bindEngine(viewport, 1024, 600)
        
        fxColorPicker.setOnAction {
            val jfxColor = fxColorPicker.value
            currentColor = Color(jfxColor.red.toFloat(), jfxColor.green.toFloat(), jfxColor.blue.toFloat())
        }
    }

    override fun setup() {
        // Setup is called once. Clearing is now handled in update() per frame.
    }

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

    override fun onKeyDown(key: KeyCode) {
        when (key) {
            KeyCode.SPACE -> {
                shapes.clear()
                currentShape = null
            }
            KeyCode.L -> {
                currentTool = Tool.LINE
                println("Herramienta seleccionada: LÍNEA")
            }
            KeyCode.R -> {
                currentTool = Tool.RECTANGLE
                println("Herramienta seleccionada: RECTÁNGULO")
            }
            KeyCode.F -> {
                isFilled = !isFilled
                println("Modo Relleno alternado a: ${if (isFilled) "ACTIVADO" else "DESACTIVADO"}")
            }
            else -> {}
        }
    }

    override fun onMouseButtonDown(button: MouseButton, x: Double, y: Double) {
        if (button == MouseButton.PRIMARY) {
            isDrawing = true
            
            // Si el modo relleno está activo, creamos una versión más oscura del color principal para el interior
            val fillCol = if (isFilled) {
                Color(currentColor.r * 0.4f, currentColor.g * 0.4f, currentColor.b * 0.4f)
            } else {
                null
            }

            // Creamos la figura correspondiente según la herramienta activa
            currentShape = when (currentTool) {
                Tool.LINE -> Line(x.toInt(), y.toInt(), x.toInt(), y.toInt(), currentColor)
                Tool.RECTANGLE -> Rectangle(x.toInt(), y.toInt(), x.toInt(), y.toInt(), currentColor, fillCol)
            }
        }
    }

    // Cuando el usuario suelta el clic
    override fun onMouseButtonUp(button: MouseButton, x: Double, y: Double) {
        if (button == MouseButton.PRIMARY) {
            isDrawing = false
            currentShape?.let { shape ->
                // Actualizamos las coordenadas finales de la figura
                when (shape) {
                    is Line -> {
                        shape.x1 = x.toInt()
                        shape.y1 = y.toInt()
                    }
                    is Rectangle -> {
                        shape.x1 = x.toInt()
                        shape.y1 = y.toInt()
                    }
                }
                // Guardamos la figura terminada definitivamente en la lista
                shapes.add(shape)
            }
            // Limpiamos la variable temporal
            currentShape = null
        }
    }

    // Cuando el usuario mueve el ratón
    override fun onMouseMove(x: Double, y: Double) {
        if (isDrawing) {
            // Mientras tenga el clic presionado, actualizamos el extremo de la figura en tiempo real
            when (val shape = currentShape) {
                is Line -> {
                    shape.x1 = x.toInt()
                    shape.y1 = y.toInt()
                }
                is Rectangle -> {
                    shape.x1 = x.toInt()
                    shape.y1 = y.toInt()
                }
            }
        }
    }
}