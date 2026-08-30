# Thành viên 2 — One-shot Recognition API

## Mục tiêu

Nhận một JPEG mới và danh sách reference embedding, sau đó trả `MATCHED` hoặc
`UNKNOWN`. Không xây classifier với danh sách đồ vật cố định và không ép ảnh nào
cũng phải khớp.

## Phạm vi code

- Branch: `feature/api-recognition`
- Sở hữu: `services/api/src/recognitions/**`
- Không sửa auth, Enrollment, Memory, Vault, Android hoặc HTTP contract.

## Chạy khi phát triển

```powershell
Set-Location services/api
npm ci
npm run start:dev
```

Mở `http://localhost:3000/docs`, đăng ký/đăng nhập, bấm **Authorize**, rồi gửi
request trực tiếp vào API của mình. Backend tự reload khi lưu code.

## HTTP contract

```text
POST /v1/recognitions
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

Input: `image` JPEG, `candidates` là JSON array chứa UUID, model name/version và
embedding; thêm `minimumSimilarity`, `maximumResults`, `rotationDegrees` tùy chọn.
Output: detections, processing time, model version và warnings.

Upload, validation và Swagger đã có. Thay `PrototypeRecognitionEngine` bằng engine
thật qua `RecognitionEngine`; không đổi controller/DTO.

## Yêu cầu kỹ thuật

- Decode rotation, resize và normalize đúng metadata model.
- Encode query image; L2-normalize và cosine similarity với candidate embeddings.
- Kiểm tra model name/version/dimensions; xử lý mismatch có kiểm soát.
- Có threshold `UNKNOWN`; detection hợp lệ phải có similarity `0..1`.
- Không log ảnh hoặc embedding; model phải có license cho phép sử dụng.

## Nghiệm thu

- Dataset khóa: ít nhất 10 đồ vật, 5 ảnh test/object và 20 ảnh unknown.
- Precision `>= 85%`, recall `>= 75%`, false-positive unknown `<= 10%`.
- Unit test cosine, L2, threshold, mismatch và invalid image.
- Swagger/Postman gửi request hợp lệ và nhận HTTP `201`.
- Chạy xanh:

```powershell
npm test -- recognitions
npm run lint
npm run build
```
