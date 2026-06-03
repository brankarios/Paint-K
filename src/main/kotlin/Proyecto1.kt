import javafx.fxml.FXML
import javafx.scene.control.ColorPicker
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton

enum class Tool {
    SELECT,
    LINE,
    RECTANGLE,
    CIRCLE,
    TRIANGLE,
    BEZIER
}

class Proyecto1 : Engine2D() {

    @FXML private lateinit var viewport: ImageView
    @FXML private lateinit var fxColorPicker: ColorPicker
    @FXML private lateinit var btnSelect: javafx.scene.control.Button
    @FXML private lateinit var btnLine: javafx.scene.control.Button
    @FXML private lateinit var btnRectangle: javafx.scene.control.Button
    @FXML private lateinit var btnCircle: javafx.scene.control.Button
    @FXML private lateinit var btnTriangle: javafx.scene.control.Button
    @FXML private lateinit var btnBezier: javafx.scene.control.Button
    @FXML private lateinit var btnFill: javafx.scene.control.Button
    
    // Variables de estado del Proyecto
    private var currentColor = Color.RED
    private var isDrawing = false
    private var currentTool = Tool.LINE 
    private var isFilled = false
    
    private var showQuadTree = false
    private var selectedShape: Shape? = null
    private var selectedControlPointIndex: Int? = null
    private var quadTree: QuadTree = QuadTree(BoundingBox(0.0, 0.0, 1024.0, 600.0))
    
    private val shapes = mutableListOf<Shape>()
    
    // Variable temporal para guardar la figura que estamos trazando actualmente con el mouse
    private var currentShape: Shape? = null

    // Variables para recordar la posición real del mouse y aplicar la restricción si se pulsa Ctrl sin moverlo
    private var lastMouseX = 0
    private var lastMouseY = 0
    
    // Rastreador de tiempo para detectar Doble Clic en Bézier
    private var lastClickTime: Long = 0

    // Estado especial para el Triángulo (requiere 3 vértices)
    private var triangleStep = 0
    @FXML
    fun initialize() {
        bindEngine(viewport, 1024, 600)
        
        // Configuramos el selector de color para que cambie nuestro "currentColor"
        fxColorPicker.setOnAction {
            val jfxColor = fxColorPicker.value
            currentColor = Color(jfxColor.red.toFloat(), jfxColor.green.toFloat(), jfxColor.blue.toFloat())
        }
        updateActiveButtonUI()
    }

    private fun updateActiveButtonUI() {
        val defaultStyle = "-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-cursor: hand;"
        val activeStyle = "-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"
        
        if (!::btnLine.isInitialized) return
        
        btnSelect.style = if (currentTool == Tool.SELECT) activeStyle else defaultStyle
        btnLine.style = if (currentTool == Tool.LINE) activeStyle else defaultStyle
        btnRectangle.style = if (currentTool == Tool.RECTANGLE) activeStyle else defaultStyle
        btnCircle.style = if (currentTool == Tool.CIRCLE) activeStyle else defaultStyle
        btnTriangle.style = if (currentTool == Tool.TRIANGLE) activeStyle else defaultStyle
        btnBezier.style = if (currentTool == Tool.BEZIER) activeStyle else defaultStyle
        
        btnFill.style = if (isFilled) activeStyle else defaultStyle
    }

    override fun setup() {
        // Setup is called once. Clearing is now handled in update() per frame.
    }

    // --- MÉTODOS DE LA INTERFAZ GRÁFICA (@FXML) ---
    @FXML fun setToolSelect() { currentTool = Tool.SELECT; currentShape = null; triangleStep = 0; updateActiveButtonUI(); println("Herramienta: SELECCIONAR") }
    @FXML fun setToolLine() { currentTool = Tool.LINE; currentShape = null; triangleStep = 0; updateActiveButtonUI(); println("Herramienta: LÍNEA") }
    @FXML fun setToolRectangle() { currentTool = Tool.RECTANGLE; currentShape = null; triangleStep = 0; updateActiveButtonUI(); println("Herramienta: RECTÁNGULO") }
    @FXML fun setToolCircle() { currentTool = Tool.CIRCLE; currentShape = null; triangleStep = 0; updateActiveButtonUI(); println("Herramienta: CÍRCULO") }
    @FXML fun setToolTriangle() { currentTool = Tool.TRIANGLE; currentShape = null; triangleStep = 0; updateActiveButtonUI(); println("Herramienta: TRIÁNGULO") }
    @FXML fun setToolBezier() { currentTool = Tool.BEZIER; currentShape = null; triangleStep = 0; updateActiveButtonUI(); println("Herramienta: BÉZIER") }
    @FXML fun toggleFill() { isFilled = !isFilled; updateActiveButtonUI(); println("Modo Relleno: ${if(isFilled) "ON" else "OFF"}") }
    @FXML fun clearCanvas() { shapes.clear(); currentShape = null; triangleStep = 0; selectedShape = null; println("Lienzo limpio") }

    // Este método es el Bucle de Juego (Game Loop), corre automáticamente 60 veces por segundo
    override fun update(deltaTime: Float) {
        // 1. Limpiamos toda la pantalla pintándola de negro
        clear(Color.BACKGROUND)
        
        // 2. Reconstruimos el QuadTree para el estado actual de las figuras
        quadTree.clear()
        for (shape in shapes) {
            quadTree.insert(shape)
        }

        // 3. Dibujamos TODAS las figuras que ya están guardadas en nuestra lista de memoria
        for (shape in shapes) {
            shape.draw(this)
        }
        
        // 4. Dibujamos la figura que estamos arrastrando actualmente (Efecto PREVIEW)
        currentShape?.draw(this)

        // 5. Dibujar caja de selección si hay una figura seleccionada
        selectedShape?.let {
            val b = it.getBounds()
            drawDashedBox(b.x.toInt() - 2, b.y.toInt() - 2, (b.x + b.width).toInt() + 2, (b.y + b.height).toInt() + 2, Color.YELLOW)
            // Si es bezier mostramos sus puntos y líneas guía
            if (it is Bezier) {
                val points = it.controlPoints
                if (points.size > 1) {
                    val lineColor = Color(0.5f, 0.5f, 0.0f) // Verde-amarillento para diferenciarla de la gris base
                    for (i in 0 until points.size - 1) {
                        drawLine(points[i].x.toInt(), points[i].y.toInt(), points[i+1].x.toInt(), points[i+1].y.toInt(), lineColor)
                    }
                }
                for (p in points) {
                    for (dx in -3..3) {
                        for (dy in -3..3) {
                            putPixel(p.x.toInt() + dx, p.y.toInt() + dy, Color.YELLOW)
                        }
                    }
                }
            }
        }

        // 6. Si showQuadTree está activo, dibujamos las fronteras
        if (showQuadTree) {
            quadTree.draw(this)
        }
    }

    private fun drawLine(startX: Int, startY: Int, endX: Int, endY: Int, color: Color) {
        var x = startX
        var y = startY
        val dx = kotlin.math.abs(endX - startX)
        val dy = kotlin.math.abs(endY - startY)
        val sx = if (startX < endX) 1 else -1
        val sy = if (startY < endY) 1 else -1
        var err = (if (dx > dy) dx else -dy) / 2

        while (true) {
            putPixel(x, y, color)
            if (x == endX && y == endY) break
            val e2 = err
            if (e2 > -dx) { err -= dy; x += sx }
            if (e2 < dy) { err += dx; y += sy }
        }
    }

    private fun drawDashedBox(x0: Int, y0: Int, x1: Int, y1: Int, color: Color) {
        // Linea superior
        for (i in x0..x1 step 4) putPixel(i, y0, color)
        // Linea inferior
        for (i in x0..x1 step 4) putPixel(i, y1, color)
        // Linea izquierda
        for (i in y0..y1 step 4) putPixel(x0, i, color)
        // Linea derecha
        for (i in y0..y1 step 4) putPixel(x1, i, color)
    }

    private fun updateCurrentShape() {
        currentShape?.let { shape ->
            var targetX = lastMouseX
            var targetY = lastMouseY

            // Si se oprime Ctrl, forzamos un cuadrado perfecto para Rectángulo y Círculo
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
                is Triangle -> {
                    if (triangleStep == 1) {
                        shape.x1 = targetX
                        shape.y1 = targetY
                        // P2 sigue al mouse para que no se dibuje basura
                        shape.x2 = targetX
                        shape.y2 = targetY
                    } else if (triangleStep == 2) {
                        shape.x2 = targetX
                        shape.y2 = targetY
                    }
                }
                is Bezier -> {
                    // Actualizamos siempre el último punto de la lista para lograr el preview de la curva
                    if (shape.controlPoints.isNotEmpty()) {
                        shape.controlPoints.last().x = targetX.toDouble()
                        shape.controlPoints.last().y = targetY.toDouble()
                    }
                }
            }
        }
    }

    // Manejo de teclado para cambiar de herramienta y utilidades
    override fun onKeyDown(key: KeyCode) {
        when (key) {
            KeyCode.SPACE -> {
                clearCanvas()
            }
            KeyCode.S -> {
                setToolSelect()
            }
            KeyCode.L -> {
                setToolLine()
            }
            KeyCode.R -> {
                currentTool = Tool.RECTANGLE
                currentShape = null
                updateActiveButtonUI()
                println("Herramienta seleccionada: RECTÁNGULO")
            }
            KeyCode.C -> {
                currentTool = Tool.CIRCLE
                currentShape = null
                updateActiveButtonUI()
                println("Herramienta seleccionada: CÍRCULO (ELIPSE)")
            }
            KeyCode.T -> {
                currentTool = Tool.TRIANGLE
                currentShape = null
                triangleStep = 0
                updateActiveButtonUI()
                println("Herramienta seleccionada: TRIÁNGULO")
            }
            KeyCode.B -> {
                currentTool = Tool.BEZIER
                currentShape = null
                updateActiveButtonUI()
                println("Herramienta seleccionada: BÉZIER")
            }
            KeyCode.Q -> {
                showQuadTree = !showQuadTree
                println("QuadTree Visualización: ${if (showQuadTree) "ON" else "OFF"}")
            }
            KeyCode.F -> {
                isFilled = !isFilled
                updateActiveButtonUI()
                println("Modo Relleno alternado a: ${if (isFilled) "ACTIVADO" else "DESACTIVADO"}")
            }
            KeyCode.E -> {
                val shape = selectedShape
                if (shape is Bezier) {
                    shape.elevateDegree()
                    println("Grado de Curva de Bézier elevado! Total puntos de control: ${shape.controlPoints.size}")
                } else {
                    println("Acción inválida: Debe seleccionar una curva de Bézier para elevar su grado.")
                }
            }
            KeyCode.CONTROL -> {
                // Si pulsa Ctrl mientras dibuja, actualizamos la figura al instante
                if (isDrawing || currentShape != null) updateCurrentShape()
            }
            else -> {}
        }
    }

    override fun onKeyUp(key: KeyCode) {
        if (key == KeyCode.CONTROL && (isDrawing || currentShape != null)) {
            // Si suelta Ctrl mientras dibuja, vuelve a la forma libre
            updateCurrentShape()
        }
    }

    // Cuando el usuario hace clic (presiona el botón)
    override fun onMouseButtonDown(button: MouseButton, x: Double, y: Double) {
        val currentTime = System.currentTimeMillis()
        val isDoubleClick = (currentTime - lastClickTime) < 300 // Umbral de 300ms para doble clic
        lastClickTime = currentTime

        if (button == MouseButton.PRIMARY) {
            isDrawing = true
            lastMouseX = x.toInt()
            lastMouseY = y.toInt()
            
            if (currentTool == Tool.SELECT) {
                // Primero verificamos si tocamos un punto de control de una Bézier ya seleccionada
                val shape = selectedShape
                if (shape is Bezier) {
                    for (i in shape.controlPoints.indices) {
                        val p = shape.controlPoints[i]
                        // Damos un margen de clic (hitbox) de 10x10 pixeles (distancia de 5)
                        if (kotlin.math.abs(p.x - x) <= 5 && kotlin.math.abs(p.y - y) <= 5) {
                            selectedControlPointIndex = i
                            println("Agarrando punto de control #$i de la curva")
                            return
                        }
                    }
                }

                // Si no tocamos un punto, consultamos el QuadTree normal para seleccionar otra figura
                val foundShapes = mutableListOf<Shape>()
                quadTree.retrieve(x, y, foundShapes)
                selectedShape = foundShapes.lastOrNull { it.getBounds().contains(x, y) }
                selectedControlPointIndex = null
                println(if (selectedShape != null) "Figura seleccionada!" else "Selección vacía")
                return
            }

            val fillCol = if (isFilled) {
                Color(currentColor.r * 0.4f, currentColor.g * 0.4f, currentColor.b * 0.4f)
            } else {
                null
            }

            if (currentTool == Tool.BEZIER) {
                if (isDoubleClick && currentShape is Bezier) {
                    // Doble clic detectado: Finalizamos la curva de Bézier
                    updateCurrentShape()
                    currentShape?.let { 
                        (it as Bezier).isFinalized = true
                        shapes.add(it) 
                    }
                    currentShape = null
                    isDrawing = false
                    println("Curva de Bézier terminada.")
                } else {
                    if (currentShape !is Bezier) {
                        // Iniciamos una nueva curva de Bézier con dos puntos iniciales (inicio y preview)
                        currentShape = Bezier(mutableListOf(Point2D(x, y), Point2D(x, y)), currentColor)
                    } else {
                        // Agregamos un nuevo punto de control de anclaje, el preview se manejará en el último punto
                        val b = currentShape as Bezier
                        b.controlPoints.add(Point2D(x, y))
                    }
                }
            } else if (currentTool == Tool.TRIANGLE) {
                if (triangleStep == 0) {
                    currentShape = Triangle(lastMouseX, lastMouseY, lastMouseX, lastMouseY, lastMouseX, lastMouseY, currentColor, fillCol)
                    triangleStep = 1
                } else if (triangleStep == 1) {
                    triangleStep = 2
                } else if (triangleStep == 2) {
                    // Tercer punto fijado, terminamos
                    updateCurrentShape()
                    currentShape?.let { shapes.add(it) }
                    currentShape = null
                    triangleStep = 0
                    isDrawing = false
                }
            } else {
                currentShape = when (currentTool) {
                    Tool.LINE -> Line(lastMouseX, lastMouseY, lastMouseX, lastMouseY, currentColor)
                    Tool.RECTANGLE -> Rectangle(lastMouseX, lastMouseY, lastMouseX, lastMouseY, currentColor, fillCol)
                    Tool.CIRCLE -> Circle(lastMouseX, lastMouseY, lastMouseX, lastMouseY, currentColor, fillCol)
                    else -> null
                }
            }
        }
    }

    // Cuando el usuario suelta el clic
    override fun onMouseButtonUp(button: MouseButton, x: Double, y: Double) {
        if (button == MouseButton.PRIMARY) {
            lastMouseX = x.toInt()
            lastMouseY = y.toInt()

            if (currentTool == Tool.SELECT) {
                selectedControlPointIndex = null
                isDrawing = false
                return
            }

            // Hacemos una última actualización
            updateCurrentShape()

            if (currentTool != Tool.TRIANGLE && currentTool != Tool.BEZIER) {
                currentShape?.let { shapes.add(it) }
                currentShape = null
                isDrawing = false
            } else if (currentTool == Tool.TRIANGLE) {
                // Soporte para "Arrastrar" vértices de triángulos
                val t = currentShape as? Triangle
                if (t != null) {
                    if (triangleStep == 1 && (t.x0 != t.x1 || t.y0 != t.y1)) {
                        // Si arrastró el ratón tras el 1er clic, asumimos que soltar el ratón fija el 2do vértice
                        triangleStep = 2
                    } else if (triangleStep == 2 && (t.x1 != t.x2 || t.y1 != t.y2)) {
                        // Si arrastró el ratón tras fijar el 2do, soltar el ratón fija el 3er vértice y termina
                        shapes.add(t)
                        currentShape = null
                        triangleStep = 0
                        isDrawing = false
                    }
                }
            }
        }
    }

    // Cuando el usuario mueve el ratón
    override fun onMouseMove(x: Double, y: Double) {
        lastMouseX = x.toInt()
        lastMouseY = y.toInt()

        if (isDrawing && currentTool == Tool.SELECT) {
            val shape = selectedShape
            val ptIndex = selectedControlPointIndex
            if (shape is Bezier && ptIndex != null) {
                shape.controlPoints[ptIndex].x = x
                shape.controlPoints[ptIndex].y = y
            }
            return
        }

        // Permitimos actualizar aunque isDrawing sea false para el preview dinámico del triángulo sin hacer clic
        if (isDrawing || currentShape != null) {
            updateCurrentShape()
        }
    }
}