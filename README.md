# TRACE — Android on-device

TRACE giúp người dùng gắn tag cho đồ vật bằng một ảnh, nhận diện lại đồ vật trong
những lần chụp sau và xem nơi/thời điểm xuất hiện gần nhất. Bản trong nhánh
`final` là ứng dụng Android độc lập: camera, model ML, database, đăng nhập cục bộ,
mã hóa và thông báo đều chạy trên điện thoại; không cần backend, Docker hay mạng.

## Thành viên

Nguyễn Hoàng Thuận Phát - 24125072
Dương Gia Khương - 24125062
Nguyễn Trọng Hùng Phong - 24125073
Nguyễn Chí Tính - 24125019

## Demo video link

[Link youtube](https://youtube.com)

## Chạy nhanh

Yêu cầu: JDK 21 và Android SDK (API 36, Build Tools 35+). Không bắt buộc Android
Studio.

```powershell
# Build, unit test và Android Lint
.\build-android.bat check

# Tạo APK nộp bài, đã ký bằng debug certificate và chạy trên máy ARM64
.\build-android.bat submission
```

Trên Linux/macOS:

```bash
./build-android.sh check
./build-android.sh submission
```

APK được tạo tại `apk/app-release.apk`. Cần xoá file trước khi build lại để tránh lỗi không ghi đè. Cài vào thiết bị Android 7.0/API 24 trở
lên bằng:

```powershell
adb install -r .\apk\app-release.apk
```

Hoặc trên Linux/macOS:

```bash
adb install -r ./apk/app-release.apk
```

Xem hướng dẫn môi trường, build Google Play và release signing tại
[`apps/android/README.md`](apps/android/README.md).

## Luồng sản phẩm

1. Tạo tài khoản cục bộ hoặc đăng nhập. Tài khoản là ngẫu nhiên, lưu trên máy và không cần kết nối server, do đó mọi tài khoản đều có thể được đăng kí. Không có tài khoản mặc định. Không có xác thực thông tin cá nhân.
2. Chọn **Scan → Gắn tag**, chụp ảnh, chỉnh ROI và đặt tên đồ vật.
3. Chọn **Scan → Nhận diện**, chụp khung cảnh; MobileNetV3 + SSD MobileNet chạy
   trên thiết bị và ghi nhận lần xuất hiện nếu khớp.
4. Mở **Tìm**, nhập/chọn tag để xem lần cuối, tọa độ và độ tin cậy.
5. Ứng dụng phát thông báo local sau khi ghi nhận thành công.

## Kiến trúc

```text
Jetpack Compose UI
        |
        v
core:contracts  <--- API ổn định giữa bốn bài toán
        |
        +-- feature:enrollment  (ROI -> embedding -> reference)
        +-- feature:recognition (SSD crop + MobileNet + cosine)
        +-- feature:memory      (sighting, dedupe, last-seen)
        +-- feature:securevault (AES-256-GCM + Android Keystore)
                         |
                         v
                    Room + encrypted files
```

- Ảnh, tag, vector embedding và vị trí chính xác được mã hóa trước khi ghi đĩa.
- Khóa AES-256 được tạo và giữ trong Android Keystore; mỗi bản ghi dùng nonce
  ngẫu nhiên và AAD ràng buộc account/record/type để chống tráo ciphertext.
- Room lưu metadata, quan hệ và timeline; dữ liệu được phân vùng theo tài khoản
  cục bộ.
- Hai model ONNX nằm trong APK và không tải dữ liệu người dùng ra ngoài.

Mã thử nghiệm Ktor/Node cũ vẫn nằm trong `playground/` và `services/` để giữ lịch
sử đóng góp, nhưng không được đóng gói và không tham gia runtime Android.

## Đóng góp đã tích hợp

- `khuong_enrollment`: pipeline enrollment và MobileNetV3 embedding.
- `phong_recognition`: nhận diện hai pipeline SSD + MobileNet/cosine.
- `Phat`: memory timeline, chống ghi trùng và truy vấn lần cuối.
- `final`: chuyển toàn bộ sang Android on-device, Room, local auth, permissions,
  notification và secure vault.

Requirement từng thành viên và lecture security nằm trong [`docs/team`](docs/team)
và [`docs/lectures`](docs/lectures).
