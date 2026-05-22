import javafx.fxml.FXML
import javafx.scene.control.ColorPicker
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton

class Proyecto1 : Engine2D() {
    // Controles de la Interfaz inyectados por JavaFX
    @FXML private lateinit var viewport: ImageView
    @FXML private lateinit var fxColorPicker: ColorPicker
    // Variables de estado del Proyecto
    private var actualColor = Color.RED
    private var dibujando = false
    @FXML
    fun initialize() {
        bindEngine(viewport, 1024, 600)
        fxColorPicker.setOnAction {
            val jfxColor = fxColorPicker.value
            actualColor = Color(jfxColor.red.toFloat(), jfxColor.green.toFloat(), jfxColor.blue.toFloat())
        }
    }

    override fun setup() {
        clear(Color.BACKGROUND)
    }

    override fun onKeyDown(key: KeyCode) {
        if (key == KeyCode.SPACE) {
            clear(Color.BACKGROUND)
        }
    }

    override fun onMouseButtonDown(button: MouseButton, x: Double, y: Double) {
        if (button == MouseButton.PRIMARY) {
            dibujando = true
            putPixel(x.toInt(), y.toInt(), actualColor)
        }
    }

    override fun onMouseButtonUp(button: MouseButton, x: Double, y: Double) {
        if (button == MouseButton.PRIMARY) {
            dibujando = false
        }
    }

    override fun onMouseMove(x: Double, y: Double) {
        if (dibujando) {
            putPixel(x.toInt(), y.toInt(), actualColor)
        }
    }
}