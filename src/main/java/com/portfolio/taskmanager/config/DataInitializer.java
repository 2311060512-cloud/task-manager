package com.portfolio.taskmanager.config;

import com.portfolio.taskmanager.entity.Project;
import com.portfolio.taskmanager.entity.Subtask;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.User;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.entity.enums.UserRole;
import com.portfolio.taskmanager.repository.ProjectRepository;
import com.portfolio.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Profile("dev")
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Dữ liệu người dùng mẫu đã tồn tại, bỏ qua DataInitializer.");
            return;
        }

        log.info("Bắt đầu nạp dữ liệu người dùng & phân quyền 3 Role cho môi trường DEV...");

        // 1. Tạo Users mẫu
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Quản Trị Viên Hệ Thống")
                .email("admin@taskmanager.com")
                .role(UserRole.ADMIN)
                .build();

        User devNam = User.builder()
                .username("dev_nam")
                .password(passwordEncoder.encode("dev123"))
                .fullName("Nguyễn Văn Nam (Tech Lead)")
                .email("nam.nv@techcorp.com")
                .role(UserRole.EMPLOYEE)
                .build();

        User devHoa = User.builder()
                .username("dev_hoa")
                .password(passwordEncoder.encode("dev123"))
                .fullName("Trần Thị Hoa (Senior Backend)")
                .email("hoa.tt@techcorp.com")
                .role(UserRole.EMPLOYEE)
                .build();

        User clientVinfast = User.builder()
                .username("client_vinfast")
                .password(passwordEncoder.encode("client123"))
                .fullName("VinFast Corporation (Khách Hàng)")
                .email("contact@vinfast.vn")
                .role(UserRole.CLIENT)
                .build();

        User clientFpt = User.builder()
                .username("client_fpt")
                .password(passwordEncoder.encode("client123"))
                .fullName("FPT Software (Khách Hàng)")
                .email("partner@fpt.com")
                .role(UserRole.CLIENT)
                .build();

        userRepository.saveAll(List.of(admin, devNam, devHoa, clientVinfast, clientFpt));
        log.info("Đã tạo 5 tài khoản mẫu: admin (ADMIN), dev_nam & dev_hoa (EMPLOYEE), client_vinfast & client_fpt (CLIENT)");

        if (projectRepository.count() > 0) {
            return;
        }

        // 2. Dự án E-Commerce (Khách hàng: VinFast, Thành viên: Nam + Hoa)
        Project ecommerce = Project.builder()
                .name("Xây dựng Nền tảng E-Commerce")
                .description("Hệ thống thương mại điện tử đa kênh tích hợp thanh toán VnPay, Momo")
                .client(clientVinfast)
                .members(List.of(devNam, devHoa))
                .build();

        Task task1 = Task.builder()
                .title("Thiết kế Schema Database & ERD")
                .description("Thiết kế các bảng Users, Products, Orders, OrderItems")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.DONE)
                .dueDate(LocalDateTime.now().minusDays(5))
                .project(ecommerce)
                .assignee(devNam)
                .creator(admin)
                .completionNote("Đã thiết kế chuẩn 3NF, xuất file DDL và import migration Flyway thành công 100%.")
                .proofUrl("https://github.com/techcorp/ecommerce-schema/pull/12")
                .completedAt(LocalDateTime.now().minusDays(5))
                .completedBy(devNam)
                .build();
        task1.addSubtask(Subtask.builder().title("Thiết kế bảng Users và Roles").completed(true).task(task1).build());
        task1.addSubtask(Subtask.builder().title("Thiết kế bảng Products và Categories").completed(true).task(task1).build());
        task1.addSubtask(Subtask.builder().title("Thiết kế bảng Orders và Payments").completed(true).task(task1).build());

        Task task2 = Task.builder()
                .title("Tích hợp Cổng thanh toán VnPay")
                .description("Xây dựng module thanh toán trực tuyến và webhook callback")
                .priority(TaskPriority.URGENT)
                .status(TaskStatus.IN_PROGRESS)
                .dueDate(LocalDateTime.now().minusDays(1)) // Overdue!
                .project(ecommerce)
                .assignee(devHoa)
                .creator(devNam)
                .build();
        task2.addSubtask(Subtask.builder().title("Tạo tài khoản Sandbox VnPay").completed(true).task(task2).build());
        task2.addSubtask(Subtask.builder().title("Viết API tạo URL thanh toán").completed(true).task(task2).build());
        task2.addSubtask(Subtask.builder().title("Xử lý IPN Verify Chữ ký điện tử").completed(false).task(task2).build());

        Task task3 = Task.builder()
                .title("Tối ưu hóa giỏ hàng Redis Caching")
                .description("Lưu trữ tạm giỏ hàng người dùng trên Redis để tăng tốc độ phản hồi")
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .dueDate(LocalDateTime.now().plusDays(3))
                .project(ecommerce)
                .assignee(devNam)
                .creator(clientVinfast)
                .build();

        ecommerce.setTasks(List.of(task1, task2, task3));
        projectRepository.save(ecommerce);

        // 3. Dự án DevOps & Hạ tầng (Khách hàng: FPT, Phụ trách: Nam)
        Project devops = Project.builder()
                .name("Nâng cấp Hạ tầng & CI/CD")
                .description("Thiết lập hệ thống CI/CD Pipeline với GitHub Actions và Docker Swarm")
                .client(clientFpt)
                .members(List.of(devNam))
                .build();

        Task task4 = Task.builder()
                .title("Viết Dockerfile và Docker-Compose chuẩn production")
                .description("Multi-stage build cho backend Spring Boot và cấu hình PostgreSQL")
                .priority(TaskPriority.URGENT)
                .status(TaskStatus.IN_PROGRESS)
                .dueDate(LocalDateTime.now().plusDays(1))
                .project(devops)
                .assignee(devNam)
                .creator(admin)
                .build();
        task4.addSubtask(Subtask.builder().title("Tạo Dockerfile tối ưu size").completed(true).task(task4).build());
        task4.addSubtask(Subtask.builder().title("Cấu hình docker-compose.yml kèm healthcheck").completed(false).task(task4).build());

        Task task5 = Task.builder()
                .title("Thiết lập GitHub Actions CI Pipeline")
                .description("Tự động chạy unit test và sonar analysis khi mở Pull Request")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .dueDate(LocalDateTime.now().plusDays(5))
                .project(devops)
                .assignee(devNam)
                .creator(devNam)
                .build();

        devops.setTasks(List.of(task4, task5));
        projectRepository.save(devops);

        // 4. Dự án Mobile App (Khách hàng: VinFast, Phụ trách: Hoa)
        Project mobileApp = Project.builder()
                .name("Ứng dụng Di động Khách hàng")
                .description("Phát triển app React Native cho iOS và Android")
                .client(clientVinfast)
                .members(List.of(devHoa))
                .build();

        Task task6 = Task.builder()
                .title("Nghiên cứu kiến trúc Flutter vs React Native")
                .description("Đánh giá hiệu năng và đưa ra quyết định kiến trúc")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.DONE)
                .dueDate(LocalDateTime.now().minusDays(10))
                .project(mobileApp)
                .assignee(devHoa)
                .creator(admin)
                .build();

        Task task7 = Task.builder()
                .title("Xây dựng màn hình đăng nhập và xác thực OTP")
                .description("Giao diện UI/UX và kết nối API xác thực")
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .dueDate(LocalDateTime.now().plusDays(8))
                .project(mobileApp)
                .assignee(devHoa)
                .creator(clientVinfast)
                .build();

        mobileApp.setTasks(List.of(task6, task7));
        projectRepository.save(mobileApp);

        log.info("Nạp dữ liệu mẫu DEV hoàn tất thành công! 5 tài khoản (ADMIN, EMPLOYEE, CLIENT) và 3 dự án đã sẵn sàng.");
    }
}
