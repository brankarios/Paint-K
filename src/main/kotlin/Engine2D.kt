import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.scene.image.ImageView
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class Engine2D {
    protected var width: Int = 0
    protected var height: Int = 0
    private lateinit var pixelData: FloatArray
    private lateinit var textureBuffer: FloatBuffer
    private lateinit var jfxPixelBuffer: PixelBuffer<ByteBuffer>
    private lateinit var fboReadBuffer: ByteBuffer
    private lateinit var writableImage: WritableImage
    private var canvasTextureID = 0
    private var renderTargetID = 0
    private var vao = 0
    private var vbo = 0
    private var ebo = 0
    private var fbo = 0
    private var shaderProgram = 0
    private val activeKeys = mutableSetOf<KeyCode>()
    private val activeButtons = mutableSetOf<MouseButton>()

    fun bindEngine(viewport: ImageView, canvasWidth: Int, canvasHeight: Int) {
        this.width = canvasWidth
        this.height = canvasHeight
        pixelData = FloatArray(width * height * 3)
        textureBuffer = MemoryUtil.memAllocFloat(pixelData.size)
        fboReadBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder())
        jfxPixelBuffer = PixelBuffer(width, height, fboReadBuffer, PixelFormat.getByteBgraPreInstance())
        writableImage = WritableImage(jfxPixelBuffer)
        viewport.image = writableImage
        glfwMakeContextCurrent(offscreenWindow)
        GL.createCapabilities()
        setupOpenGL()
        viewport.setOnMousePressed { e ->
            activeButtons.add(e.button)
            onMouseButtonDown(e.button, e.x, e.y)
        }
        viewport.setOnMouseReleased { e ->
            activeButtons.remove(e.button)
            onMouseButtonUp(e.button, e.x, e.y)
        }
        viewport.setOnMouseDragged { e -> onMouseMove(e.x, e.y) }
        viewport.setOnMouseMoved { e -> onMouseMove(e.x, e.y) }
        Platform.runLater {
            viewport.scene.setOnKeyPressed { e ->
                if (activeKeys.add(e.code)) onKeyDown(e.code)
            }
            viewport.scene.setOnKeyReleased { e ->
                activeKeys.remove(e.code)
                onKeyUp(e.code)
            }
        }
        setup()
        var lastTime = System.nanoTime()
        object : AnimationTimer() {
            override fun handle(now: Long) {
                val deltaTime = (now - lastTime) / 1_000_000_000.0f
                lastTime = now
                update(deltaTime)
                renderInternal()
            }
        }.start()
    }

    //API
    protected fun putPixel(x: Int, y: Int, color: Color) {
        if (x < 0 || x >= width || y < 0 || y >= height) return
        val index = (y * width + x) * 3
        pixelData[index] = color.r
        pixelData[index + 1] = color.g
        pixelData[index + 2] = color.b
    }

    protected fun clear(color: Color) {
        var i = 0
        while (i < pixelData.size) {
            pixelData[i] = color.r
            pixelData[i + 1] = color.g
            pixelData[i + 2] = color.b
            i += 3
        }
    }

    protected fun isKeyPressed(key: KeyCode): Boolean = activeKeys.contains(key)
    protected fun isMouseButtonPressed(button: MouseButton): Boolean = activeButtons.contains(button)

    open fun setup() {}
    open fun update(deltaTime: Float) {}
    open fun onKeyDown(key: KeyCode) {}
    open fun onKeyUp(key: KeyCode) {}
    open fun onMouseButtonDown(button: MouseButton, x: Double, y: Double) {}
    open fun onMouseButtonUp(button: MouseButton, x: Double, y: Double) {}
    open fun onMouseMove(x: Double, y: Double) {}

    private fun renderInternal() {
        textureBuffer.clear()
        textureBuffer.put(pixelData)
        textureBuffer.flip()
        glBindTexture(GL_TEXTURE_2D, canvasTextureID)
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGB, GL_FLOAT, textureBuffer)
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glViewport(0, 0, width, height)
        glClear(GL_COLOR_BUFFER_BIT)
        glUseProgram(shaderProgram)
        glBindVertexArray(vao)
        glBindTexture(GL_TEXTURE_2D, canvasTextureID) // Leemos del canvas
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0)
        glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, fboReadBuffer)
        jfxPixelBuffer.updateBuffer { null }
    }

    private fun setupOpenGL() {
        val vertexSrc = """
            #version 330 core
            layout (location = 0) in vec2 aPos;
            layout (location = 1) in vec2 aTexCoord;
            out vec2 TexCoord;
            void main() {
                gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
                TexCoord = vec2(aTexCoord.x, aTexCoord.y); 
            }
        """.trimIndent()
        val fragmentSrc = """
            #version 330 core
            out vec4 FragColor;
            in vec2 TexCoord;
            uniform sampler2D screenTex;
            void main() {
                FragColor = texture(screenTex, TexCoord);
            }
        """.trimIndent()
        val vs = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vs, vertexSrc)
        glCompileShader(vs)
        val fs = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fs, fragmentSrc)
        glCompileShader(fs)
        shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vs)
        glAttachShader(shaderProgram, fs)
        glLinkProgram(shaderProgram)
        canvasTextureID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, canvasTextureID)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_FLOAT, MemoryUtil.NULL)
        renderTargetID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, renderTargetID)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, MemoryUtil.NULL)
        fbo = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderTargetID, 0)
        val vertices = floatArrayOf(
            1.0f,  1.0f,  1.0f, 1.0f,
            1.0f, -1.0f,  1.0f, 0.0f,
            -1.0f, -1.0f,  0.0f, 0.0f,
            -1.0f,  1.0f,  0.0f, 1.0f
        )
        val indices = intArrayOf(0, 1, 3, 1, 2, 3)

        vao = glGenVertexArrays()
        vbo = glGenBuffers()
        ebo = glGenBuffers()
        MemoryStack.stackPush().use { stack ->
            val vBuffer = stack.mallocFloat(vertices.size).put(vertices).flip()
            val iBuffer = stack.mallocInt(indices.size).put(indices).flip()
            glBindVertexArray(vao)
            glBindBuffer(GL_ARRAY_BUFFER, vbo)
            glBufferData(GL_ARRAY_BUFFER, vBuffer, GL_STATIC_DRAW)
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, iBuffer, GL_STATIC_DRAW)
        }
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0L)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 2 * 4L)
        glEnableVertexAttribArray(1)
    }
}