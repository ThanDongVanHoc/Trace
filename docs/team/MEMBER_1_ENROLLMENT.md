# Thành viên 1 — One-shot Enrollment

## Mục tiêu

Từ đúng một ảnh client chụp, ROI người dùng khoanh và tag, tạo `ObjectReference` mà module Recognition có thể dùng ngay.

## Phạm vi code

- Branch: `feature/android-enrollment`
- Sở hữu: `apps/android/feature/enrollment/**`
- Không sửa Compose UI, database, crypto, Recognition hoặc `core/contracts`.

## API phải implement

```kotlin
suspend fun EnrollmentApi.enroll(request: EnrollRequest): TraceResult<EnrollResponse>
```

Input: `tag`, JPEG `ImageInput`, `NormalizedRect roi`.

Output: `objectId`, `referenceId`, quality, số embedding và warning.

Chỉ được gọi:

- `VisualEncoder.encode(image, roi)`
- `SecureAssetStore.write/delete`
- `ObjectStore.create`

Không đọc Room/DAO trực tiếp.

## Yêu cầu

- Validate tag dài `1..80`, ROI nằm trong `0..1`, diện tích ROI tối thiểu `1%` ảnh.
- Chuẩn hóa orientation trước khi crop; crop lệch tối đa 3 px so với ROI kỳ vọng.
- Tính quality và từ chối ảnh rỗng, quá tối, quá mờ hoặc ROI quá nhỏ bằng đúng `TraceErrorCode`.
- Tạo 3–5 augmentation hợp lệ: crop nhẹ, brightness/contrast nhẹ; không lật nếu làm đổi đặc trưng đồ vật.
- Gọi `VisualEncoder` cho từng ảnh hợp lệ; mọi embedding phải cùng model/version/dimensions.
- Lưu asset trước, sau đó object/reference; nếu bước sau lỗi phải xóa asset đã tạo.
- Không log ảnh, embedding hoặc tag.

## Nghiệm thu

- Ảnh + ROI + tag hợp lệ tạo đúng một object và một reference.
- Invalid input không ghi dữ liệu.
- Lỗi encoder/store không để asset mồ côi.
- Unit test: valid ROI, rotation crop, từng quality rejection và rollback.
- Chạy xanh:

```powershell
.\gradlew.bat :feature:enrollment:testDebugUnitTest
```
