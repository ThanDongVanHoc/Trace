# Tóm tắt Công việc Thành viên 1: One-shot Enrollment

Tài liệu này tóm tắt ngắn gọn các file đã triển khai, vai trò của từng file và cách sử dụng để các thành viên khác trong team dễ dàng nắm bắt và tích hợp.

---

## 1. Danh sách file & Chức năng

| File | Loại | Vai trò / Chức năng |
|---|---|---|
| [`playground/data/models/mobilenet_v3_small.onnx`](file:///c:/Users/HP/OneDrive%20-%20VNU-HCMUS/Desktop/HCMUS/MOBILE/final_project/Trace/playground/data/models/mobilenet_v3_small.onnx) | **Mới** | Model AI MobileNetV3-Small (3.55 MB). Trích xuất đặc trưng ảnh thành vector 576 chiều (bỏ lớp phân loại). |
| [`playground/member1-enrollment/build.gradle.kts`](file:///c:/Users/HP/OneDrive%20-%20VNU-HCMUS/Desktop/HCMUS/MOBILE/final_project/Trace/playground/member1-enrollment/build.gradle.kts) | **Sửa** | Thêm thư viện `com.microsoft.onnxruntime:onnxruntime:1.18.0`. |
| [`EnrollmentAlgorithm.kt`](file:///c:/Users/HP/OneDrive%20-%20VNU-HCMUS/Desktop/HCMUS/MOBILE/final_project/Trace/playground/member1-enrollment/src/main/kotlin/com/trace/playground/enrollment/EnrollmentAlgorithm.kt) | **Sửa** | **Lõi thuật toán Enrollment:** Giải mã ảnh, xoay góc, cắt ROI, lọc chất lượng ảnh, đưa về tensor chuẩn ImageNet, chạy model ONNX và chuẩn hóa L2 vector 576 chiều. |
| [`EnrollmentAlgorithmTest.kt`](file:///c:/Users/HP/OneDrive%20-%20VNU-HCMUS/Desktop/HCMUS/MOBILE/final_project/Trace/playground/member1-enrollment/src/test/kotlin/com/trace/playground/enrollment/EnrollmentAlgorithmTest.kt) | **Sửa** | Bộ Unit test kiểm tra: kích thước vector 576 chiều, chuẩn hóa L2, tính nhất quán (deterministic), từ chối ảnh đen/trắng/quá nhỏ. |
| [`EnrollmentIntegrationTest.kt`](file:///c:/Users/HP/OneDrive%20-%20VNU-HCMUS/Desktop/HCMUS/MOBILE/final_project/Trace/playground/member1-enrollment/src/test/kotlin/com/trace/playground/enrollment/EnrollmentIntegrationTest.kt) | **Mới** | Test tích hợp với 2 ảnh mẫu thực tế: `enroll_mug.jpg` và `retrieval_scene.jpg`. |
| [`playground/data/trace-dev.db`](file:///c:/Users/HP/OneDrive%20-%20VNU-HCMUS/Desktop/HCMUS/MOBILE/final_project/Trace/playground/data/trace-dev.db) | **Cập nhật** | SQLite database đã được enroll sẵn vật thể **"Ly cà phê"** (vector 576 float) làm dữ liệu mẫu cho Thành viên 2 thử nghiệm. |
| [`docs/team/MEMBER_2_INTEGRATION_GUIDE.md`](file:///c:/Users/HP/OneDrive%20-%20VNU-HCMUS/Desktop/HCMUS/MOBILE/final_project/Trace/docs/team/MEMBER_2_INTEGRATION_GUIDE.md) | **Mới** | Hướng dẫn chi tiết cho Thành viên 2 (Recognition): thông số model, cách so khớp Cosine Similarity và code mẫu. |

---

## 2. Thông số kỹ thuật nhanh

- **Model AI:** MobileNetV3-Small (`.onnx`), chạy trên **ONNX Runtime** (không cần cài Python/PyTorch).
- **Kích thước ảnh vào:** `224 x 224` (nội suy Bilinear).
- **Chuẩn hóa pixel:** ImageNet Mean `[0.485, 0.456, 0.406]`, Std `[0.229, 0.224, 0.225]`.
- **Output:** Đúng **1 vector 576 chiều**, đã chuẩn hóa L2 (`||v|| = 1.0`).
- **Metadata:** `modelName = "mobilenet-v3-small"`, `modelVersion = "1"`.

---

## 3. Cách chạy thử nghiệm

### Chạy kiểm thử tự động
```cmd
.\test.bat member1
```
*(Kết quả: 7/7 test cases Passed).*

### Chạy Dev-Server giao diện Swagger
```cmd
.\dev.bat
```
Truy cập `http://localhost:8080/docs` để gọi thử API:
- `POST /dev/enrollments`: Đăng ký đồ vật mới.
- `POST /dev/recognitions`: Nhận diện đồ vật.

---

## 4. Dành cho các thành viên khác khi tái sử dụng

1. **Thành viên 2 (Recognition):**
   - Đọc kỹ file [`docs/team/MEMBER_2_INTEGRATION_GUIDE.md`](file:///c:/Users/HP/OneDrive%20-%20VNU-HCMUS/Desktop/HCMUS/MOBILE/final_project/Trace/docs/team/MEMBER_2_INTEGRATION_GUIDE.md).
   - Dùng chung file model tại `playground/data/models/mobilenet_v3_small.onnx`.
   - Vì vector đã L2-normalize, tính Cosine Similarity chỉ cần nhân vô hướng (Dot Product).
   - Đã có sẵn dữ liệu "Ly cà phê" trong SQLite để test với `retrieval_scene.jpg`.

2. **Khi đưa lên Android App:**
   - Thư viện: Thay `onnxruntime` bằng `onnxruntime-android:1.18.0`.
   - File model: Copy `mobilenet_v3_small.onnx` vào thư mục `app/src/main/assets/`.
   - Tiền xử lý ảnh: Thay hàm lấy pixel `BufferedImage.getRGB` bằng `Bitmap.getPixels()`. Toàn bộ code suy luận ONNX và chuẩn hóa L2 giữ nguyên 100%.
