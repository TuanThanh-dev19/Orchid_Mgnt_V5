# SBA301 Lab 05 — Orchid Management

Ứng dụng React/Vite ↔ Axios ↔ Spring Boot 3 ↔ Spring Data JPA ↔ SQL Server.
Backend và frontend là hai project độc lập, theo cấu trúc và mẫu code trong PDF Lab 05.

## Đối chiếu với tài liệu

- Giữ `chapter15api`, package `fu.se.chapter15api`, `IOrchidRepository`, `IOrchidService`, `OrchidService`, `OrchidController` và `CorsConfig` theo PDF.
- Giữ React **JavaScript/JSX**, Bootstrap, Axios client, `NavBar`, `ListOfOrchids`, `EditOrchid`, các route và luồng CRUD theo PDF.
- Dùng **OrchidManagementDB**, `spring.application.name=orchid-api` và toàn bộ cấu hình datasource do người dùng cung cấp, thay cho tên database minh họa `SBA301_OrchidDB` trong PDF.
- Tên cột SQL, validation và chức năng xem chi tiết bằng cách bấm ảnh/tên theo README tham khảo. Chuyển phần mở rộng TypeScript của README sang JSX để giữ baseline frontend của PDF.
- GET/POST collection nhận cả `/orchids/` (PDF) và `/orchids` (README); Axios giữ `get("/")` và `post("/", payload)` như PDF.
- Bổ sung trạng thái chờ tải/lưu, khóa form khi chưa tải xong, giới hạn chiều dài input và cleanup effect để tránh lưu nhầm dữ liệu hoặc phản hồi cũ.
- CORS giữ đúng origin `http://localhost:5173`. Vite cố định port 5173 bằng `strictPort`.

## Cấu trúc

```text
SBA301-Lab05/
├── chapter15api/         # Java 21, Spring Boot 3.5.16, Maven, JPA
├── chapter15reactjs/     # React JSX, Vite, Axios, React Router, Bootstrap
├── sql/                 # Chỉ chạy thủ công bằng SSMS
├── postman/             # Collection kiểm tra REST API
└── docs/                # Test matrix, Break/Fix, hướng dẫn GitHub
```

## 1. SQL Server — bạn tự chạy

Mở SSMS, kết nối SQL Server của bạn, mở `sql/01-create-and-seed.sql`, rồi Execute.
Script tạo `OrchidManagementDB`, bảng `dbo.Orchid` nếu chưa có và thêm ba mẫu khi bảng rỗng.
Script không xóa bảng/dữ liệu. Nếu bảng đã tồn tại, script giữ nguyên schema; đối chiếu các cột trước khi chạy backend.

SQL Server cần bật TCP/IP, port 1433, SQL Server Authentication và cho phép tài khoản `sa` đăng nhập.
`sql/02-verify.sql` dùng kiểm tra số hàng/dữ liệu sau khi thao tác CRUD.
Không có script tự động chạy SQL Server khi cài/chạy project.

## 2. Backend

Yêu cầu JDK 21 và Maven 3.6.3+. Máy triển khai đã có JDK 21 và Maven.

File `chapter15api/src/main/resources/application.properties` trên máy này giữ **nguyên cấu hình bạn gửi**, kể cả giá trị mặc định local. File này được `.gitignore` loại khỏi Git để không đẩy mật khẩu lên GitHub.
Repository sử dụng `application.properties.example` không chứa mật khẩu.

**Sau khi clone trên máy khác**, thực hiện trong PowerShell:

```powershell
cd chapter15api
Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties
$env:DB_PASSWORD = 'MAT_KHAU_SQL_SERVER_CUA_BAN'
mvn spring-boot:run
```

**Trên máy hiện tại** đã có cấu hình local, chỉ cần:

```powershell
cd chapter15api
mvn spring-boot:run
```

Nếu cần đổi host/user/password, đặt `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` trong terminal chạy Maven hoặc Environment variables của cấu hình Run trong IntelliJ. Không đặt vào frontend.

Chạy IntelliJ: mở `chapter15api/pom.xml` → Load Maven → chọn SDK Java 21 → Run `fu.se.chapter15api.Chapter15apiApplication`. Để Active profiles trống khi dùng SQL Server. Profile `test` chỉ dành cho H2 trong automated tests.

Xác nhận `GET http://localhost:8080/orchids/` trả `200` trước khi tích hợp React.

## 3. Frontend

Mở terminal thứ hai tại thư mục gốc:

```powershell
cd chapter15reactjs
npm ci
```

Sau khi clone, tạo cấu hình từ mẫu (máy hiện tại đã có `.env`):

```powershell
Copy-Item .env.example .env
npm run dev
```

Mở `http://localhost:5173`. `.env` có:

```dotenv
VITE_API_URL=http://localhost:8080/orchids
```

Khởi động lại Vite sau khi sửa `.env`. Khi port 5173 bị chiếm, dừng ứng dụng đang chiếm port; không chạy Vite ở port khác vì CORS hiện chỉ cho 5173.

## 4. API và giao diện

| Method | Endpoint | Kết quả |
| --- | --- | --- |
| GET | `/orchids/` hoặc `/orchids` | 200 + array |
| GET | `/orchids/{id}` | 200 + object; 404 nếu không tồn tại |
| POST | `/orchids/` hoặc `/orchids` | 201 + object có ID tự sinh |
| PUT | `/orchids/{id}` | 200 + object; 404 nếu không tồn tại |
| DELETE | `/orchids/{id}` | 204; 404 nếu không tồn tại |

Payload theo PDF, không cần ID khi tạo:

```json
{
  "orchidName": "Taichung Beauty",
  "natural": true,
  "orchidDescription": "Demo orchid for SBA301 Lab 05",
  "orchidCategory": "Cattleya",
  "attractive": true,
  "orchidURL": "https://example.com/orchid.jpg"
}
```

`example.com/orchid.jpg` chỉ là URL minh họa của PDF; thay bằng link ảnh thật hoặc để trống. Dữ liệu seed để ảnh NULL, không dùng ảnh giả. Ứng dụng lưu URL, không upload file.

Frontend có `/` (list), `/orchids/new` (create), `/orchids/:id/edit` (edit), `/orchids/:id` (detail).
Bấm tên/ảnh để xem detail; List/Create điều hướng SPA. Delete có hộp xác nhận theo mẫu PDF.

## 5. Kiểm chứng

Backend automated tests dùng H2 memory ở scope test, không kết nối SQL Server:

```powershell
cd chapter15api
mvn test
mvn package
```

Frontend:

```powershell
cd chapter15reactjs
npm run lint
npm run build
```

Import `postman/OrchidManagement.postman_collection.json` vào Postman, chạy thứ tự request bằng Collection Runner. Collection tạo record riêng, ghi nhớ ID, cập nhật, rồi xóa đúng record đó.
Trong DevTools → Network kiểm tra GET 200, POST 201, PUT 200, DELETE 204; đối chiếu SQL bằng `sql/02-verify.sql`.
Test matrix và hướng dẫn thu bằng chứng nằm tại `docs/TESTING.md`; bài Break/Fix nằm tại `docs/BREAK_FIX.md`.

Automated tests/build không thay thế kiểm chứng SQL Server thực tế. Người dùng cần chạy SQL và kiểm tra UI ↔ API ↔ SQL Server sau khi cấu hình dịch vụ local.

## 6. Chẩn đoán

- Connection refused: backend chưa chạy/sai URL/port. Kiểm tra Postman trước.
- Login failed: kiểm tra SQL Authentication, tài khoản, password và quyền database.
- CORS: mở frontend bằng `http://localhost:5173`, không dùng `127.0.0.1` hoặc port khác.
- Invalid column name: đối chiếu `dbo.Orchid`, các `@Column` và physical naming strategy; không đổi `ddl-auto=create` để sửa trên dữ liệu thật.
- 400: tên trống, trường vượt chiều dài hoặc JSON sai; 415: Content-Type phải là application/json.
- 404 detail: ID không tồn tại. Ảnh lỗi: kiểm tra URL bên ngoài, ứng dụng không lưu file ảnh.
- Deploy SPA sau này cần history fallback về `index.html`; Vite dev đã hỗ trợ reload URL detail.

## 7. Đẩy lên GitHub

Xem từng bước và các lệnh PowerShell tại `docs/GITHUB.md`. Commit source, SQL, Postman, README và package-lock; không commit `.env`, mật khẩu, node_modules, target hoặc dist.

Tham khảo baseline công cụ: [Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Vite](https://vite.dev/guide/).
