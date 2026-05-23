import javafx.fxml.FXML
import javafx.scene.control.ColorPicker
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton

class Proyecto1 : Engine2D() {
    // Controles de la Interfaz inyectados por JavaFX (Visor y Selector de Color)
    @FXML private lateinit var viewport: ImageView
    @FXML private lateinit var fxColorPicker: ColorPicker
    
    // Variables de estado del Proyecto
    private var currentColor = Color.RED
    private var isDrawing = false
    
    // Lista en memoria que guarda todas las figuras 
    private val shapes = mutableListOf<Shape>()
    
    // Variable temporal para guardar la línea que estamos trazando actualmente con el mouse
    private var currentLine: Line? = null

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
        clear(Color.BACKGROUND)
        
        for (shape in shapes) {
            shape.draw(this)
        }
        
        currentLine?.draw(this)
    }

    override fun onKeyDown(key: KeyCode) {
        if (key == KeyCode.SPACE) {
            shapes.clear()
            currentLine = null
        }
    }

    override fun onMouseButtonDown(button: MouseButton, x: Double, y: Double) {
        if (button == MouseButton.PRIMARY) {
            // Empezamos a dibujar: creamos una nueva línea temporal que empieza y termina donde se hizo el clic
            isDrawing = true
            currentLine = Line(x.toInt(), y.toInt(), x.toInt(), y.toInt(), currentColor)
        }
    }

    override fun onMouseButtonUp(button: MouseButton, x: Double, y: Double) {
        if (button == MouseButton.PRIMARY) {
            isDrawing = false
            currentLine?.let {
                it.x1 = x.toInt()
                it.y1 = y.toInt()
                
                shapes.add(it)
            }
            currentLine = null
        }
    }

    override fun onMouseMove(x: Double, y: Double) {
        if (isDrawing) {
            // Mientras tenga el clic presionado, actualizamos a dónde apunta la línea en tiempo real
            currentLine?.x1 = x.toInt()
            currentLine?.y1 = y.toInt()
        }
    }
}