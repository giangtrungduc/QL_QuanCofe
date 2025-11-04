package com.example.qlquancoffe.controllers;

import com.example.qlquancoffe.dao.HoaDonDAO;
import com.example.qlquancoffe.dao.SanPhamDAO; // ✅ SỬA: Xóa import này
import com.example.qlquancoffe.models.TaiKhoan;
import com.example.qlquancoffe.utils.CurrencyUtil;
import com.example.qlquancoffe.utils.DialogUtils; // ✅ SỬA: Thêm import
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
    @FXML private VBox menuQuanLy;
    @FXML private VBox menuNhanVien;
    @FXML private Button btnDashboardQL;
    @FXML private Button btnSanPham;
    @FXML private Button btnNhanVien;
    @FXML private Button btnDoanhThu;
    @FXML private Button btnLichSuHoaDon;
    @FXML private Button btnDashboardNV;
    @FXML private Button btnBanHang;
    @FXML private Button btnLichSuBan;
    @FXML private Button btnHuongDan;
    // ==================== CENTER ====================
    @FXML private StackPane contentArea;
    @FXML private VBox defaultContent;
    @FXML private Label lblWelcome;
    @FXML private HBox statsContainerQL;
    @FXML private HBox statsContainerNV;
    @FXML private Label lblDoanhThuHomNayQL;
    @FXML private Label lblDoanhThuHomNayNV;
    @FXML private Label lblSoDonHangQL;
    @FXML private Label lblSoDonHangNV;
    @FXML private Label lblSanPhamSapHet;

    @FXML private VBox statCardSapHet;

    @FXML private HBox quickActionsQuanLy;
    @FXML private HBox quickActionsNhanVien;
    // ==================== BOTTOM BAR ====================
    @FXML private Label lblStatus;
    @FXML private Label lblVersion;

    // ==================== FIELDS ====================
    private TaiKhoan currentUser;
    private Button currentActiveButton;
    private List<Button> menuButtons;
    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        System.out.println("🏠 Khởi tạo MainController");

        currentUser = SceneSwitcher.getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ Không tìm thấy thông tin user!");
            SceneSwitcher.switchToLogin();
            return;
        }

        // Khởi tạo UI
        initializeUserInfo();
        initializeMenuByRole();
        initializeClock();

        // Load stats theo vai trò
        if (currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy) {
            loadDashboardStatsQL();
        } else {
            loadDashboardStatsNV();
        }

        System.out.println("✅ MainController đã sẵn sàng!");
    }

    private void initializeUserInfo() {
        // (Hàm này đã đúng, giữ nguyên)
        lblUsername.setText(currentUser.getHoTen());
        lblWelcome.setText("Chào mừng " + currentUser.getHoTen() + " đến với hệ thống!");

        if (currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy) {
            lblRole.setText("Quản lý");
            lblRole.getStyleClass().add("role-label-manager");
            lblSubtitle.setText("Hệ thống Quản lý");
        } else {
            lblRole.setText("Nhân viên");
            lblRole.getStyleClass().add("role-label-staff");
            lblSubtitle.setText("Hệ thống POS - Bán hàng");
        }
        System.out.println("✅ Đã load thông tin user: " + currentUser.getHoTen());
    }

    private void initializeMenuByRole() {
        menuButtons = new ArrayList<>();

        if (currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy) {
            // ==================== QUẢN LÝ ====================
            System.out.println("👔 Khởi tạo menu QUẢN LÝ");
            menuQuanLy.setVisible(true);
            menuQuanLy.setManaged(true);
            menuNhanVien.setVisible(false);
            menuNhanVien.setManaged(false);
            statsContainerQL.setVisible(true);
            statsContainerQL.setManaged(true);
            statsContainerNV.setVisible(false);
            statsContainerNV.setManaged(false);
            quickActionsQuanLy.setVisible(true);
            quickActionsQuanLy.setManaged(true);
            quickActionsNhanVien.setVisible(false);
            quickActionsNhanVien.setManaged(false);
            statCardSapHet.setVisible(false);
            statCardSapHet.setManaged(false);

            menuButtons.add(btnDashboardQL);
            menuButtons.add(btnSanPham);
            menuButtons.add(btnNhanVien);
            menuButtons.add(btnDoanhThu);
            menuButtons.add(btnLichSuHoaDon);
            setActiveButton(btnDashboardQL);

        } else {
            // ==================== NHÂN VIÊN ====================
            System.out.println("👤 Khởi tạo menu NHÂN VIÊN");
            menuQuanLy.setVisible(false);
            menuQuanLy.setManaged(false);
            menuNhanVien.setVisible(true);
            menuNhanVien.setManaged(true);
            statsContainerQL.setVisible(false);
            statsContainerQL.setManaged(false);
            statsContainerNV.setVisible(true);
            statsContainerNV.setManaged(true);
            quickActionsQuanLy.setVisible(false);
            quickActionsQuanLy.setManaged(false);
            quickActionsNhanVien.setVisible(true);
            quickActionsNhanVien.setManaged(true);
            menuButtons.add(btnDashboardNV);
            menuButtons.add(btnBanHang);
            menuButtons.add(btnLichSuBan);
            setActiveButton(btnDashboardNV);
        }
        menuButtons.add(btnHuongDan);
    }

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
     * Load thống kê Dashboard (Quản lý)
     */
    private void loadDashboardStatsQL() {
        new Thread(() -> {
            try {
                HoaDonDAO hoaDonDAO = new HoaDonDAO();

                BigDecimal doanhThu = hoaDonDAO.getTongDoanhThuByDate(LocalDate.now());
                int soDon = hoaDonDAO.countHoaDonByDate(LocalDate.now());

                Platform.runLater(() -> {
                    lblDoanhThuHomNayQL.setText(CurrencyUtil.formatVND(doanhThu));
                    lblSoDonHangQL.setText(soDon + " đơn");
                });

            } catch (Exception e) {
                System.err.println("❌ Lỗi load thống kê Quản lý: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Load thống kê Dashboard (Nhân viên)
     */
    private void loadDashboardStatsNV() {
        new Thread(() -> {
            try {
                HoaDonDAO hoaDonDAO = new HoaDonDAO();
                BigDecimal doanhThu = hoaDonDAO.getTongDoanhThuNhanVien(currentUser.getIdNhanVien(), LocalDate.now());
                int soHoaDon = hoaDonDAO.countHoaDonNhanVien(currentUser.getIdNhanVien(), LocalDate.now());

                Platform.runLater(() -> {
                    lblDoanhThuHomNayNV.setText(CurrencyUtil.formatVND(doanhThu));
                    lblSoDonHangNV.setText(soHoaDon + " đơn");
                });
            } catch (Exception e) {
                System.err.println("❌ Lỗi load thống kê Nhân viên: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // ==================== MENU ACTIONS (Giữ nguyên) ====================
    @FXML private void loadDashboardQL() {
        setActiveButton(btnDashboardQL);
        showDefaultContent();
        loadDashboardStatsQL();
    }
    @FXML private void loadSanPham() {
        setActiveButton(btnSanPham);
        loadView("quanly/SanPhamView.fxml", "Quản lý Sản phẩm");
    }
    @FXML private void loadNhanVien() {
        setActiveButton(btnNhanVien);
        loadView("quanly/NhanVienView.fxml", "Quản lý Nhân viên");
    }
    @FXML private void loadDoanhThu() {
        setActiveButton(btnDoanhThu);
        loadView("quanly/BaoCaoView.fxml", "Báo cáo Doanh thu");
    }
    @FXML private void loadLichSuHoaDon() {
        setActiveButton(btnLichSuHoaDon);
        loadView("quanly/LichSuHoaDonView.fxml", "Lịch sử Hóa đơn");
    }
    @FXML private void loadDashboardNV() {
        setActiveButton(btnDashboardNV);
        showDefaultContent();
        loadDashboardStatsNV();
    }
    @FXML private void loadBanHang() {
        setActiveButton(btnBanHang);
        loadView("nhanvien/BanHangView.fxml", "Bán hàng - POS");
    }
    @FXML private void loadLichSuBan() {
        setActiveButton(btnLichSuBan);
        loadView("nhanvien/LichSuBanView.fxml", "Lịch sử bán hàng");
    }
    // ==========================================================

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
                "1. Dashboard: Xem thống kê cá nhân\n" +
                "2. Bán hàng: Tạo đơn hàng mới\n" +
                "3. Lịch sử bán: Xem các đơn đã tạo\n\n" +
                "Liên hệ hỗ trợ: support@coffee.com";

        DialogUtils.showInfo("Hướng dẫn sử dụng", helpText);
    }

    @FXML
    private void handleLogout() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        SceneSwitcher.logout();
    }

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
        placeholder.getStyleClass().add("placeholder-container");
        Label icon = new Label("🚧");
        icon.getStyleClass().add("placeholder-icon");
        Label title = new Label(viewName);
        title.getStyleClass().add("placeholder-title");
        Label message = new Label("Màn hình này đang được phát triển...");
        message.getStyleClass().add("placeholder-message");
        Button btnBack = new Button("🏠 Về Dashboard");
        btnBack.getStyleClass().addAll("action-btn", "action-btn-blue");

        if (currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy) {
            btnBack.setOnAction(e -> loadDashboardQL());
        } else {
            btnBack.setOnAction(e -> loadDashboardNV());
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
            btn.getStyleClass().remove("menu-button-active");
        }
        if (button != null) {
            if (!button.getStyleClass().contains("menu-button-active")) {
                button.getStyleClass().add("menu-button-active");
            }
            currentActiveButton = button;
        }
    }
}