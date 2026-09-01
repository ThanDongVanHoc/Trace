# Thành viên 1 — One-shot Enrollment

## Bài toán

Nhận một JPEG, vùng ROI người dùng khoanh và tag. Chuẩn hóa/crop ảnh, đánh giá chất
lượng và sinh representation đủ ổn định để Recognition nhận lại đồ vật từ ảnh sau.

## Phạm vi

- Branch: `feature/kotlin-enrollment`
- Chỉ sửa: `playground/member1-enrollment/**`
- Implementation: `EnrollmentAlgorithm.kt`
- API thử: `POST /dev/enrollments`
- Test: `test.bat member1`

## Nghiệm thu

- Crop đúng ROI sau khi xử lý rotation; sai lệch tối đa 3 px.
- Từ chối ảnh hỏng, quá tối, quá mờ và ROI không đủ thông tin.
- Sinh 3–5 representation/augmentation từ đúng một ảnh gốc.
- Representation có model name, version và dimension nhất quán.
- Unit test bao phủ crop, rotation, quality rejection và output compatibility.
- Không sửa HTTP, SQLite hay module của thành viên khác.
