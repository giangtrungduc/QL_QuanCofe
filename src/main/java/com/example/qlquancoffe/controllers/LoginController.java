package com.example.qlquancoffe.controllers;

import com.example.qlquancoffe.dao.TaiKhoanDAO;
import com.example.qlquancoffe.models.TaiKhoan;
import com.example.qlquancoffe.utils.SceneSwitcher;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

/**
 * Controller cho màn hình đăng nhập
 */
public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private CheckBox chkShowPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    private TaiKhoanDAO taiKhoanDAO;

    /**
     * Khởi tạo controller (tự động được gọi sau khi load FXML)
     */
    @FXML
    public void initialize() {
        System.out.println("🔐 Khởi tạo LoginController");

        // Khởi tạo DAO
        taiKhoanDAO = new TaiKhoanDAO();

        // Ẩn thông báo lỗi ban đầu
        lblError.setVisible(false);

        // Thiết lập sự kiện nhấn Enter
        setupEnterKeyEvent();

        // Thiết lập chức năng hiển thị mật khẩu
        setupShowPasswordToggle();

        // Focus vào username khi mở màn hình
        Platform.runLater(() -> txtUsername.requestFocus());

        // Thêm animation cho button
        setupButtonAnimation();
    }

    /**
     * Xử lý sự kiện nhấn nút Đăng nhập
     */
    @FXML
    private void handleLogin() {
        // Ẩn thông báo lỗi cũ
        hideError();

        // Lấy dữ liệu từ form
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Validate input
        if (username.isEmpty()) {
            showError("❌ Vui lòng nhập tên đăng nhập!");
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("❌ Vui lòng nhập mật khẩu!");
            txtPassword.requestFocus();
            return;
        }

        // Disable button để tránh click nhiều lần
        btnLogin.setDisable(true);
        btnLogin.setText("Đang đăng nhập...");

        // Thực hiện đăng nhập trong thread riêng (tránh block UI)
        new Thread(() -> {
            try {
                // Gọi DAO để kiểm tra đăng nhập
                TaiKhoan user = taiKhoanDAO.checkLogin(username, password);

                // Cập nhật UI trong JavaFX Application Thread
                Platform.runLater(() -> {
                    if (user != null) {
                        // Đăng nhập thành công
                        System.out.println("✅ Đăng nhập thành công: " + user.getHoTen());

                        // Lưu thông tin user
                        SceneSwitcher.setCurrentUser(user);

                        // Hiển thị thông báo
                        showSuccess("Đăng nhập thành công!");

                        // Chuyển sang màn hình chính sau 0.5s
                        new Thread(() -> {
                            try {
                                Thread.sleep(500);
                                Platform.runLater(() -> {
                                    SceneSwitcher.switchToMain();
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                    } else {
                        // Đăng nhập thất bại
                        System.out.println("❌ Đăng nhập thất bại");
                        showError("❌ Sai tên đăng nhập hoặc mật khẩu!");

                        // Clear password
                        txtPassword.clear();
                        txtPassword.requestFocus();

                        // Enable lại button
                        btnLogin.setDisable(false);
                        btnLogin.setText("ĐĂNG NHẬP");
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ Lỗi khi đăng nhập: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    showError("❌ Lỗi hệ thống! Vui lòng thử lại.");
                    btnLogin.setDisable(false);
                    btnLogin.setText("ĐĂNG NHẬP");
                });
            }
        }).start();
    }

    /**
     * Thiết lập sự kiện nhấn Enter để đăng nhập
     */
    private void setupEnterKeyEvent() {
        txtUsername.setOnKeyPressed(this::handleEnterKey);
        txtPassword.setOnKeyPressed(this::handleEnterKey);
    }

    /**
     * Xử lý sự kiện nhấn phím
     */
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    /**
     * Thiết lập chức năng hiển thị/ẩn mật khẩu
     */
    private void setupShowPasswordToggle() {
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());

        chkShowPassword.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                txtPasswordVisible.setVisible(true);
                txtPasswordVisible.setManaged(true);
                txtPassword.setVisible(false);
                txtPassword.setManaged(false);
            } else {
                txtPasswordVisible.setVisible(false);
                txtPasswordVisible.setManaged(false);
                txtPassword.setVisible(true);
                txtPassword.setManaged(true);
            }
        });
    }

    /**
     * Thêm animation cho button
     */
    private void setupButtonAnimation() {
        btnLogin.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btnLogin);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        btnLogin.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btnLogin);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        lblError.setVisible(true);

        // Animation
        FadeTransition fade = new FadeTransition(Duration.millis(300), lblError);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Hiển thị thông báo thành công
     */
    private void showSuccess(String message) {
        lblError.setText("✅ " + message);
        lblError.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        lblError.setVisible(true);

        // Animation
        FadeTransition fade = new FadeTransition(Duration.millis(300), lblError);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Ẩn thông báo lỗi
     */
    private void hideError() {
        lblError.setVisible(false);
    }

    /**
     * Xử lý nút "Quên mật khẩu" (nếu cần)
     */
    @FXML
    private void handleForgotPassword() {
        SceneSwitcher.showInfoAlert(
                "Quên mật khẩu",
                "Vui lòng liên hệ quản trị viên để được hỗ trợ."
        );
    }
}