# Task Manager Backend API (Quản lý Dự án & Công việc Doanh nghiệp Thông minh)

Dự án Backend RESTful API Quản lý Dự án & Công việc được xây dựng theo kiến trúc phân lớp chuẩn Enterprise bằng **Java 17** và **Spring Boot 3.3.x**. Dự án bao gồm đầy đủ nghiệp vụ CRUD, phân quyền vai trò bảo mật (Spring Security 6 & JWT), cách ly dữ liệu chuyên sâu (Data-level Authorization), nghiệp vụ **bắt buộc minh chứng nghiệm thu khi hoàn thành công việc**, bộ lọc tiến độ theo từng nhân sự kết hợp mốc thời gian (**24h, 3 ngày, 7 ngày, 30 ngày**), phân trang & sắp xếp, tính toán tiến độ tự động, thuật toán điểm ưu tiên thông minh (Smart Priority Scoring), bộ bóc tách ngôn ngữ tự nhiên (NLP Task Parser), giao diện Web Dashboard tối giản hiện đại (Linear / GitHub / Notion SaaS style), tài liệu OpenAPI Swagger UI, và bộ kiểm thử tự động **66 automated tests** đạt chuẩn chất lượng cao.

---

## 🌟 Tính Năng Nổi Bật

### 1. Giao Diện Web Dashboard Tối Giản & Chuẩn Doanh Nghiệp (`http://localhost:8080/`)
- Thiết kế theo phong cách tối giản thanh lịch (**Linear / GitHub / Notion SaaS style**) với tông màu trung tính (`Slate-900`, `Slate-50`, `Slate-200`), viền mảnh 1px tinh tế, loại bỏ triệt để màu mè lòe loẹt.
- **Kiến Trúc Giao Diện Đơn Nhiệm (1 Trang - 1 Chức Năng)**:
  - **Bảng Kanban (`tasksTab`)**: Toàn bộ không gian cho quy trình làm việc Kanban 4 cột và thanh công cụ tìm kiếm / lọc 2 tầng.
  - **Thống Kê & Báo Cáo (`statsTab`)**: 4 thẻ KPI tổng quan, phân bổ công việc theo 4 trạng thái, tiến độ chi tiết từng dự án.
  - **Ưu Tiên Thông Minh (`priorityTab`)**: Bảng tính điểm ưu tiên và độ khẩn cấp (Smart Priority Scoring).
  - **Quản Lý Dự Án (`projectsTab`)**: Danh sách dự án, đối tác khách hàng, phân quyền thành viên.
- **Kanban Board 4 Cột Chuẩn Agile/QA**: Cần làm (`TODO`), Đang làm (`IN_PROGRESS`), Cần sửa lại (`REWORK` - Khi Khách hàng hoặc Tester kiểm tra lại phát hiện lỗi và yêu cầu nhân viên làm lại kèm ghi chú chi tiết), Đã xong (`DONE`).
- **Nghiệp vụ Nộp Minh Chứng & Sửa Lỗi**: Bắt buộc hoàn thành 100% subtask và minh chứng trước khi sang `DONE`. Khi task ở `DONE`, Khách hàng (`CLIENT`) hoặc Tester/Admin có thể nhấn "Báo lỗi / Yêu cầu sửa" (hoặc kéo thẻ sang cột `REWORK`) để nhân viên tiếp tục sửa lại.
- **Bộ Lọc Đa Năng 1 Chạm**:
  - Chuyển đổi nhanh giữa `[ Tất cả việc ]` và `[ 👤 Việc của tôi ]`.
  - Huy hiệu `Admin: Toàn hệ thống` thông báo chế độ giám sát toàn cảnh.
  - Dropdown lọc công việc theo **từng người thực hiện** (`Nguyễn Văn Nam`, `Trần Thị Hoa`, `Chưa phân công`).
  - Dropdown lọc công việc theo **mốc thời gian** (⏱️ `Trong 24 giờ qua`, 📅 `Trong 3 ngày qua`, 📅 `Trong 7 ngày qua`, 🗓️ `Trong 30 ngày qua`).
- **Thanh Command Bar Nhập Việc Nhanh (NLP)**: Thiết kế dạng phím tắt Spotlight tinh gọn (`⚡ Nhập việc: Họp triển khai lúc 14:00 ngày mai...`).
- **Menu Người Dùng Chuẩn UX (Click-to-Toggle)**: Bấm vào mở ra gọn gàng, bấm lại hoặc bấm ra ngoài màn hình (Click Outside) tự động thu lại, chỉ hiển thị thông tin chính chủ và các tác vụ cần thiết.

---

### 2. Phân Quyền 3 Cấp & Cách Ly Dữ Liệu Chuyên Sâu (Spring Security 6 & JWT RBAC)
- **Quản Trị Viên (`ROLE_ADMIN`)**:
  - Toàn quyền quản trị hệ thống, quản lý tất cả dự án, phân công nhân sự cho bất kỳ thành viên nào.
  - Xem và lọc toàn bộ công việc của mọi dự án và mọi nhân viên trong công ty.
  - Xem dashboard thống kê tổng thể toàn hệ thống.
- **Nhân Viên (`ROLE_EMPLOYEE`)**:
  - Xem danh sách dự án kỹ thuật chung, cập nhật trạng thái công việc (`TODO -> IN_PROGRESS -> DONE`).
  - Tự nhận việc (`Self-assign`), quản lý checklist subtask, lọc nhanh "Việc của tôi".
  - Không được quyền xóa dự án hay xóa công việc của hệ thống.
- **Khách Hàng (`ROLE_CLIENT`) - Cách Ly Dữ Liệu Độc Lập (Multi-Tenant Isolation)**:
  - **Cách ly dữ liệu 100%**: Chỉ nhìn thấy và tạo task/yêu cầu trong phạm vi các dự án thuộc quyền sở hữu của công ty mình (VinFast chỉ thấy dự án VinFast, hoàn toàn không thấy dự án FPT hay dự án nội bộ).
  - Không nhìn thấy công việc nội bộ hay các subtask kỹ thuật nhạy cảm.
  - Dashboard thống kê (`/analytics/dashboard`) tự động co gọn tính toán riêng trên tập dữ liệu của khách hàng.
- **Ràng Buộc Nghiệp Vụ & An Toàn Dữ Liệu**:
  - **Chặn xóa dự án khi còn việc dở dang**: Nếu dự án còn task đang làm (`IN_PROGRESS`), hệ thống chủ động chặn xóa và trả về `400 Bad Request`.
  - **Chặn phân công task kỹ thuật cho Khách hàng**: Hệ thống chỉ cho phép gán việc cho tài khoản có vai trò kỹ thuật (`ADMIN`, `EMPLOYEE`).

---

### 3. Nghiệp Vụ Minh Chứng Hoàn Thành Công Việc (Proof of Completion)
- Khi một công việc được đánh dấu hoàn thành (`status = DONE`), hệ thống **bắt buộc** phải có minh chứng thực tế:
  - `completionNote` (*Bắt buộc*): Báo cáo kết quả thực hiện, giải pháp đã triển khai, kết quả chạy kiểm thử (tối đa 2000 ký tự).
  - `proofUrl` (*Tùy chọn*): Đường dẫn đính kèm minh chứng nghiệm thu (Link GitHub Pull Request, Jira issue, tài liệu Google Docs, Demo URL...).
  - `completedAt`: Thời điểm hoàn thành chuẩn ISO.
  - `completedBy`: Nhân sự đánh dấu hoàn thành.
- **Chặn Lách Luật Tầng Nghiệp Vụ**: Không thể đổi trạng thái sang `DONE` thông qua API cập nhật nhanh hay API sửa task nếu chưa nộp minh chứng qua endpoint `/complete`.

---

### 4. Bộ Lọc Tiến Độ Từng Nhân Sự Theo Thời Gian (24h, 3 ngày, 7 ngày, 30 ngày)
- Phục vụ trực tiếp cho các kịch bản quản lý dự án Agile/Scrum thực tế:
  - **Họp Standup đầu ngày (24 giờ qua)**: Lọc các công việc mà một nhân sự cụ thể đã tạo, cập nhật hoặc hoàn thành trong vòng 24 giờ.
  - **Theo dõi giữa tuần (3 ngày qua)**: Đánh giá tiến độ công việc các ngày gần nhất.
  - **Báo cáo tuần / Sprint (7 ngày qua)**: Rà soát khối lượng công việc hoàn tất trong chu kỳ 1 tuần.
  - **Đánh giá tháng (30 ngày qua)**: Thống kê hiệu suất và KPI của từng thành viên trong tháng.
- Phối hợp đa điều kiện bằng JPA Specification: Người thực hiện + Mốc thời gian + Dự án + Độ ưu tiên + Trạng thái + Từ khóa.

---

### 5. Quản Lý Dự Án & Công Việc Con (Subtask & Progress Tracking)
- Quản lý Dự án (Project) với tính toán tự động số lượng việc và tỷ lệ hoàn thành.
- Mỗi công việc chính (Task) có thể chia nhỏ thành nhiều việc con (Subtasks).
- Tiến độ của Task (`0.0% -> 100.0%`) được **tự động tính toán theo thời gian thực** theo tỷ lệ subtask đã hoàn thành.

---

### 6. Thuật Toán Tính Điểm Ưu Tiên Thông Minh (Smart Priority Scoring)
- Tự động chấm điểm độ khẩn cấp và tầm quan trọng của từng đầu việc theo công thức minh bạch:
  $$\text{Total Score} = \text{PriorityWeight} + \text{UrgencyScore} + \text{StatusModifier}$$
  - **Việc đã xong (DONE)**: 0 điểm.
  - **Mức độ ưu tiên**: URGENT (+80), HIGH (+50), MEDIUM (+25), LOW (+10).
  - **Mức độ khẩn cấp theo hạn chót (Urgency)**:
    - Quá hạn: $100 + \min(|daysOverdue| \times 5, 50)$ điểm (phạt khẩn cấp cao nhất).
    - Đến hạn hôm nay: +90 điểm.
    - Sắp đến hạn (1-3 ngày): $70 - (daysRemaining \times 10)$ điểm.
    - Đến hạn trong tuần tới: +35 điểm; Dài hạn (> 7 ngày): +10 điểm; Không hạn: +5 điểm.
  - **Trạng thái dở dang**: `IN_PROGRESS` nhận thêm **+15 điểm** để ưu tiên chốt việc đang làm dở.
- Cung cấp danh sách việc cần làm ngay kèm lời giải thích điểm số chi tiết từng tiêu chí.

---

### 7. Tạo Nhanh Công Việc Từ Câu Tự Nhiên (NLP Task Parser)
- Bóc tách tự động thời gian, ngày tháng và mức độ ưu tiên từ câu nói tiếng Việt và tiếng Anh thông dụng bằng Regex (VD: `"Họp triển khai lúc 14:00 ngày mai mức độ URGENT"`, `"Nộp báo cáo gấp lúc 17h hôm nay"`).

---

## 🛠 Công Nghệ Sử Dụng

- **Ngôn ngữ**: Java 17 (LTS)
- **Framework**: Spring Boot 3.3.4 (Spring Web, Spring Data JPA, Spring Validation, Spring Security 6)
- **Bảo mật & Token**: JSON Web Token (JWT - `jjwt-api 0.12.6`), BCrypt Password Encoder
- **Cơ sở dữ liệu**:
  - **Dev Profile**: H2 In-Memory Database + H2 Console
  - **Prod Profile**: PostgreSQL 16
- **Tài liệu API**: SpringDoc OpenAPI 2.6.0 (Swagger UI tương tác, cấu hình Bearer Token Authorize)
- **Kiểm thử (Testing)**: JUnit 5, Mockito, Spring Security Test, Spring Boot Test (MockMvc)
- **Containerization**: Docker & Docker Compose
- **Tiếng Việt & UTF-8**: Bắt buộc bảng mã UTF-8 toàn diện ở Maven Compiler, Servlet Filter, Jackson Serializer và Unicode Regex Parser.

---

## 📁 Cấu Trúc Thư Mục Dự Án

```
task-manager
├── .gitignore
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
└── src
    ├── main
    │   ├── java/com/portfolio/taskmanager
    │   │   ├── TaskManagerApplication.java
    │   │   ├── config
    │   │   │   ├── OpenApiConfig.java              # Cấu hình Swagger OpenAPI & Bearer JWT
    │   │   │   ├── JpaAuditingConfig.java          # Kích hoạt Auditing createdAt, updatedAt
    │   │   │   ├── CaseInsensitiveEnumConverterFactory.java
    │   │   │   ├── WebConfig.java
    │   │   │   └── DataInitializer.java            # Seed dữ liệu 5 users, 3 projects, tasks & subtasks
    │   │   ├── controller
    │   │   │   ├── AuthController.java             # REST API Đăng nhập, Đăng ký, Lấy thông tin cá nhân
    │   │   │   ├── UserController.java             # REST API Danh bạ nhân sự thực hiện (assignees)
    │   │   │   ├── ProjectController.java          # REST API Quản lý Dự án
    │   │   │   ├── TaskController.java             # REST API Quản lý Task, My Tasks, Assign, Complete kèm minh chứng
    │   │   │   ├── SubtaskController.java          # REST API Quản lý Subtask
    │   │   │   └── AnalyticsController.java        # REST API Thống kê & Dashboard
    │   │   ├── dto
    │   │   │   ├── request                         # DTOs đầu vào có Bean Validation (@NotBlank, @Size...)
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── TaskCompleteRequest.java    # DTO nộp minh chứng hoàn thành
    │   │   │   │   ├── TaskCreateRequest.java
    │   │   │   │   ├── TaskUpdateRequest.java
    │   │   │   │   ├── QuickTaskRequest.java
    │   │   │   │   └── ...
    │   │   │   └── response                        # DTOs đầu ra (TaskResponse, PageResponse, ApiResponse...)
    │   │   ├── entity
    │   │   │   ├── BaseEntity.java                 # Superclass quản lý createdAt, updatedAt
    │   │   │   ├── User.java                       # Tài khoản người dùng & Role
    │   │   │   ├── Project.java                    # Dự án, Client sở hữu & Members
    │   │   │   ├── Task.java                       # Công việc, Assignee, Creator, Minh chứng & Tiến độ
    │   │   │   ├── Subtask.java                    # Công việc con
    │   │   │   └── enums (UserRole, TaskPriority, TaskStatus)
    │   │   ├── repository
    │   │   │   ├── UserRepository.java
    │   │   │   ├── ProjectRepository.java
    │   │   │   ├── TaskRepository.java
    │   │   │   ├── SubtaskRepository.java
    │   │   │   └── specification/TaskSpecification.java # Lọc động: assignee, timeRange, status, project...
    │   │   ├── security
    │   │   │   ├── SecurityConfig.java             # Cấu hình Spring Security 6, JWT Filter, Phân quyền endpoint
    │   │   │   ├── JwtTokenProvider.java           # Tạo và xác thực JWT token
    │   │   │   ├── JwtAuthenticationFilter.java     # Intercept header Authorization: Bearer
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   └── SecurityUtils.java              # Helper trích xuất User hiện tại & kiểm tra Role
    │   │   ├── service
    │   │   │   ├── AuthService.java & impl
    │   │   │   ├── ProjectService.java & impl
    │   │   │   ├── TaskService.java & impl
    │   │   │   ├── SubtaskService.java & impl
    │   │   │   ├── AnalyticsService.java & impl
    │   │   │   └── smart
    │   │   │       ├── TaskPriorityCalculator.java # Thuật toán tính điểm ưu tiên thông minh
    │   │   │       └── NaturalLanguageTaskParser.java # Bộ phân tích NLP câu tự nhiên
    │   │   ├── mapper                              # MapStruct-style DTO Mapper an toàn
    │   │   └── exception
    │   │       ├── ResourceNotFoundException.java  # Lỗi 404
    │   │       ├── BadRequestException.java        # Lỗi 400
    │   │       └── GlobalExceptionHandler.java     # Bắt lỗi tập trung (@RestControllerAdvice)
    │   └── resources
    │       ├── application.yml                     # Cấu hình chung
    │       ├── application-dev.yml                 # Cấu hình môi trường DEV (H2 Database)
    │       ├── application-prod.yml                # Cấu hình môi trường PROD (PostgreSQL)
    │       └── static/index.html                   # Giao diện Web Dashboard SaaS tối giản
    └── test
        └── java/com/portfolio/taskmanager
            ├── service                             # Unit tests nghiệp vụ
            ├── controller                          # Integration tests Web layer MockMvc
            └── security                            # RBAC Authorization tests
```

---

## 🚀 Hướng Dẫn Cài Đặt & Khởi Chạy

### Cách 1: Chạy trực tiếp bằng Maven (Môi trường Dev với H2 DB)

**Yêu cầu**: Máy đã cài JDK 17 và Maven 3.8+.

1. Clone hoặc mở thư mục dự án:
   ```bash
   cd task-manager
   ```

2. Biên dịch và chạy ứng dụng:
   ```bash
   mvn clean spring-boot:run
   ```

3. Sau khi khởi động thành công:
   - **Giao diện Web Dashboard**: [http://localhost:8080/](http://localhost:8080/)
   - **Swagger UI (Tài liệu API tương tác & Test trực tiếp)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
     - JDBC URL: `jdbc:h2:mem:taskdb`
     - Username: `sa`
     - Password: *(để trống)*

Khi chạy ở profile `dev`, hệ thống tự động nạp sẵn 5 tài khoản mẫu và 3 dự án thực tế thông qua `DataInitializer`:

| Username | Mật khẩu | Vai trò (Role) | Chức danh / Đại diện | Quyền hạn chính |
| :--- | :--- | :--- | :--- | :--- |
| `admin` | `admin123` | **ADMIN** | Quản Trị Viên Hệ Thống | Toàn quyền quản trị, giao việc, xem toàn bộ hệ thống |
| `dev_nam` | `dev123` | **EMPLOYEE** | Nguyễn Văn Nam (Tech Lead) | Nhận việc, nộp minh chứng, xem "Việc của tôi" |
| `dev_hoa` | `dev123` | **EMPLOYEE** | Trần Thị Hoa (Senior Dev) | Thực hiện task, cập nhật subtask |
| `client_vinfast` | `client123` | **CLIENT** | VinFast Corporation | Chỉ xem dự án VinFast, gửi yêu cầu phản hồi |
| `client_fpt` | `client123` | **CLIENT** | FPT Software | Chỉ xem dự án FPT, gửi yêu cầu phản hồi |

---

### Cách 2: Chạy Production bằng Docker & Docker Compose (Cùng PostgreSQL)

**Yêu cầu**: Máy đã cài đặt Docker và Docker Compose.

1. Khởi động toàn bộ cụm dịch vụ (Backend + PostgreSQL Database):
   ```bash
   docker-compose up --build -d
   ```

2. Kiểm tra trạng thái containers:
   ```bash
   docker-compose ps
   ```

3. Dừng hệ thống:
   ```bash
   docker-compose down
   ```

---

## 🧪 Hướng Dẫn Chạy Test

Hệ thống được kiểm thử tự động toàn diện bằng JUnit 5, Mockito, Spring Security Test và MockMvc với **66 automated tests** (100% PASS):

```bash
mvn test
```

### Chi tiết các bộ kiểm thử:
- **Unit Test (Service & Smart Utilities Layer)**:
  - `AuthServiceTest` (7 tests): Đăng nhập, đăng ký, mã hóa mật khẩu, kiểm tra trùng username/email, lấy thông tin cá nhân.
  - `ProjectServiceTest` (8 tests): CRUD dự án, phân quyền cách ly dữ liệu Client, chặn xóa dự án khi có task `IN_PROGRESS` dở dang.
  - `TaskServiceTest` (11 tests): Tạo task, lọc đa tiêu chí, tự nhận việc, Admin giao việc, chặn phân công Client, việc của tôi (`/my-tasks`), **bắt buộc minh chứng hoàn thành, ném BadRequestException khi thiếu minh chứng**.
  - `SubtaskServiceTest` (5 tests): Tạo subtask, đảo trạng thái toggle, cập nhật, xóa, tự động tính lại % task.
  - `AnalyticsServiceTest` (1 test): Tính toán số liệu thống kê tổng quan, tỷ lệ hoàn thành, việc quá hạn (cách ly số liệu theo Client).
  - `TaskPriorityCalculatorTest` (4 tests): Kiểm thử công thức trọng số ưu tiên, mức phạt quá hạn, điểm ưu tiên việc dở dang.
  - `NaturalLanguageTaskParserTest` (4 tests): Kiểm thử bóc tách câu tiếng Việt có dấu, ngày mai, hôm nay, giờ phút, mức độ gấp.
- **Web Controller MockMvc Test**:
  - `AuthControllerTest` (2 tests): Test API login và register.
  - `ProjectControllerTest` (5 tests): Kiểm thử HTTP 200, 201, 204, Validation 400.
  - `TaskControllerTest` (9 tests): Kiểm thử CRUD, phân trang, lọc theo nhân sự & mốc thời gian, **hoàn thành kèm minh chứng (200 OK), thiếu minh chứng (400 Bad Request)**, việc của tôi (`/my-tasks`), giao việc (`/assign`).
  - `SubtaskControllerTest` (4 tests): Kiểm thử tạo, sửa, toggle, xóa subtask.
  - `AnalyticsControllerTest` (1 test): Kiểm thử endpoint dashboard.
- **Security & RBAC Integration Test (`@SpringBootTest`)**:
  - `RoleAuthorizationIntegrationTest` (5 tests): Kiểm thử chặn 401 Unauthorized khi chưa đăng nhập, chặn 403 Forbidden khi `CLIENT` hoặc `EMPLOYEE` cố xóa dự án, chặn `CLIENT` chỉnh sửa subtask nội bộ, cho phép `ADMIN` thực hiện đầy đủ chức năng.

---

## 📖 Danh Sách API Endpoints & Ví Dụ Request/Response

### 0. Xác thực & Quản lý Người Dùng (`/api/v1/auth`, `/api/v1/users`)

#### 0.1. Đăng nhập hệ thống (Lấy JWT Bearer Token)
- **Method**: `POST`
- **URL**: `/api/v1/auth/login`
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
      "token": "eyJhbGciOiJIUzUxMiJ9...",
      "tokenType": "Bearer",
      "id": 1,
      "username": "admin",
      "fullName": "Quản Trị Viên Hệ Thống",
      "email": "admin@taskmanager.com",
      "role": "ADMIN",
      "roleDisplayName": "Quản trị viên"
    }
  }
  ```

#### 0.2. Đăng ký tài khoản mới (Mặc định vai trò CLIENT)
- **Method**: `POST`
- **URL**: `/api/v1/auth/register`
- **Request Body**:
  ```json
  {
    "username": "khachhang_moi",
    "password": "password123",
    "fullName": "Công ty TNHH Vận Tải X",
    "email": "contact@vantai-x.vn"
  }
  ```
- **Response** (`201 Created`): Trả về JWT Token cùng thông tin tài khoản vừa đăng ký.

#### 0.3. Xem thông tin tài khoản cá nhân
- **Method**: `GET`
- **URL**: `/api/v1/auth/me`
- **Headers**: `Authorization: Bearer <TOKEN>`
- **Response** (`200 OK`): Trả về thông tin chi tiết người dùng đang đăng nhập.

#### 0.4. Danh bạ nhân sự thực hiện công việc (Assignee Directory)
- **Method**: `GET`
- **URL**: `/api/v1/users/assignees`
- **Headers**: `Authorization: Bearer <TOKEN>`
- **Response** (`200 OK`): Trả về danh sách các user có vai trò kỹ thuật (`ADMIN`, `EMPLOYEE`) phục vụ giao việc và lọc task, không bao gồm `CLIENT`.

---

### 1. Quản lý Dự án (`/api/v1/projects`)

#### 1.1. Tạo dự án mới
- **Method**: `POST`
- **URL**: `/api/v1/projects`
- **Quyền**: `ADMIN` hoặc `EMPLOYEE`
- **Request Body**:
  ```json
  {
    "name": "Hệ thống Cổng Thanh Toán",
    "description": "Module tích hợp VnPay và QR Code"
  }
  ```
- **Response** (`201 Created`)

#### 1.2. Lấy danh sách dự án
- **Method**: `GET`
- **URL**: `/api/v1/projects`
- **Quy tắc cách ly**: `ADMIN` và `EMPLOYEE` thấy các dự án chung; `CLIENT` chỉ thấy các dự án thuộc công ty mình.
- **Response** (`200 OK`): Trả về danh sách dự án kèm tỷ lệ hoàn thành công việc.

#### 1.3. Lấy chi tiết dự án
- **Method**: `GET`
- **URL**: `/api/v1/projects/{id}`
- **Response** (`200 OK`): Chi tiết dự án cùng danh sách `tasks` thuộc dự án.

#### 1.4. Xóa dự án (Có ràng buộc an toàn dữ liệu)
- **Method**: `DELETE`
- **URL**: `/api/v1/projects/{id}`
- **Quyền**: Chỉ `ADMIN`
- **Ràng buộc**: Nếu dự án còn task đang làm dở (`IN_PROGRESS`), hệ thống chặn lại và trả về `400 Bad Request`.
- **Response** (`204 No Content`)

---

### 2. Quản lý Công việc (`/api/v1/tasks`)

#### 2.1. Tạo mới công việc
- **Method**: `POST`
- **URL**: `/api/v1/tasks`
- **Request Body**:
  ```json
  {
    "title": "Tích hợp Cổng thanh toán VnPay",
    "description": "Xây dựng module thanh toán và webhook verify chữ ký",
    "dueDate": "2026-12-31T18:00:00",
    "priority": "URGENT",
    "projectId": 1,
    "subtasks": [
      "Tạo tài khoản Sandbox VnPay",
      "Viết API tạo URL thanh toán",
      "Xử lý IPN Verify Chữ ký điện tử"
    ]
  }
  ```
- **Response** (`201 Created`): Trả về chi tiết công việc, subtasks và tiến độ khởi tạo 0.0%.

#### 2.2. Danh sách công việc (Lọc theo Nhân Sự, Mốc Thời Gian & Sắp Xếp)
- **Method**: `GET`
- **URL**: `/api/v1/tasks?assigneeId=2&timeRange=24h&status=IN_PROGRESS&page=0&size=10&sortBy=createdAt&sortDir=DESC`
- **Tham số truy vấn (Query Params)**:
  - `assigneeId`: Lọc theo người thực hiện (`-1`: Chưa phân công, `>0`: ID nhân sự cụ thể).
  - `timeRange`: Mốc thời gian (`24h`, `3d`, `7d`, `30d`).
  - `timeField`: Trường áp dụng lọc thời gian (`active` - mặc định, `createdAt`, `updatedAt`, `dueDate`, `completedAt`).
  - `status`: `TODO`, `IN_PROGRESS`, `DONE`.
  - `priority`: `LOW`, `MEDIUM`, `HIGH`, `URGENT`.
  - `projectId`: Lọc theo ID dự án.
  - `dueDateFrom`, `dueDateTo`: Lọc theo khoảng thời hạn (ISO Format).
  - `keyword`: Tìm kiếm từ khóa trong tiêu đề hoặc mô tả.
  - `page`, `size`: Phân trang.
  - `sortBy`, `sortDir`: Cột sắp xếp và chiều (`ASC`/`DESC`).

#### 2.3. Đánh dấu hoàn thành công việc kèm Minh Chứng Nghiệm Thu (Proof Required)
- **Method**: `PATCH`
- **URL**: `/api/v1/tasks/{id}/complete`
- **Quyền**: `ADMIN` hoặc `EMPLOYEE`
- **Request Body** (*Bắt buộc phải có `completionNote`*):
  ```json
  {
    "completionNote": "Đã hoàn thành merge PR #42, kiểm thử 10/10 test case pass trên staging.",
    "proofUrl": "https://github.com/techcorp/ecommerce/pull/42"
  }
  ```
- **Quy tắc nghiệp vụ**: Nếu không gửi body hoặc `completionNote` để trống, hệ thống trả về lỗi `400 Bad Request`: *"Bắt buộc phải cung cấp minh chứng hoàn thành công việc"*.
- **Response** (`200 OK`):
  ```json
  {
    "success": true,
    "message": "Đã hoàn thành công việc kèm minh chứng thành công",
    "data": {
      "id": 2,
      "title": "Tích hợp Cổng thanh toán VnPay",
      "status": "DONE",
      "progress": 100.0,
      "completionNote": "Đã hoàn thành merge PR #42, kiểm thử 10/10 test case pass trên staging.",
      "proofUrl": "https://github.com/techcorp/ecommerce/pull/42",
      "completedAt": "2026-11-04T23:39:04",
      "completedByName": "Quản Trị Viên Hệ Thống"
    }
  }
  ```

#### 2.4. Đổi nhanh trạng thái (TODO / IN_PROGRESS)
- **Method**: `PATCH`
- **URL**: `/api/v1/tasks/{id}/status?status=IN_PROGRESS`
- **Quy tắc an toàn**: Không thể chuyển trực tiếp sang `DONE` tại endpoint này nếu task chưa có minh chứng; hệ thống sẽ yêu cầu nộp minh chứng qua endpoint `/complete`.

#### 2.5. Xem danh sách "Việc của tôi" (My Tasks)
- **Method**: `GET`
- **URL**: `/api/v1/tasks/my-tasks?page=0&size=10`
- **Quyền**: `ADMIN` hoặc `EMPLOYEE`
- **Response** (`200 OK`): Tự động trích xuất người dùng từ JWT Token để lọc các công việc được giao cho chính mình.

#### 2.6. Phân công công việc / Tự nhận việc (Assign Task)
- **Method**: `PATCH`
- **URL**: `/api/v1/tasks/{id}/assign?userId=2`
- **Quyền**: `ADMIN` (có thể giao cho bất kỳ ai) hoặc `EMPLOYEE` (tự nhận việc nếu bỏ trống `userId`). Chặn phân công cho Khách hàng (`CLIENT`).
- **Response** (`200 OK`): Cập nhật thông tin `assigneeId` và `assigneeName`.

---

### 3. Tính Năng Thông Minh (Smart Features)

#### 3.1. Danh sách công việc sắp xếp theo Điểm Ưu Tiên
- **Method**: `GET`
- **URL**: `/api/v1/tasks/smart-priority`
- **Response** (`200 OK`):
  ```json
  {
    "success": true,
    "message": "Lấy danh sách công việc ưu tiên thông minh thành công",
    "data": [
      {
        "task": {
          "id": 2,
          "title": "Tích hợp Cổng thanh toán VnPay",
          "priority": "URGENT",
          "status": "IN_PROGRESS",
          "overdue": true,
          "progress": 66.7
        },
        "priorityScore": 80,
        "urgencyScore": 105,
        "statusModifier": 15,
        "totalScore": 200,
        "scoreExplanation": "Ưu tiên URGENT (+80) | Quá hạn 1 ngày (+105 điểm) | Đang làm dở (IN_PROGRESS: +15 điểm) => Tổng: 200 điểm"
      }
    ]
  }
  ```

#### 3.2. Tạo nhanh công việc từ câu tự nhiên (NLP)
- **Method**: `POST`
- **URL**: `/api/v1/tasks/quick-parse`
- **Request Body**:
  ```json
  {
    "text": "Họp triển khai dự án lúc 14:00 ngày mai mức độ URGENT",
    "projectId": 1
  }
  ```
- **Response** (`201 Created`): Bóc tách thành công tiêu đề `"Họp triển khai dự án"`, hạn chót 14:00 ngày mai, mức ưu tiên `URGENT`.

---

### 4. Thống Kê & Báo Cáo (`/api/v1/analytics/dashboard`)

- **Method**: `GET`
- **URL**: `/api/v1/analytics/dashboard`
- **Quy tắc cách ly**: Tài khoản Khách hàng (`CLIENT`) chỉ nhận số liệu thống kê thuộc các dự án của riêng mình.
- **Response** (`200 OK`):
  ```json
  {
    "success": true,
    "message": "Lấy dữ liệu thống kê thành công",
    "data": {
      "totalProjects": 3,
      "totalTasks": 7,
      "tasksByStatus": {
        "TODO": 3,
        "IN_PROGRESS": 2,
        "DONE": 2
      },
      "overdueTasks": 1,
      "overallCompletionRate": 28.6,
      "projectSummaries": [
        {
          "projectId": 1,
          "projectName": "Xây dựng Nền tảng E-Commerce",
          "totalTasks": 3,
          "completedTasks": 1,
          "completionRate": 33.3
        }
      ]
    }
  }
  ```

---

### 5. Định Dạng Lỗi Thống Nhất (`GlobalExceptionHandler`)

Mọi lỗi ngoại lệ (400, 401, 403, 404, 500) đều được xử lý tập trung và trả về cấu trúc JSON nhất quán:
```json
{
  "timestamp": "2026-11-04T23:30:00",
  "status": 400,
  "error": "Yêu cầu không hợp lệ (Bad Request)",
  "message": "Bắt buộc phải cung cấp minh chứng hoàn thành công việc (nội dung báo cáo hoặc link minh chứng)",
  "path": "/api/v1/tasks/2/complete"
}
```

Các mã HTTP status tiêu chuẩn được áp dụng:
- `200 OK`: Thành công với dữ liệu trả về.
- `201 Created`: Tạo mới thành công (Task, Project, User, Subtask).
- `204 No Content`: Xóa thành công (Delete Task, Delete Project, Delete Subtask).
- `400 Bad Request`: Thiếu minh chứng hoàn thành, vi phạm ràng buộc dữ liệu hoặc lỗi validation.
- `401 Unauthorized`: Chưa cung cấp Bearer Token hoặc token không hợp lệ.
- `403 Forbidden`: Người dùng không có đủ quyền thực hiện thao tác.
- `404 Not Found`: Không tìm thấy thực thể theo ID.
- `500 Internal Server Error`: Lỗi hệ thống phát sinh ngoài ý muốn.

---

## 🔮 Hướng Cải Tiến Trong Tương Lai

1. **Thông Báo Thời Gian Thực (Real-time Notifications)**:
   - Tích hợp **Spring WebSocket (STOMP)** hoặc **Server-Sent Events (SSE)** để cập nhật tiến độ công việc và trạng thái Kanban theo thời gian thực khi đồng đội nộp minh chứng hoặc chuyển việc.

2. **Mở Rộng Tính Năng NLP Với Generative AI**:
   - Tích hợp Spring AI / Gemini API để phân tích ngữ nghĩa sâu hơn từ các đoạn email trao đổi với khách hàng thành danh sách nhiều task cụ thể.

3. **Nhắc Việc Tự Động (Scheduler & Email Notification)**:
   - Sử dụng `@Scheduled` quét định kỳ các công việc sắp đến hạn hoặc quá hạn để gửi thông báo qua Email (Spring Mail) hoặc Slack / Telegram Webhook.

4. **Multi-Tenancy & Đăng Nhập Mạng Xã Hội (OAuth2)**:
   - Hỗ trợ kiến trúc đa người thuê (Multi-Tenancy) và tích hợp đăng nhập qua Google / GitHub bằng Spring Security OAuth2 Client.
