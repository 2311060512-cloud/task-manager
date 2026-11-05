package com.portfolio.taskmanager.config;

import com.portfolio.taskmanager.entity.Project;
import com.portfolio.taskmanager.entity.Subtask;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.User;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.repository.ProjectRepository;
import com.portfolio.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeder nạp dữ liệu mẫu kế hoạch học tập 14 ngày môn Lý luận Nhà nước và Pháp luật.
 * Chỉ kích hoạt trên profile 'dev' và đảm bảo tính Idempotent (không tạo trùng lặp nếu dự án đã tồn tại).
 */
@Slf4j
@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
public class StudyPlanSeeder implements CommandLineRunner {

    public static final String PROJECT_NAME = "Ôn tập Lý luận Nhà nước và Pháp luật";

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedStudyPlan();
    }

    @Transactional
    public void seedStudyPlan() {
        if (projectRepository.existsByNameIgnoreCase(PROJECT_NAME)) {
            log.info("Dự án '{}' đã tồn tại, bỏ qua StudyPlanSeeder.", PROJECT_NAME);
            return;
        }

        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            log.warn("Không tìm thấy tài khoản 'admin' để gán kế hoạch học tập. Bỏ qua StudyPlanSeeder.");
            return;
        }

        log.info("Bắt đầu nạp kế hoạch học tập 14 ngày '{}' cho tài khoản admin...", PROJECT_NAME);

        Project studyProject = Project.builder()
                .name(PROJECT_NAME)
                .description("Kế hoạch học tập và ôn luyện 14 ngày môn Lý luận Nhà nước và Pháp luật chuẩn bị cho kỳ thi kết thúc học phần.")
                .members(new ArrayList<>(List.of(admin)))
                .tasks(new ArrayList<>())
                .build();

        // Danh sách 14 ngày học tập
        // Ngày 1 (06/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 1: Nguồn gốc và khái niệm nhà nước",
                "Mục tiêu: Hiểu nhà nước xuất hiện từ đâu và định nghĩa nhà nước.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận chung về Nhà nước và Pháp luật (Chương nguồn gốc và khái niệm nhà nước).",
                LocalDateTime.of(2026, 11, 6, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc lý thuyết về nguồn gốc nhà nước (thuyết thần học, gia trưởng, khế ước xã hội, bạo lực, duy vật lịch sử)",
                        "Ghi định nghĩa nhà nước theo giáo trình",
                        "Lập bảng so sánh các học thuyết",
                        "Tự kiểm tra bằng 5 câu hỏi"
                )
        );

        // Ngày 2 (07/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 2: Bản chất và đặc trưng của nhà nước",
                "Mục tiêu: Phân biệt bản chất giai cấp và bản chất xã hội của nhà nước.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận Nhà nước và Pháp luật (Chương bản chất và đặc trưng của nhà nước).",
                LocalDateTime.of(2026, 11, 7, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về bản chất nhà nước",
                        "Liệt kê đặc trưng của nhà nước (quyền lực công, lãnh thổ, dân cư, chủ quyền, bộ máy cưỡng chế)",
                        "Viết tóm tắt 1 trang",
                        "Làm 5 câu hỏi tự kiểm tra"
                )
        );

        // Ngày 3 (08/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 3: Chức năng của nhà nước",
                "Mục tiêu: Phân biệt chức năng đối nội và đối ngoại, chức năng giai cấp và chức năng xã hội.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận Nhà nước và Pháp luật và các tài liệu tham khảo về chức năng quản lý nhà nước.",
                LocalDateTime.of(2026, 11, 8, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về các chức năng",
                        "Lập sơ đồ phân loại chức năng",
                        "Nêu ví dụ thực tế ở Việt Nam cho từng chức năng",
                        "Ôn lại các khái niệm đã học"
                )
        );

        // Ngày 4 (09/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 4: Hình thức nhà nước",
                "Mục tiêu: Hiểu hình thức chính thể, hình thức cấu trúc và chế độ chính trị.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận chung về Nhà nước và Pháp luật (Chương hình thức nhà nước).",
                LocalDateTime.of(2026, 11, 9, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về hình thức chính thể (quân chủ, cộng hòa)",
                        "Đọc về hình thức cấu trúc (đơn nhất, liên bang)",
                        "Đọc về chế độ chính trị",
                        "Lập bảng so sánh các hình thức"
                )
        );

        // Ngày 5 (10/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 5: Bộ máy nhà nước và nguyên tắc tổ chức",
                "Mục tiêu: Nắm các cơ quan quyền lực, hành pháp, tư pháp và nguyên tắc tổ chức.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận Nhà nước và Pháp luật; Hiến pháp Việt Nam.",
                LocalDateTime.of(2026, 11, 10, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về các cơ quan nhà nước",
                        "Vẽ sơ đồ bộ máy nhà nước Việt Nam",
                        "Nêu nguyên tắc tập trung dân chủ và nguyên tắc phân công phối hợp",
                        "Làm bài tập tự kiểm tra"
                )
        );

        // Ngày 6 (11/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 6: Khái niệm và bản chất pháp luật",
                "Mục tiêu: Định nghĩa pháp luật và phân biệt với đạo đức, tập quán.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận chung về Nhà nước và Pháp luật (Phần lý luận về pháp luật).",
                LocalDateTime.of(2026, 11, 11, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc định nghĩa pháp luật",
                        "So sánh pháp luật với đạo đức và tập quán",
                        "Ghi lại các đặc trưng của pháp luật",
                        "Viết đoạn ngắn về bản chất pháp luật"
                )
        );

        // Ngày 7 (12/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 7: Hình thức pháp luật và hệ thống pháp luật",
                "Mục tiêu: Hiểu các loại văn bản quy phạm pháp luật và cấu trúc hệ thống pháp luật.\n" +
                "Tài liệu gợi ý: Luật Ban hành văn bản quy phạm pháp luật và giáo trình liên quan.",
                LocalDateTime.of(2026, 11, 12, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về các hình thức pháp luật (tập quán, tiền lệ, văn bản quy phạm pháp luật)",
                        "Lập sơ đồ hệ thống pháp luật",
                        "Phân biệt văn bản quy phạm pháp luật với văn bản hành chính",
                        "Làm bài tập phân loại văn bản"
                )
        );

        // Ngày 8 (13/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 8: Quy phạm pháp luật",
                "Mục tiêu: Nắm cấu thành của quy phạm pháp luật và các loại quy phạm.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận Nhà nước và Pháp luật (Chương quy phạm pháp luật).",
                LocalDateTime.of(2026, 11, 13, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về cấu thành quy phạm (giả định, quy định, chế tài)",
                        "Phân tích ba ví dụ quy phạm có thật",
                        "Phân loại quy phạm theo nội dung",
                        "Viết lại một quy phạm theo cấu trúc ba phần"
                )
        );

        // Ngày 9 (14/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 9: Quan hệ pháp luật",
                "Mục tiêu: Hiểu chủ thể, khách thể và nội dung của quan hệ pháp luật.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận chung về Nhà nước và Pháp luật (Chương quan hệ pháp luật).",
                LocalDateTime.of(2026, 11, 14, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về các yếu tố của quan hệ pháp luật",
                        "Phân tích một tình huống thực tế để xác định chủ thể và quyền nghĩa vụ",
                        "Phân biệt quan hệ pháp luật với quan hệ xã hội",
                        "Làm bài tập tình huống"
                )
        );

        // Ngày 10 (15/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 10: Thực hiện pháp luật",
                "Mục tiêu: Nắm bốn hình thức thực hiện pháp luật và đặc điểm áp dụng pháp luật.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận Nhà nước và Pháp luật (Chương thực hiện và áp dụng pháp luật).",
                LocalDateTime.of(2026, 11, 15, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về tuân thủ, thi hành, sử dụng và áp dụng pháp luật",
                        "Phân biệt các hình thức với nhau",
                        "Nêu ví dụ cho từng hình thức",
                        "Ôn lại các bước áp dụng pháp luật"
                )
        );

        // Ngày 11 (16/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 11: Vi phạm pháp luật và trách nhiệm pháp lý",
                "Mục tiêu: Nắm cấu thành vi phạm pháp luật và các loại trách nhiệm pháp lý.\n" +
                "Tài liệu gợi ý: Giáo trình Lý luận Nhà nước và Pháp luật (Chương vi phạm pháp luật và trách nhiệm pháp lý).",
                LocalDateTime.of(2026, 11, 16, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về bốn yếu tố cấu thành vi phạm",
                        "Phân biệt trách nhiệm hình sự, hành chính, dân sự, kỷ luật",
                        "Phân tích một tình huống vi phạm",
                        "Lập bảng so sánh các loại trách nhiệm"
                )
        );

        // Ngày 12 (17/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 12: Nhà nước pháp quyền xã hội chủ nghĩa Việt Nam",
                "Mục tiêu: Hiểu đặc trưng và nguyên tắc của nhà nước pháp quyền XHCN Việt Nam.\n" +
                "Tài liệu gợi ý: Giáo trình và các văn kiện, nghị quyết về xây dựng Nhà nước pháp quyền XHCN Việt Nam.",
                LocalDateTime.of(2026, 11, 17, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về khái niệm nhà nước pháp quyền",
                        "Liệt kê các đặc trưng và nguyên tắc",
                        "Phân biệt nhà nước pháp quyền với nhà nước pháp trị",
                        "Viết tóm tắt 1 trang"
                )
        );

        // Ngày 13 (18/11/2026)
        createDayTask(
                studyProject,
                admin,
                "Ngày 13: Hiến pháp và các nguyên tắc hiến định",
                "Mục tiêu: Nắm vai trò của Hiến pháp và các nội dung cơ bản của Hiến pháp 2013.\n" +
                "Tài liệu gợi ý: Hiến pháp nước Cộng hòa Xã hội Chủ nghĩa Việt Nam năm 2013.",
                LocalDateTime.of(2026, 11, 18, 21, 0, 0),
                TaskPriority.MEDIUM,
                List.of(
                        "Đọc về vị trí pháp lý của Hiến pháp",
                        "Liệt kê các chế định chính của Hiến pháp 2013",
                        "Ghi chú các nguyên tắc hiến định (quyền con người, quyền lực nhà nước thống nhất)",
                        "Làm bài tập tự kiểm tra"
                )
        );

        // Ngày 14 (19/11/2026) - Ôn tập tổng hợp & Kiểm tra -> HIGH priority
        createDayTask(
                studyProject,
                admin,
                "Ngày 14: Ôn tập tổng hợp và kiểm tra",
                "Mục tiêu: Hệ thống lại toàn bộ kiến thức 13 ngày trước và tự đánh giá.\n" +
                "Tài liệu gợi ý: Toàn bộ đề cương, sơ đồ tư duy và bộ câu hỏi trắc nghiệm/tự luận môn Lý luận Nhà nước và Pháp luật.",
                LocalDateTime.of(2026, 11, 19, 21, 0, 0),
                TaskPriority.HIGH,
                List.of(
                        "Đọc lại các sơ đồ và bảng so sánh đã lập",
                        "Làm đề kiểm tra tổng hợp 30 câu",
                        "Chấm điểm và liệt kê phần còn yếu",
                        "Lập kế hoạch ôn lại cho phần yếu"
                )
        );

        projectRepository.save(studyProject);
        log.info("Đã nạp thành công kế hoạch học tập 14 ngày '{}' với 14 task và đầy đủ subtask cho admin.", PROJECT_NAME);
    }

    private void createDayTask(
            Project project,
            User admin,
            String title,
            String description,
            LocalDateTime dueDate,
            TaskPriority priority,
            List<String> subtaskTitles
    ) {
        Task task = Task.builder()
                .title(title)
                .description(description)
                .dueDate(dueDate)
                .priority(priority)
                .status(TaskStatus.TODO)
                .project(project)
                .assignee(admin)
                .creator(admin)
                .build();

        for (String stTitle : subtaskTitles) {
            task.addSubtask(Subtask.builder()
                    .title(stTitle)
                    .completed(false)
                    .task(task)
                    .build());
        }

        project.addTask(task);
    }
}

