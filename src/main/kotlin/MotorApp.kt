import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryUtil

var offscreenWindow: Long = 0L

fun main() {
    if (!glfwInit()) throw IllegalStateException("Fallo al iniciar GLFW")
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    offscreenWindow = glfwCreateWindow(800, 600, "Offscreen", MemoryUtil.NULL, MemoryUtil.NULL)
    Application.launch(MotorApp::class.java)
}

class MotorApp : Application() {
    override fun start(stage: Stage) {
        val fxmlUrl = MotorApp::class.java.getResource("interface.fxml")
        val loader = FXMLLoader(fxmlUrl)
        val root: Parent = loader.load()
        stage.title = "Proyecto #1 - Gestión y Despliegue de Primitivas"
        stage.scene = Scene(root)
        // Alterar esto va a requerir hacer ciertos cambios para evitar errores
        stage.isResizable = false
        stage.show()
        stage.setOnCloseRequest {
            glfwDestroyWindow(offscreenWindow)
            glfwTerminate()
        }
    }
}