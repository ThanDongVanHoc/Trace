# Thành viên 1 — One-shot Enrollment API

## Mục tiêu

Nhận đúng một JPEG, ROI người dùng khoanh và tag qua HTTP; kiểm tra chất lượng,
crop/augmentation và tạo các embedding tương thích cho một object reference.

## Phạm vi code

- Branch: `feature/api-enrollment`
- Sở hữu: `services/api/src/enrollments/**`
- Không sửa auth, object API, Android, database hoặc HTTP contract đã chốt.

## Chạy khi phát triển

```powershell
Set-Location services/api
npm ci
npm run start:dev
```

Docker Desktop phải đang chạy. Lệnh trên tự tạo `.env`, bật PostgreSQL và chạy
NestJS watch mode. Mở `http://localhost:3000/docs` để gửi request.

## HTTP contract

```text
POST /v1/enrollments
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

Input: `image` JPEG, `tag`, `roiLeft`, `roiTop`, `roiRight`, `roiBottom`, và
`rotationDegrees` tùy chọn. Output: `objectId`, `referenceId`, `qualityScore`,
`embeddingCount`, `warnings`.

Controller, upload, validation, Swagger và lưu object metadata đã được dựng sẵn.
Chỉ thay `PrototypeEnrollmentEngine` bằng engine thật qua interface
`EnrollmentEngine`; không đổi controller/DTO để tránh phá tích hợp.

## Yêu cầu kỹ thuật

- Chuẩn hóa orientation rồi crop; lệch tối đa 3 px so với ROI kỳ vọng.
- Từ chối ảnh rỗng, quá tối, quá mờ hoặc ROI không đạt bằng HTTP `400` rõ ràng.
- Tạo 3–5 augmentation nhẹ từ đúng một ảnh; không lật nếu làm đổi đặc trưng.
- Mọi embedding phải cùng model name, version và dimensions.
- Không ghi ảnh, embedding hoặc tag vào log.
- Không giữ ảnh upload trên disk; xử lý từ buffer trong request.

## Test và nghiệm thu

- Unit test: rotation/crop, từng quality rejection, augmentation và compatibility.
- HTTP test: multipart hợp lệ, thiếu ảnh, sai MIME, ROI sai và file quá 10 MiB.
- Swagger/Postman gửi một request hợp lệ và nhận HTTP `201`.
- Chạy xanh:

```powershell
npm test -- enrollments
npm run lint
npm run build
```
