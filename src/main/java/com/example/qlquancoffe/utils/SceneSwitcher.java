package com.example.qlquancoffe.utils;

import com.example.qlquancoffe.models.TaiKhoan;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * Utility class để quản lý chuyển đổi giữa các màn hình (Scene)
 * và quản lý thông tin người dùng đang đăng nhập
 */
public class SceneSwitcher {

    // ==================== STATIC FIELDS ====================

    /** Stage chính của ứng dụng */
    private static Stage primaryStage = null;

    /** Tài khoản người dùng hiện tại */
    private static TaiKhoan currentUser = null;

    /** Base path cho FXML files */
    private static final String FXML_BASE_PATH = "/com/example/qlquancoffe/views/";


    // ==================== INITIALIZATION ====================

    /**
     * Thiết lập Stage chính của ứng dụng (gọi 1 lần trong MainApplication)
     * @param stage Stage chính
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;

        // Cấu hình mặc định
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();

        // Xử lý sự kiện đóng cửa sổ
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Ngăn đóng tự động
            confirmExit();
        });
    }

    /**
     * Lấy Stage chính
     * @return Stage chính
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Thiết lập người dùng hiện tại
     * @param user Tài khoản người dùng
     */
    public static void setCurrentUser(TaiKhoan user) {
        currentUser = user;
        if (user != null) {
            System.out.println("✅ Đã đăng nhập: " + user.getHoTen() + " (" + user.getVaiTro() + ")");
        }
    }

    /**
     * Lấy thông tin người dùng hiện tại
     * @return Tài khoản đang đăng nhập hoặc null
     */
    public static TaiKhoan getCurrentUser() {
        return currentUser;
    }

    /**
     * Kiểm tra đã đăng nhập chưa
     * @return true nếu đã đăng nhập
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Kiểm tra user hiện tại có phải Quản lý không
     * @return true nếu là Quản lý
     */
    public static boolean isQuanLy() {
        return currentUser != null &&
                currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy;
    }

    /**
     * Kiểm tra user hiện tại có phải Nhân viên không
     * @return true nếu là Nhân viên
     */
    public static boolean isNhanVien() {
        return currentUser != null &&
                currentUser.getVaiTro() == TaiKhoan.VaiTro.NhanVien;
    }


    // ==================== SCENE SWITCHING ====================

    /**
     * Chuyển sang màn hình mới
     * @param fxmlFileName Tên file FXML (VD: "LoginView.fxml")
     * @param title Tiêu đề cửa sổ
     * @param width Chiều rộng (0 = giữ nguyên)
     * @param height Chiều cao (0 = giữ nguyên)
     */
    public static void switchScene(String fxmlFileName, String title, int width, int height) {
        try {
            // Load FXML
            String fxmlPath = FXML_BASE_PATH + fxmlFileName;
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Tạo Scene
            Scene scene = new Scene(root);

            // Cập nhật Stage
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

            // Đặt kích thước nếu được chỉ định
            if (width > 0) primaryStage.setWidth(width);
            if (height > 0) primaryStage.setHeight(height);

            // Căn giữa màn hình
            primaryStage.centerOnScreen();

            // Hiển thị
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }

            System.out.println("✅ Đã chuyển sang: " + fxmlFileName);

        } catch (IOException e) {
            System.err.println("❌ Lỗi tải giao diện: " + fxmlFileName);
            e.printStackTrace();
            showErrorAlert("Lỗi tải giao diện",
                    "Không thể tải file: " + fxmlFileName + "\n" +
                            "Chi tiết: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Lỗi không xác định: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    /**
     * Chuyển sang màn hình mới và trả về Controller
     * @param fxmlFileName Tên file FXML
     * @param title Tiêu đề cửa sổ
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Controller của màn hình (để truyền dữ liệu)
     */
    public static <T> T switchSceneWithController(String fxmlFileName, String title, int width, int height) {
        try {
            String fxmlPath = FXML_BASE_PATH + fxmlFileName;
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

            if (width > 0) primaryStage.setWidth(width);
            if (height > 0) primaryStage.setHeight(height);

            primaryStage.centerOnScreen();

            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }

            System.out.println("✅ Đã chuyển sang: " + fxmlFileName);

            return loader.getController();

        } catch (IOException e) {
            System.err.println("❌ Lỗi tải giao diện: " + fxmlFileName);
            e.printStackTrace();
            showErrorAlert("Lỗi tải giao diện",
                    "Không thể tải file: " + fxmlFileName);
        }

        return null;
    }


    // ==================== SPECIFIC SCENES ====================

    /**
     * Chuyển sang màn hình đăng nhập
     */
    public static void switchToLogin() {
        currentUser = null; // Xóa thông tin đăng nhập
        switchScene("LoginView.fxml", "Đăng nhập - Quản lý Quán Cà Phê", 450, 550);
    }

    /**
     * Chuyển sang màn hình chính (sau khi đăng nhập)
     */
    public static void switchToMain() {
        if (!isLoggedIn()) {
            showErrorAlert("Lỗi", "Chưa đăng nhập!");
            switchToLogin();
            return;
        }

        String role = isQuanLy() ? "Quản lý" : "Nhân viên";
        String title = "Quản lý Quán Cà Phê - " + role + ": " + currentUser.getHoTen();

        switchScene("MainView.fxml", title, 1200, 700);
    }

    /**
     * Chuyển sang màn hình bán hàng
     */
    public static void switchToBanHang() {
        if (!isLoggedIn()) {
            showErrorAlert("Lỗi", "Chưa đăng nhập!");
            switchToLogin();
            return;
        }

        switchScene("nhanvien/BanHangView.fxml", "Bán hàng - " + currentUser.getHoTen(), 1200, 700);
    }


    // ==================== POPUP DIALOG ====================

    /**
     * Mở popup dialog
     * @param fxmlFileName Tên file FXML
     * @param title Tiêu đề dialog
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Controller của dialog
     */
    public static <T> T openDialog(String fxmlFileName, String title, int width, int height) {
        try {
            String fxmlPath = FXML_BASE_PATH + fxmlFileName;
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            Stage dialogStage = new Stage();
            dialogStage.setScene(scene);
            dialogStage.setTitle(title);
            dialogStage.setWidth(width);
            dialogStage.setHeight(height);
            dialogStage.setResizable(false);

            // Modal: Phải đóng dialog trước khi làm việc với cửa sổ chính
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);

            dialogStage.centerOnScreen();
            dialogStage.showAndWait();

            return loader.getController();

        } catch (IOException e) {
            System.err.println("❌ Lỗi mở dialog: " + fxmlFileName);
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể mở dialog: " + fxmlFileName);
        }

        return null;
    }


    // ==================== ALERT UTILITIES ====================

    /**
     * Hiển thị thông báo lỗi
     * @param title Tiêu đề
     * @param content Nội dung
     */
    public static void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (primaryStage != null && primaryStage.getScene() != null) {
            alert.initOwner(primaryStage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo thông tin
     * @param title Tiêu đề
     * @param content Nội dung
     */
    public static void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (primaryStage != null && primaryStage.getScene() != null) {
            alert.initOwner(primaryStage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo cảnh báo
     * @param title Tiêu đề
     * @param content Nội dung
     */
    public static void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        if (primaryStage != null && primaryStage.getScene() != null) {
            alert.initOwner(primaryStage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo thành công (custom)
     * @param title Tiêu đề
     * @param content Nội dung
     */
    public static void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (primaryStage != null && primaryStage.getScene() != null) {
            alert.initOwner(primaryStage);
        }
        alert.setTitle(title);
        alert.setHeaderText("✅ " + title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Hiển thị hộp thoại xác nhận
     * @param title Tiêu đề
     * @param content Nội dung
     * @return true nếu người dùng chọn OK, false nếu chọn Cancel
     */
    public static boolean showConfirmAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (primaryStage != null && primaryStage.getScene() != null) {
            alert.initOwner(primaryStage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Hiển thị hộp thoại xác nhận với custom buttons
     * @param title Tiêu đề
     * @param content Nội dung
     * @param okText Text cho nút OK
     * @param cancelText Text cho nút Cancel
     * @return true nếu chọn OK
     */
    public static boolean showConfirmAlert(String title, String content, String okText, String cancelText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (primaryStage != null && primaryStage.getScene() != null) {
            alert.initOwner(primaryStage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        ButtonType btnOk = new ButtonType(okText);
        ButtonType btnCancel = new ButtonType(cancelText);

        alert.getButtonTypes().setAll(btnOk, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnOk;
    }


    // ==================== LOGOUT & EXIT ====================

    /**
     * Đăng xuất và quay về màn hình đăng nhập
     */
    public static void logout() {
        boolean confirm = showConfirmAlert(
                "Xác nhận đăng xuất",
                "Bạn có chắc chắn muốn đăng xuất?",
                "Đăng xuất",
                "Hủy"
        );

        if (confirm) {
            System.out.println("👋 Đăng xuất: " + (currentUser != null ? currentUser.getHoTen() : ""));
            currentUser = null;
            switchToLogin();
        }
    }

    /**
     * Xác nhận thoát ứng dụng
     */
    public static void confirmExit() {
        boolean confirm = showConfirmAlert(
                "Xác nhận thoát",
                "Bạn có chắc chắn muốn thoát ứng dụng?",
                "Thoát",
                "Hủy"
        );

        if (confirm) {
            exit();
        }
    }

    /**
     * Thoát ứng dụng
     */
    public static void exit() {
        System.out.println("👋 Đóng ứng dụng...");

        // Đóng kết nối database
        DatabaseConnection.closeDataSource();

        // Đóng stage
        if (primaryStage != null) {
            primaryStage.close();
        }

        // Thoát ứng dụng
        System.exit(0);
    }


    // ==================== VALIDATION ====================

    /**
     * Kiểm tra quyền truy cập (dùng cho các màn hình chỉ admin mới vào được)
     * @return true nếu có quyền
     */
    public static boolean checkQuanLyPermission() {
        if (!isQuanLy()) {
            showWarningAlert(
                    "Không có quyền truy cập",
                    "Chức năng này chỉ dành cho Quản lý!"
            );
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra đã đăng nhập chưa
     * @return true nếu đã đăng nhập
     */
    public static boolean checkLoginRequired() {
        if (!isLoggedIn()) {
            showWarningAlert(
                    "Yêu cầu đăng nhập",
                    "Bạn cần đăng nhập để sử dụng chức năng này!"
            );
            switchToLogin();
            return false;
        }
        return true;
    }
}