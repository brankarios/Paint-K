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

    var isAntialiasingEnabled = false

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
    fun putPixel(x: Int, y: Int, color: Color, alpha: Float = 1.0f) {
        if (x < 0 || x >= width || y < 0 || y >= height) return
        val index = (y * width + x) * 3
        
        if (alpha >= 1.0f) {
            pixelData[index] = color.r
            pixelData[index + 1] = color.g
            pixelData[index + 2] = color.b
        } else {
            // Alpha Blending
            val bgR = pixelData[index]
            val bgG = pixelData[index + 1]
            val bgB = pixelData[index + 2]
            pixelData[index] = color.r * alpha + bgR * (1.0f - alpha)
            pixelData[index + 1] = color.g * alpha + bgG * (1.0f - alpha)
            pixelData[index + 2] = color.b * alpha + bgB * (1.0f - alpha)
        }
    }

    fun drawLine(x0: Int, y0: Int, x1: Int, y1: Int, color: Color) {
        if (isAntialiasingEnabled) {
            drawLineWu(x0, y0, x1, y1, color)
        } else {
            drawLineBresenham(x0, y0, x1, y1, color)
        }
    }

    private fun drawLineBresenham(startX: Int, startY: Int, endX: Int, endY: Int, color: Color) {
        var x = startX; var y = startY
        val dx = kotlin.math.abs(endX - startX); val dy = kotlin.math.abs(endY - startY)
        val sx = if (startX < endX) 1 else -1; val sy = if (startY < endY) 1 else -1
        var err = (if (dx > dy) dx else -dy) / 2
        while (true) {
            putPixel(x, y, color)
            if (x == endX && y == endY) break
            val e2 = err
            if (e2 > -dx) { err -= dy; x += sx }
            if (e2 < dy) { err += dx; y += sy }
        }
    }

    private fun drawLineWu(startX: Int, startY: Int, endX: Int, endY: Int, color: Color) {
        var x0 = startX; var y0 = startY; var x1 = endX; var y1 = endY
        val steep = kotlin.math.abs(y1 - y0) > kotlin.math.abs(x1 - x0)
        
        if (steep) {
            var tmp = x0; x0 = y0; y0 = tmp
            tmp = x1; x1 = y1; y1 = tmp
        }
        if (x0 > x1) {
            var tmp = x0; x0 = x1; x1 = tmp
            tmp = y0; y0 = y1; y1 = tmp
        }

        val dx = x1 - x0
        val dy = y1 - y0
        
        // Aritmética de punto fijo (Fixed Point) - 8 bits de fracción (16.8)
        val gradient = if (dx == 0) (1 shl 8) else ((dy shl 8) / dx)
        var yAcc = y0 shl 8

        for (x in x0..x1) {
            val yPix = yAcc shr 8
            val fraction = yAcc and 255
            
            val alpha2 = fraction / 255.0f
            val alpha1 = 1.0f - alpha2

            if (steep) {
                putPixel(yPix, x, color, alpha1)
                putPixel(yPix + 1, x, color, alpha2)
            } else {
                putPixel(x, yPix, color, alpha1)
                putPixel(x, yPix + 1, color, alpha2)
            }
            
            yAcc += gradient
        }
    }

    fun clear(color: Color) {
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