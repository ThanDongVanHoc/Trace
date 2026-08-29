# Thành viên 1 — One-shot Enrollment

## Mục tiêu

Từ một ảnh, vùng người dùng khoanh và tag, tạo hồ sơ nhận dạng có thể dùng trực tiếp bởi module Recognition.

## Code sở hữu

`apps/mobile/lib/features/enrollment/**`

## API phải implement

`EnrollmentApi.enroll(EnrollRequest) -> TraceResult<EnrollResponse>`

Không thay đổi DTO trong `core/contracts` nếu chưa được cả nhóm duyệt.

## Dependency được dùng

- `VisualEncoder`
- `ObjectStore`
- `SecureAssetStore`

Không đọc database và không tự mã hóa file.

## Bảng liên quan

- `local_objects`
- `local_object_references`
- `local_reference_embeddings`
- `secure_assets`

Chỉ truy cập qua Store interface.

## Yêu cầu

- Kiểm tra tag và ROI chuẩn hóa `0..1`.
- Crop đúng ROI sau khi xử lý rotation.
- Từ chối ảnh mờ, quá tối hoặc ROI quá nhỏ.
- Sinh các augmentation hợp lệ từ ảnh gốc.
- Gọi `VisualEncoder` và lưu model name/version.
- Lưu reference và asset theo một transaction logic; lỗi phải rollback asset.

## Nghiệm thu

- Ảnh + ROI + tag hợp lệ trả về `objectId` và `referenceId`.
- Crop sai lệch không quá 3 px so với ROI kỳ vọng trong unit test.
- Ảnh lỗi trả đúng `TraceErrorCode` và không tạo dữ liệu rác.
- Có unit test cho valid ROI, invalid ROI, ảnh rỗng và rollback.
