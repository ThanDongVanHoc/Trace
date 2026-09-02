# Member 2 Summary: One-Shot Recognition (Dual-Pipeline)

This document provides a concise summary of the implemented files, the dual-pipeline architecture, technical specifications, empirical test results, and integration guidelines for other team members.

---

## 1. File List & Responsibilities

| File | Type | Role / Functionality |
|---|---|---|
| [`playground/data/models/ssd_mobilenet_v2.onnx`](file:///home/phongcs/Project/Trace/playground/data/models/ssd_mobilenet_v2.onnx) | **New** | MobileNet SSD v2 AI model (69.6 MB, ONNX opset 13) with integrated NMS post-processing. Detects bounding boxes of objects in scene images (input `[1, 300, 300, 3]` uint8). |
| [`playground/data/models/convert_ssd_v2.py`](file:///home/phongcs/Project/Trace/playground/data/models/convert_ssd_v2.py) | **New** | Python script to automatically download the official TensorFlow Model Zoo checkpoint and convert it to ONNX with built-in post-processing nodes. |
| [`playground/member2-recognition/build.gradle.kts`](file:///home/phongcs/Project/Trace/playground/member2-recognition/build.gradle.kts) | **Modified** | Added dependencies `com.microsoft.onnxruntime:onnxruntime:1.18.0` and `kotlinx-coroutines-core:1.10.2`. |
| [`RecognitionAlgorithm.kt`](file:///home/phongcs/Project/Trace/playground/member2-recognition/src/main/kotlin/com/trace/playground/recognition/RecognitionAlgorithm.kt) | **Modified** | **Core Recognition Engine (Dual-Pipeline):** Manages two parallel ONNX sessions (SSD v2 object detector + MobileNetV3-Small feature extractor), bounding box crop with 10% context padding, cosine similarity via dot product, model/dimension validation, and result merging & deduplication. |
| [`RecognitionAlgorithmTest.kt`](file:///home/phongcs/Project/Trace/playground/member2-recognition/src/test/kotlin/com/trace/playground/recognition/RecognitionAlgorithmTest.kt) | **Modified** | 18 unit tests verifying: dot product, L2 normalization, 576-dim embeddings, SSD v2 object detection, padded crop, rejection of corrupt images / invalid limits, model/dim mismatch filtering, and UNKNOWN fallback. |
| [`RecognitionIntegrationTest.kt`](file:///home/phongcs/Project/Trace/playground/member2-recognition/src/test/kotlin/com/trace/playground/recognition/RecognitionIntegrationTest.kt) | **New** | End-to-end integration test with real data: successfully recognizes the enrolled "Coffee Mug" (from `enroll_mug.jpg`) in the cluttered desk scene of `retrieval_scene.jpg`. |

---

## 2. Dual-Pipeline Architecture & Technical Specifications

### Processing Flow

```text
Input Image (Scene JPEG)
   │
   ├─► Pipeline A (Full-Image Embedding):
   │   └─► Resize 224x224 (Bilinear) ─► ImageNet Normalization
   │       ─► MobileNetV3-Small ─► L2 Normalization (576-dim)
   │       ─► Dot Product with Reference Vectors from DB
   │
   └─► Pipeline B (SSD Object Detection & Crop):
       └─► Resize 300x300 (Bilinear) ─► Format NHWC uint8 [1, 300, 300, 3]
           ─► SSD MobileNet v2 ─► Post-processed Detection Boxes & Scores
           ─► Filter candidate boxes with confidence >= 0.30
           ─► Crop candidate regions with 10% contextual padding
           ─► Feed each crop into MobileNetV3-Small (576-dim)
           ─► Dot Product with Reference Vectors from DB
   │
   ▼
Merge & Deduplication:
   - Combine detections from both pipelines and sort by similarity descending.
   - Deduplicate by objectId: retain only the highest-similarity detection per object.
   - Assign MATCHED (with precise bounding box) or UNKNOWN if below minimumSimilarity.
```

### Technical Specifications of the Two AI Models

| Parameter | Model 1: SSD MobileNet v2 (Detection) | Model 2: MobileNetV3-Small (Embedding) |
|---|---|---|
| **Purpose** | Detect object bounding boxes in scenes | Extract dense feature vector for matching |
| **Model Path** | `playground/data/models/ssd_mobilenet_v2.onnx` | `playground/data/models/mobilenet_v3_small.onnx` |
| **Input Shape** | `[1, 300, 300, 3]` NHWC | `[1, 3, 224, 224]` NCHW |
| **Input Data Type** | `uint8` (`0 .. 255`) | `float32` (ImageNet normalized) |
| **Output** | `detection_boxes` `[1, 100, 4]`<br>`detection_scores` `[1, 100]`<br>`num_detections` `[1]` | `[1, 576]` Float32 (L2 normalized, `\|\|v\|\| = 1.0`) |
| **Metadata** | `opset = 13` | `modelName = "mobilenet-v3-small"`, `modelVersion = "1"` |

---

## 3. Empirical Test Results

### A. Real-World Sample Verification
Recognizing the enrolled object **"Ly cà phê" (Coffee Mug)** (from `enroll_mug.jpg`) in the complex desk scene `retrieval_scene.jpg`:

- **Status:** `MatchStatus.MATCHED` with tag `"Ly cà phê"`.
- **Cosine Similarity:** **`78.4%`** (`0.7842`), vastly outperforming the ~35% baseline similarity achieved by full-image embedding alone.
- **Bounding Box Localization:** `Roi(left=0.133, top=0.428, right=0.255, bottom=0.589)` (precisely bounding the yellow mug on the left side of the desk).
- **Processing Time:** **`202 ms`** for the entire pipeline (decode + detection + crop + embedding extraction + similarity matching).

### B. Automated Test Suite
- **19/19 test cases Passed** (18 unit tests in `RecognitionAlgorithmTest` + 1 integration test in `RecognitionIntegrationTest`).

---

## 4. How to Run & Verify

### A. Automated Tests

**On Linux:**
```bash
~/.gradle/wrapper/dists/gradle-9.4.1-bin/*/gradle-9.4.1/bin/gradle -p playground :member2-recognition:test
# Or using the repository wrapper:
apps/android/gradlew -p playground :member2-recognition:test
```

**On Windows:**
```cmd
.\test.bat member2
```

### B. Interactive Dev-Server (Swagger UI)
Start the server:
```bash
~/.gradle/wrapper/dists/gradle-9.4.1-bin/*/gradle-9.4.1/bin/gradle -p playground :dev-server:run
```
1. Open your browser: `http://localhost:8080/docs`
2. Invoke `POST /dev/recognitions`:
   - Upload `playground/data/samples/retrieval_scene.jpg`
   - Set `minimumSimilarity`: `0.30` (or `0.75`)
   - Verify the JSON response: contains detection `tag = "Ly cà phê"`, `status = "MATCHED"`, `similarity ~ 0.78` along with the mug's `boundingBox`.

---

## 5. Guidelines for Team Integration & Android Migration

1. **For Member 3 (Memory Module):**
   - The `POST /dev/recognitions` API returns a list of `Detection` items containing `objectId`, `tag`, `similarity`, `status`, and `boundingBox`.
   - When `status == MATCHED`, the integration layer extracts `objectId` and `similarity` (as confidence) to call `MemoryApi.recordSighting()`.

2. **When Migrating to Android (`apps/android/feature/recognition`):**
   - **Dependency:** Replace `onnxruntime` with `com.microsoft.onnxruntime:onnxruntime-android:1.18.0`.
   - **Model Assets:** Place both `mobilenet_v3_small.onnx` and `ssd_mobilenet_v2.onnx` into `app/src/main/assets/`.
   - **Bitmap Preprocessing:**
     - For SSD v2: Extract RGB byte array from `Bitmap` scaled to $300 \times 300$.
     - For MobileNetV3: Extract pixel floats into an ImageNet-normalized FloatBuffer CHW $224 \times 224$.
   - **100% Reusable Code:**
     - All ONNX session execution logic (`OrtSession.run()`).
     - Bounding box extraction and 10% context padded crop calculation.
     - `dotProduct()` and `l2Normalize()` functions.
     - `mergeDetections()` deduplication logic.
