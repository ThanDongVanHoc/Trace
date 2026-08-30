# Thành viên 2 — One-shot Recognition

## Mục tiêu

Nhận diện một đồ vật bất kỳ đã được người dùng đăng ký từ một ảnh mới. Hệ thống phải trả `MATCHED` hoặc `UNKNOWN`, không phải classifier với danh sách đồ vật cố định.

## Phạm vi code

- Branch: `feature/android-recognition`
- Sở hữu: `apps/android/feature/recognition/**`, model tại `feature/recognition/src/main/assets/models/`
- Thay `PrototypeVisualEngine`; không sửa UI, DB, location, backend hoặc contract.

## API phải implement

```kotlin
suspend fun VisualEncoder.encode(
    image: ImageInput,
    roi: NormalizedRect? = null,
): TraceResult<VisualEmbedding>

suspend fun RecognitionApi.recognize(
    request: RecognizeRequest,
): TraceResult<RecognizeResponse>
```

`RecognizeRequest` chứa ảnh mới và toàn bộ `ObjectReference`; module không tự đọc database.

## Yêu cầu

- Chạy model on-device bằng LiteRT, không block main thread.
- Tiền xử lý phải áp dụng JPEG rotation, resize, normalization đúng metadata model.
- Khi enrollment có ROI: encode đúng crop. Khi recognition không có ROI: sinh candidate region bằng detector tổng quát hoặc chiến lược multi-crop đã đo kiểm.
- L2-normalize embedding và cosine similarity với reference.
- Khóa `modelName`, `modelVersion`, dimensions; model mismatch trả lỗi hoặc bỏ candidate có kiểm soát.
- Có threshold từ chối `UNKNOWN`; tuyệt đối không ép ảnh nào cũng khớp object gần nhất.
- Detection trả bounding box chuẩn hóa `0..1`, similarity `0..1`.
- Model phải có license cho phép phân phối ứng dụng; ghi nguồn và license trong module README.

## Nghiệm thu

- Dataset khóa: tối thiểu 10 đồ vật, 1 ảnh enrollment, 5 ảnh test/object và 20 ảnh unknown.
- Precision `>= 85%`, recall `>= 75%`, false-positive unknown `<= 10%`.
- Median `<= 1.5 giây/ảnh`, peak RAM `<= 350 MB` trên thiết bị nhóm chọn.
- Xuất `evaluation.csv`: sample, expected, predicted, similarity, latency.
- Unit test: cosine, L2, threshold, model mismatch; instrumentation test load/infer model.
- Chạy xanh:

```powershell
.\gradlew.bat :feature:recognition:testDebugUnitTest
```
