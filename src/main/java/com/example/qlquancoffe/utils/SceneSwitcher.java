package com.example.qlquancoffe.utils;

import com.example.qlquancoffe.models.TaiKhoan;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * Utility quản lý chuyển đổi màn hình và session
 */
public class SceneSwitcher {

    private static Stage primaryStage = null;
    private static TaiKhoan currentUser = null;
    private static final String FXML_BASE_PATH = "/com/example/qlquancoffe/views/";

    // ==================== INITIALIZATION ====================

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            confirmExit();
        });
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    // ==================== USER SESSION ====================

    public static void setCurrentUser(TaiKhoan user) {
        currentUser = user;
        if (user != null) {
            System.out.println("✅ Đăng nhập: " + user.getHoTen() + " (" + user.getVaiTro() + ")");
        }
    }

    public static TaiKhoan getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isQuanLy() {
        return currentUser != null && currentUser.getVaiTro() == TaiKhoan.VaiTro.QuanLy;
    }

    // ==================== SCENE SWITCHING ====================

    /**
     * Chuyển scene chính
     * Tự động lấy kích thước từ FXML
     */
    public static void switchScene(String fxmlFileName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(FXML_BASE_PATH + fxmlFileName));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

            primaryStage.sizeToScene();

            primaryStage.centerOnScreen();

            if (!primaryStage.isShowing()) primaryStage.show();

            System.out.println("✅ Chuyển sang: " + fxmlFileName);

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Lỗi tải Scene", "Không thể tải: " + fxmlFileName);
        }
    }

    /**
     * Chuyển scene và trả về controller
     */
    public static <T> T switchSceneWithController(String fxmlFileName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(FXML_BASE_PATH + fxmlFileName));
            Parent root = loader.load();

            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle(title);

            primaryStage.sizeToScene();

            primaryStage.centerOnScreen();

            if (!primaryStage.isShowing()) primaryStage.show();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Lỗi tải Scene", "Không thể tải: " + fxmlFileName);
        }
        return null;
    }

    /**
     * Mở popup dialog
     */
    public static <T> T openDialog(String fxmlFileName, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(FXML_BASE_PATH + fxmlFileName));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setScene(new Scene(root));
            dialogStage.setTitle(title);
            dialogStage.setWidth(width);
            dialogStage.setHeight(height);
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Lỗi mở Dialog", "Không thể mở dialog: " + fxmlFileName);
        }
        return null;
    }

    // ==================== SPECIFIC SCENES ====================

    public static void switchToLogin() {
        currentUser = null;
        switchScene("LoginView.fxml", "Đăng nhập - Quản lý Quán Cà Phê");
    }

    public static void switchToMain() {
        if (!isLoggedIn()) {
            DialogUtils.showError("Lỗi Phiên", "Chưa đăng nhập!");
            switchToLogin();
            return;
        }
        String title = "Quản lý Quán Cà Phê - " + currentUser.getVaiTro() + ": " + currentUser.getHoTen();

        switchScene("MainView.fxml", title);
    }

    // ==================== LOGOUT & EXIT ====================

    public static void logout() {
        Optional<ButtonType> result = DialogUtils.showConfirmation("Xác nhận đăng xuất", "Bạn có chắc muốn đăng xuất?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("👋 Đăng xuất: " + currentUser.getHoTen());
            currentUser = null;
            switchToLogin();
        }
    }

    public static void confirmExit() {
        Optional<ButtonType> result = DialogUtils.showConfirmation("Xác nhận thoát", "Bạn có chắc muốn thoát?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            exit();
        }
    }

    public static void exit() {
        System.out.println("👋 Đóng ứng dụng...");
        DatabaseConnection.closeDataSource();
        if (primaryStage != null) primaryStage.close();
        System.exit(0);
    }
}