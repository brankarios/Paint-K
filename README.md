# 2D Graphics Rendering Engine (Kotlin)

A low-level, high-performance 2D graphics rendering engine built from scratch in Kotlin and JavaFX. This project demonstrates core computer graphics concepts without relying on modern high-level rendering libraries (like JavaFX's built-in `Canvas` shapes). Instead, it manipulates a raw 1D pixel array using custom mathematics and an OpenGL backend proxy.

## Features

- **Custom Rasterization:** Direct manipulation of pixel data (`FloatArray`) mapping to an OpenGL framebuffer.
- **Mathematical Primitives:** Drawing logic for Lines, Rectangles, Circles, Triangles, and N-degree Bézier curves.
- **Xiaolin Wu's Antialiasing:** Implements Wu's line algorithm with 8-bit fixed-point arithmetic optimization for real-time edge smoothing via alpha blending.
- **Spatial Partitioning:** Features a custom QuadTree data structure (Capacity: 4) to optimize collision detection and point-selection queries in $O(\log n)$ time.
- **Interactive UI:** Full desktop interface to draw, select, move, and scale primitives.
- **Clipboard System:** Copy, Cut, and Paste geometry preserving relative center coordinates.
- **State Management (History):** Deep-clone based Undo/Redo stacks (`Ctrl+Z`, `Ctrl+Y`).
- **File Persistence:** Custom lightweight serialization and deserialization engine to save and load the canvas state to `.paint` text files.
- **Fill Algorithms:** Custom Scan-Line and modified Bresenham polygon filling capabilities.

## Mathematical Algorithms Implemented

1. **Bresenham's Line Algorithm:** Used as the fallback discrete line rendering technique. Eliminates floating-point multiplication in the inner loop using an error accumulator.
2. **Xiaolin Wu's Line Algorithm:** Handles high-fidelity antialiasing using light energy conservation and an 8-bit fraction bitwise-shift trick to blend adjacent pixels.
3. **De Casteljau's Algorithm:** Used for rendering Bézier curves recursively. The program supports dynamic degree elevation.
4. **Midpoint Circle Algorithm (Bresenham's Circle):** Generates rasterized circles utilizing 8-way octant symmetry.
5. **Barycentric Scaling:** Scaling matrix transformations computed relative to the calculated geometric center (barycenter) of the polygon to prevent spatial translation during scaling operations.
6. **Painter's Algorithm:** Z-index rendering technique that dictates the overlapping layer priority of drawn primitives.

## Technologies Used

- **Language:** Kotlin
- **UI Framework:** JavaFX (via FXML)
- **Low-Level Bridge:** LWJGL (Lightweight Java Game Library)
- **Build Tool:** Maven

## How to Build and Run

There are two ways to run the project depending on your needs.

### Option A: Fast Run (For Development)
If you want to compile and execute the project immediately in a single command, run:
```bash
mvn clean compile exec:java
```

### Option B: Build Standalone JAR (For Distribution)
This project can be packaged into a standalone "Fat JAR" containing all necessary JavaFX components.

1. Ensure you have **Java 17+** and **Maven** installed.
2. Open a terminal in the project root folder.
3. Build the standalone JAR file by running:
   ```bash
   mvn clean package
   ```
4. Navigate to the generated `target` directory and execute the JAR:
   ```bash
   cd target
   java -jar proyecto1-K-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```
