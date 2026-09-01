# Hướng dẫn Tích hợp & Phát triển: Thành viên 2 (One-shot Recognition)

> **Dành cho:** Thành viên phụ trách Module `member2-recognition`  
> **Người biên soạn:** Thành viên 1 (One-shot Enrollment)  
> **Model sử dụng:** MobileNetV3-Small (ONNX Runtime, 576 dimensions)  
> **Dữ liệu mẫu đã nạp:** `enroll_mug.jpg` đã được enroll vào SQLite database `trace-dev.db`

---

## 1. Tổng quan kiến trúc & Hợp đồng giao tiếp (Contract)

Trong luồng hoạt động của hệ thống:
1. **Thành viên 1 (Enrollment):** Cắt ROI của đồ vật từ ảnh đăng ký -> trích xuất vector embedding 576 chiều bằng model `mobilenet_v3_small.onnx` -> chuẩn hóa L2 -> lưu vào SQLite database.
2. **Dev-Server (`Application.kt`):** Khi có request nhận diện `POST /dev/recognitions`, server **tự động đọc toàn bộ vector đã lưu trong database** thông qua `repository.references()` và truyền sẵn vào tham số `request.references: List<ReferenceVector>` cho bạn.
3. **Thành viên 2 (Recognition):** Bạn **KHÔNG CẦN** viết câu lệnh SQL nào. Bạn chỉ cần nhận ảnh cảnh (`retrieval_scene.jpg`), trích xuất vector embedding bằng cùng model `mobilenet_v3_small.onnx`, và tính Cosine Similarity với danh sách `request.references` được cấp sẵn.

---

## 2. Thông số kỹ thuật của Embedding Model

Để vector nhận diện của bạn khớp chính xác với vector đã đăng ký của Thành viên 1, bạn **bắt buộc** phải tuân thủ đúng các thông số sau:

| Thông số | Giá trị chuẩn | Ghi chú |
|---|---|---|
| **Model** | MobileNetV3-Small Feature Extractor | Đã cắt bỏ classification head |
| **Vị trí file model** | `playground/data/models/mobilenet_v3_small.onnx` | Dung lượng ~3.55 MB |
| **Input Shape** | `[1, 3, 224, 224]` Float32 | Thứ tự kênh màu: **CHW** (Channel, Height, Width) |
| **Kích thước ảnh vào** | `224 x 224` | **Phải dùng nội suy Bilinear** (`VALUE_INTERPOLATION_BILINEAR`) |
| **Chuẩn hóa ImageNet** | Mean: `[0.485, 0.456, 0.406]`<br>Std: `[0.229, 0.224, 0.225]` | Công thức: `(pixel / 255.0f - mean) / std` |
| **Output Shape** | `[1, 576]` Float32 | Vector đặc trưng 576 chiều |
| **Chuẩn hóa Vector** | L2 Normalization | Đưa độ dài vector về `||v|| = 1.0` |
| **Metadata nhận diện** | `modelName = "mobilenet-v3-small"`<br>`modelVersion = "1"` | Dùng để lọc đúng model trước khi so khớp |

---

## 3. "Đề bài" thực tế: Dữ liệu mẫu đã lưu sẵn trong Database

Thành viên 1 đã chạy thuật toán và **lưu sẵn 1 đồ vật mẫu vào SQLite database (`playground/data/trace-dev.db`)**:

- **Đồ vật:** `Ly cà phê` (được enroll từ file `playground/data/samples/enroll_mug.jpg`)
- **Object ID:** `eb6345a2-20cd-4c53-a489-89cdb0a6c8cf`
- **Reference ID:** `a2ebfb26-9270-4f34-be3e-3e9e02d71669`
- **Model Name / Version:** `mobilenet-v3-small` / `1`
- **Vector:** 576 số thực float (L2 normalized) lưu trong cột `embedding`
- **Ảnh gốc tham chiếu:** `playground/data/blobs/a2ebfb26-9270-4f34-be3e-3e9e02d71669.jpg`

### 🎯 Mục tiêu của bạn:
Chạy kiểm thử với ảnh cảnh **`playground/data/samples/retrieval_scene.jpg`** (ảnh chụp không gian bàn làm việc có chứa chiếc ly cà phê):
- Khi ảnh query đưa vào có chiếc ly cà phê: Thuật toán của bạn phải so khớp với vector trong DB và trả về `MatchStatus.MATCHED` với `tag = "Ly cà phê"`.
- Khi ảnh query không có đồ vật khớp (hoặc cosine similarity < `minimumSimilarity`): Trả về `MatchStatus.UNKNOWN`.

---

## 4. Hướng dẫn từng bước triển khai trong `playground/member2-recognition`

### Bước 1: Thêm dependency ONNX Runtime vào Gradle
Mở file `playground/member2-recognition/build.gradle.kts` và thêm thư viện:

```kotlin
dependencies {
    implementation(project(":contracts"))
    implementation("com.microsoft.onnxruntime:onnxruntime:1.18.0") // Thêm dòng này
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
```

### Bước 2: Công thức tính Cosine Similarity siêu nhanh
Vì vector đăng ký và vector nhận diện đều đã được **L2-normalize** (độ dài vector = 1.0), công thức Cosine Similarity rút gọn thành **Tích vô hướng (Dot Product)**:

$$\text{Cosine}(A, B) = A \cdot B = \sum_{i=0}^{575} A[i] \times B[i]$$

```kotlin
fun cosineSimilarity(left: FloatArray, right: FloatArray): Float {
    if (left.size != right.size || left.isEmpty()) return 0f
    var dot = 0.0f
    for (i in left.indices) {
        dot += left[i] * right[i]
    }
    return dot.coerceIn(-1f, 1f)
}
```

### Bước 3: Code mẫu tham khảo cho `RecognitionAlgorithm.kt`

Dưới đây là khung code hoàn chỉnh để bạn tham khảo và phát triển:

```kotlin
package com.trace.playground.recognition

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.trace.playground.contracts.Detection
import com.trace.playground.contracts.MatchStatus
import com.trace.playground.contracts.RecognitionEngine
import com.trace.playground.contracts.RecognitionRequest
import com.trace.playground.contracts.RecognitionResult
import com.trace.playground.contracts.Roi
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.FloatBuffer
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

class RecognitionAlgorithm(
    modelPath: Path = resolveDefaultModelPath(),
) : RecognitionEngine, AutoCloseable {

    private val ortEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession = ortEnvironment.createSession(
        modelPath.toAbsolutePath().toString(),
        OrtSession.SessionOptions(),
    )

    override suspend fun recognize(request: RecognitionRequest): RecognitionResult {
        require(request.minimumSimilarity in 0f..1f) { "minimumSimilarity must be between 0 and 1" }
        require(request.maximumResults in 1..50) { "maximumResults must be between 1 and 50" }

        var detections: List<Detection>
        val elapsed = measureTimeMillis {
            val image = ImageIO.read(ByteArrayInputStream(request.image.jpegBytes))
                ?: throw IllegalArgumentException("image must be a valid JPEG")

            // 1. Trích xuất vector cho ảnh query (hoặc các proposals nếu có detector)
            val queryTensor = imageToTensor(image)
            val queryEmbedding = l2Normalize(runOnnxInference(queryTensor))

            // 2. Lọc các reference hợp lệ (đúng model mobilenet-v3-small và 576 chiều)
            val validReferences = request.references.filter {
                it.modelName == MODEL_NAME && it.values.size == EMBEDDING_DIM
            }

            // 3. So khớp Cosine Similarity
            detections = validReferences.map { reference ->
                val similarity = dotProduct(queryEmbedding, reference.values.toFloatArray())
                Detection(
                    objectId = reference.objectId.takeIf { similarity >= request.minimumSimilarity },
                    tag = reference.tag.takeIf { similarity >= request.minimumSimilarity },
                    similarity = similarity,
                    status = if (similarity >= request.minimumSimilarity) {
                        MatchStatus.MATCHED
                    } else {
                        MatchStatus.UNKNOWN
                    },
                    boundingBox = Roi(0f, 0f, 1f, 1f),
                )
            }.sortedByDescending { it.similarity }.take(request.maximumResults)

            if (detections.isEmpty()) {
                detections = listOf(Detection(similarity = 0f, status = MatchStatus.UNKNOWN))
            }
        }

        return RecognitionResult(
            detections = detections,
            processingTimeMillis = elapsed,
            modelVersion = "$MODEL_NAME:$MODEL_VERSION",
            warnings = emptyList(),
        )
    }

    private fun imageToTensor(image: BufferedImage): FloatArray {
        val resized = BufferedImage(224, 224, BufferedImage.TYPE_INT_RGB)
        val g2d = resized.createGraphics()
        g2d.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
        )
        g2d.drawImage(image, 0, 0, 224, 224, null)
        g2d.dispose()

        val tensor = FloatArray(3 * 224 * 224)
        val stride = 224 * 224
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val rgb = resized.getRGB(x, y)
                val idx = y * 224 + x
                val r = ((rgb shr 16) and 0xFF) / 255.0f
                val g = ((rgb shr 8) and 0xFF) / 255.0f
                val b = (rgb and 0xFF) / 255.0f
                tensor[idx] = (r - 0.485f) / 0.229f
                tensor[stride + idx] = (g - 0.456f) / 0.224f
                tensor[2 * stride + idx] = (b - 0.406f) / 0.225f
            }
        }
        return tensor
    }

    private fun runOnnxInference(tensor: FloatArray): FloatArray {
        val shape = longArrayOf(1, 3, 224, 224)
        OnnxTensor.createTensor(ortEnvironment, FloatBuffer.wrap(tensor), shape).use { inputTensor ->
            ortSession.run(mapOf("input" to inputTensor)).use { result ->
                return when (val out = result[0].value) {
                    is Array<*> -> (out[0] as FloatArray)
                    is FloatArray -> out
                    else -> throw IllegalStateException("Unexpected output shape")
                }
            }
        }
    }

    private fun dotProduct(a: FloatArray, b: FloatArray): Float {
        var sum = 0f
        for (i in a.indices) sum += a[i] * b[i]
        return sum.coerceIn(-1f, 1f)
    }

    private fun l2Normalize(vec: FloatArray): FloatArray {
        val norm = sqrt(vec.sumOf { (it * it).toDouble() }).toFloat()
        return if (norm == 0f) vec else FloatArray(vec.size) { vec[it] / norm }
    }

    override fun close() {
        ortSession.close()
        ortEnvironment.close()
    }

    companion object {
        const val MODEL_NAME = "mobilenet-v3-small"
        const val MODEL_VERSION = "1"
        const val EMBEDDING_DIM = 576

        fun resolveDefaultModelPath(): Path {
            val candidates = listOf(
                Path.of("playground", "data", "models", "mobilenet_v3_small.onnx"),
                Path.of("data", "models", "mobilenet_v3_small.onnx"),
                Path.of("..", "data", "models", "mobilenet_v3_small.onnx"),
                Path.of("..", "..", "playground", "data", "models", "mobilenet_v3_small.onnx"),
            )
            return candidates.firstOrNull { it.toFile().exists() }
                ?: throw IllegalStateException("Model not found in candidates: $candidates")
        }
    }
}
```

---

## 5. Kiểm thử & Chạy thử nghiệm (Testing Workflow)

### A. Chạy Unit Test
```cmd
.\test.bat member2
```

### B. Kiểm thử qua Dev-Server (Swagger UI)
1. Khởi động server:
   ```cmd
   .\dev.bat
   ```
2. Truy cập trình duyệt: `http://localhost:8080/docs`
3. Gọi API `POST /dev/recognitions`:
   - Gửi kèm file ảnh `playground/data/samples/retrieval_scene.jpg`
   - `minimumSimilarity`: ví dụ `0.30` (nếu full-image match) hoặc `0.75` (nếu crop ROI chiếc cốc)
   - Kiểm tra kết quả trả về trong Swagger: `detections[0].tag` phải là `"Ly cà phê"` và `status` là `"MATCHED"`.

---

## 6. Ghi chú chuyển đổi sang Android (Migration to Android)

Khi đưa giải pháp này lên ứng dụng Android (`apps/android/feature/recognition`):

1. **Gradle Dependency:**
   Thay `onnxruntime` bằng `onnxruntime-android`:
   ```kotlin
   // apps/android/feature/recognition/build.gradle.kts
   implementation("com.microsoft.onnxruntime:onnxruntime-android:1.18.0")
   ```
2. **File Model:**
   Copy `mobilenet_v3_small.onnx` vào thư mục `apps/android/app/src/main/assets/mobilenet_v3_small.onnx`.
3. **Tiền xử lý ảnh (Bitmap):**
   Thay `BufferedImage.getRGB` bằng `Bitmap.getPixels()` của Android SDK:
   ```kotlin
   val pixels = IntArray(224 * 224)
   val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 224, 224, true)
   scaledBitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)
   ```
4. **Giữ nguyên 100%:**
   - Toàn bộ code chạy inference `OrtSession.run()`
   - Toàn bộ code tính `dotProduct` và `l2Normalize`
   - Kích thước vector 576 chiều và các hằng số ImageNet mean/std
