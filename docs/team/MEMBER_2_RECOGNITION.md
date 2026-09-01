# Thành viên 2 — One-shot Recognition

## Bài toán

Nhận một JPEG mới, phát hiện các đồ vật và so khớp với reference đã enroll. Kết quả
phải cho phép `UNKNOWN`; không ép mọi ảnh phải khớp một đồ vật đã biết.

## Phạm vi

- Branch: `feature/kotlin-recognition`
- Chỉ sửa: `playground/member2-recognition/**`
- Implementation: `RecognitionAlgorithm.kt`
- API thử: `POST /dev/recognitions`
- Test: `test.bat member2`

## Nghiệm thu

- Pipeline có detection, preprocessing, embedding và similarity matching rõ ràng.
- Kiểm tra model version/dimension trước khi so khớp.
- Dataset khóa: ít nhất 10 đồ vật, 5 ảnh test/object và 20 ảnh unknown.
- Precision `>= 85%`, recall `>= 75%`, false-positive unknown `<= 10%`.
- Unit test cosine, threshold, model mismatch, ảnh hỏng và `UNKNOWN`.
- Không sửa HTTP, SQLite hay module của thành viên khác.
