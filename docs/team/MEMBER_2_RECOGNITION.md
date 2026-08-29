# Thành viên 2 — One-shot Recognition

## Mục tiêu

Tìm đồ vật đã đăng ký trong frame mới và trả về `MATCHED` hoặc `UNKNOWN`, bounding box và similarity.

## Code sở hữu

`apps/mobile/lib/features/recognition/**` và `apps/mobile/assets/models/**`

## API phải implement

- `VisualEncoder.encode(ImageInput, roi) -> TraceResult<VisualEmbedding>`
- `RecognitionApi.recognize(RecognizeRequest) -> TraceResult<RecognizeResponse>`

Thay hoàn toàn `PrototypeVisualEngine`; không thay contract.

## Dữ liệu được đọc

`RecognizeRequest.references`. Không truy cập database, location hoặc backend.

## Yêu cầu

- Chạy model bằng TensorFlow Lite ngoài UI isolate.
- Tìm candidate region, tạo embedding và so khớp reference.
- L2-normalize embedding; khóa model name/version và dimensions.
- Có threshold từ chối `UNKNOWN`.
- Không ép mọi ảnh về object gần nhất.
- Bounding box dùng tọa độ chuẩn hóa `0..1`.

## Nghiệm thu

- Dataset: tối thiểu 10 đồ vật, 5 ảnh test/đồ vật và 20 ảnh unknown.
- Precision `>= 85%`, recall `>= 75%` trên dataset đã khóa.
- False positive của tập unknown `<= 10%`.
- Median processing time `<= 1.5 giây/ảnh` trên thiết bị test của nhóm.
- Có report CSV và unit test cho cosine, threshold, model mismatch.
