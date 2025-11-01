package com.example.qlquancoffe.controllers;

import com.example.qlquancoffe.dao.HoaDonDAO;
import com.example.qlquancoffe.dao.SanPhamDAO;
import com.example.qlquancoffe.models.TaiKhoan;
import com.example.qlquancoffe.utils.CurrencyUtil;
import com.example.qlquancoffe.utils.SceneSwitcher;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller cho màn hình chính (MainView)
 * PHÂN QUYỀN:
 * - QUẢN LÝ: Dashboard, Sản phẩm, Nhân viên, Doanh thu, Lịch sử HĐ
 * - NHÂN VIÊN: Bán hàng, Lịch sử bán
 */
public class MainController {

    // ==================== TOP BAR ====================
    @FXML private Label lblSubtitle;
    @FXML private Label lblDateTime;
    @FXML private Label lblDayOfWeek;
    @FXML private Label lblUsername;
    @FXML private Label lblRole;
    @FXML private Button btnLogout;

    // ==================== SIDEBAR ====================
    @FXML private VBox sidebar;
    @FXML private VBox menuQuanLy;      // Menu dành cho Quản lý
    @FXML private VBox menuNhanVien;    // Menu dành cho Nhân viên

    // Menu buttons - Quản lý
    @FXML private Button btnDashboard;
    @FXML private Button btnSanPham;
    @FXML private Button btnNhanVien;
    @FXML private Button btnDoanhThu;
    @FXML private Button btnLichSuHoaDon;

    // Menu buttons - Nhân viên
    @FXML private Button btnBanHang;
    @FXML private Button btnLichSuBan;

    // Menu buttons - Chung
    @FXML private Button btnHuongDan;

    // ==================== CENTER ====================
    @FXML private StackPane contentArea;
    @FXML private VBox defaultContent;

    @FXML private Label lblWelcome;
    @FXML private HBox statsContainer;          // Stats chỉ cho Quản lý
    @FXML private Label lblDoanhThuHomNay;
    @FXML private Label lblSoDonHang;
    @FXML private Label lblSanPhamSapHet;

    @FXML private HBox quickActionsQuanLy;     // Quick actions cho Quản lý
    @FXML private HBox quickActionsNhanVien;   // Quick actions cho Nhân viên

    // ==================== BOTTOM BAR ====================
    @FXML private Label lblStatus;
    @FXML private Label lblVersion;

    // ==================== FIELDS ====================
    private TaiKhoan currentUser;
    private Button currentActiveButton;
    private List<Button> menuButtons;
    private Timeline clockTimeline;

    /**
     * Khởi tạo controller
     */
    @FXML
    public void initialize() {
        System.out.println("🏠 Khởi tạo MainController");

        // Lấy thông tin user
        currentUser = SceneSwitcher.getCurrentUser();

        if (currentUser == null) {
            System.err.println("❌ Không tìm thấy thông tin user!");
            SceneSwitcher.switchToLogin();
            return;
        }

        // Khởi tạo UI
        initializeUserInfo();
        initializeMenuByRole();  // ← PHÂN QUYỀN Ở ĐÂY
        initializeClock();

        // Load stats nếu là Quản lý
        if (currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy) {
            loadDashboardStats();
        }

        System.out.println("✅ MainController đã sẵn sàng!");
    }

    /**
     * Khởi tạo thông tin user
     */
    private void initializeUserInfo() {
        lblUsername.setText(currentUser.getHoTen());
        lblWelcome.setText("Chào mừng " + currentUser.getHoTen() + " đến với hệ thống!");

        if (currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy) {
            lblRole.setText("Quản lý");
            lblRole.setStyle("-fx-text-fill: #e74c3c;");
            lblSubtitle.setText("Hệ thống Quản lý");
        } else {
            lblRole.setText("Nhân viên");
            lblRole.setStyle("-fx-text-fill: #3498db;");
            lblSubtitle.setText("Hệ thống POS - Bán hàng");
        }

        System.out.println("✅ Đã load thông tin user: " + currentUser.getHoTen());
    }

    /**
     * Khởi tạo menu theo vai trò
     */
    private void initializeMenuByRole() {
        menuButtons = new ArrayList<>();

        if (currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy) {
            // ==================== QUẢN LÝ ====================
            System.out.println("👔 Khởi tạo menu QUẢN LÝ");

            // Hiển thị menu Quản lý
            menuQuanLy.setVisible(true);
            menuQuanLy.setManaged(true);

            // Ẩn menu Nhân viên
            menuNhanVien.setVisible(false);
            menuNhanVien.setManaged(false);

            // Hiển thị stats và quick actions
            statsContainer.setVisible(true);
            statsContainer.setManaged(true);
            quickActionsQuanLy.setVisible(true);
            quickActionsQuanLy.setManaged(true);
            quickActionsNhanVien.setVisible(false);
            quickActionsNhanVien.setManaged(false);

            // Thêm buttons vào list
            menuButtons.add(btnDashboard);
            menuButtons.add(btnSanPham);
            menuButtons.add(btnNhanVien);
            menuButtons.add(btnDoanhThu);
            menuButtons.add(btnLichSuHoaDon);

            // Set Dashboard làm active mặc định
            setActiveButton(btnDashboard);

        } else {
            // ==================== NHÂN VIÊN ====================
            System.out.println("👤 Khởi tạo menu NHÂN VIÊN");

            // Ẩn menu Quản lý
            menuQuanLy.setVisible(false);
            menuQuanLy.setManaged(false);

            // Hiển thị menu Nhân viên
            menuNhanVien.setVisible(true);
            menuNhanVien.setManaged(true);

            // Ẩn stats, hiển thị quick actions nhân viên
            statsContainer.setVisible(false);
            statsContainer.setManaged(false);
            quickActionsQuanLy.setVisible(false);
            quickActionsQuanLy.setManaged(false);
            quickActionsNhanVien.setVisible(true);
            quickActionsNhanVien.setManaged(true);

            // Thêm buttons vào list
            menuButtons.add(btnBanHang);
            menuButtons.add(btnLichSuBan);

            // Set Bán hàng làm active mặc định
            setActiveButton(btnBanHang);
        }

        // Menu chung cho cả 2
        menuButtons.add(btnHuongDan);
    }

    /**
     * Khởi tạo đồng hồ real-time
     */
    private void initializeClock() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");

        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            lblDateTime.setText(now.format(timeFormatter));

            String day = now.format(dayFormatter);
            String viDay = switch(day) {
                case "Monday" -> "Thứ Hai";
                case "Tuesday" -> "Thứ Ba";
                case "Wednesday" -> "Thứ Tư";
                case "Thursday" -> "Thứ Năm";
                case "Friday" -> "Thứ Sáu";
                case "Saturday" -> "Thứ Bảy";
                case "Sunday" -> "Chủ Nhật";
                default -> day;
            };
            lblDayOfWeek.setText(viDay);
        }));

        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    /**
     * Load thống kê Dashboard (chỉ cho Quản lý)
     */
    private void loadDashboardStats() {
        new Thread(() -> {
            try {
                HoaDonDAO hoaDonDAO = new HoaDonDAO();
                SanPhamDAO sanPhamDAO = new SanPhamDAO();

                BigDecimal doanhThu = hoaDonDAO.getTongDoanhThuByDate(LocalDate.now());
                int soDon = hoaDonDAO.countHoaDonByDate(LocalDate.now());
                int sanPhamSapHet = sanPhamDAO.getSanPhamSapHet(10).size();

                Platform.runLater(() -> {
                    lblDoanhThuHomNay.setText(CurrencyUtil.formatVND(doanhThu));
                    lblSoDonHang.setText(soDon + " đơn");
                    lblSanPhamSapHet.setText(sanPhamSapHet + " SP");
                });

            } catch (Exception e) {
                System.err.println("❌ Lỗi load thống kê: " + e.getMessage());
            }
        }).start();
    }

    // ==================== MENU ACTIONS - QUẢN LÝ ====================

    @FXML
    private void loadDashboard() {
        setActiveButton(btnDashboard);
        showDefaultContent();
        loadDashboardStats();
    }

    @FXML
    private void loadSanPham() {
        setActiveButton(btnSanPham);
        loadView("quanly/SanPhamView.fxml", "Quản lý Sản phẩm");
    }

    @FXML
    private void loadNhanVien() {
        setActiveButton(btnNhanVien);
        loadView("quanly/NhanVienView.fxml", "Quản lý Nhân viên");
    }

    @FXML
    private void loadDoanhThu() {
        setActiveButton(btnDoanhThu);
        loadView("quanly/DoanhThuView.fxml", "Báo cáo Doanh thu");
    }

    @FXML
    private void loadLichSuHoaDon() {
        setActiveButton(btnLichSuHoaDon);
        loadView("quanly/LichSuHoaDonView.fxml", "Lịch sử Hóa đơn");
    }

    // ==================== MENU ACTIONS - NHÂN VIÊN ====================

    @FXML
    private void loadBanHang() {
        setActiveButton(btnBanHang);
        loadView("nhanvien/BanHangView.fxml", "Bán hàng - POS");
    }

    @FXML
    private void loadLichSuBan() {
        setActiveButton(btnLichSuBan);
        loadView("nhanvien/LichSuBanView.fxml", "Lịch sử bán hàng");
    }

    // ==================== MENU ACTIONS - CHUNG ====================

    @FXML
    private void showHelp() {
        String helpText = currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy
                ? "📖 HƯỚNG DẪN QUẢN LÝ\n\n" +
                "1. Dashboard: Xem tổng quan hệ thống\n" +
                "2. Sản phẩm: Quản lý danh mục sản phẩm\n" +
                "3. Nhân viên: Quản lý tài khoản nhân viên\n" +
                "4. Doanh thu: Xem báo cáo kinh doanh\n" +
                "5. Lịch sử HĐ: Xem tất cả hóa đơn\n\n" +
                "Liên hệ: admin@coffee.com"
                : "📖 HƯỚNG DẪN SỬ DỤNG\n\n" +
                "1. Bán hàng: Tạo đơn hàng mới\n" +
                "2. Lịch sử bán: Xem các đơn đã tạo\n\n" +
                "Liên hệ hỗ trợ: support@coffee.com";

        SceneSwitcher.showInfoAlert("Hướng dẫn sử dụng", helpText);
    }

    @FXML
    private void handleLogout() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        SceneSwitcher.logout();
    }

    @FXML
    private void onLogoutHover() {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), btnLogout);
        st.setToX(1.1);
        st.setToY(1.1);
        st.play();
    }

    @FXML
    private void onLogoutExit() {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), btnLogout);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    // ==================== HELPER METHODS ====================

    private void loadView(String fxmlPath, String viewName) {
        try {
            System.out.println("📂 Đang load view: " + fxmlPath);

            defaultContent.setVisible(false);
            defaultContent.setManaged(false);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/qlquancoffe/views/" + fxmlPath)
            );
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            FadeTransition fade = new FadeTransition(Duration.millis(300), view);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

            System.out.println("✅ Đã load view: " + viewName);

        } catch (IOException e) {
            System.err.println("❌ Lỗi load view: " + fxmlPath);
            e.printStackTrace();
            showPlaceholder(viewName);
        }
    }

    private void showDefaultContent() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(defaultContent);
        defaultContent.setVisible(true);
        defaultContent.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(300), defaultContent);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void showPlaceholder(String viewName) {
        VBox placeholder = new VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setStyle("-fx-padding: 50;");

        Label icon = new Label("🚧");
        icon.setStyle("-fx-font-size: 80px;");

        Label title = new Label(viewName);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label message = new Label("Màn hình này đang được phát triển...");
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Button btnBack = new Button("🏠 Về Dashboard");
        btnBack.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");

        if (currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy) {
            btnBack.setOnAction(e -> loadDashboard());
        } else {
            btnBack.setText("🛒 Về Bán hàng");
            btnBack.setOnAction(e -> loadBanHang());
        }

        placeholder.getChildren().addAll(icon, title, message, btnBack);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(placeholder);

        FadeTransition fade = new FadeTransition(Duration.millis(300), placeholder);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void setActiveButton(Button button) {
        for (Button btn : menuButtons) {
            btn.setStyle(btn.getStyle().replace("-fx-background-color: #3498db;", "-fx-background-color: transparent;")
                    .replace("-fx-text-fill: white;", "-fx-text-fill: #2c3e50;"));
        }

        if (button != null && button != btnBanHang) { // btnBanHang có style riêng
            String currentStyle = button.getStyle();
            if (!currentStyle.contains("-fx-background-color: #27ae60")) {
                button.setStyle(currentStyle + "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            currentActiveButton = button;
        }
    }
}