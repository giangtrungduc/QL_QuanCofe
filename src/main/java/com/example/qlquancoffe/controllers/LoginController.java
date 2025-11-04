package com.example.qlquancoffe.controllers;

import com.example.qlquancoffe.dao.TaiKhoanDAO;
import com.example.qlquancoffe.models.TaiKhoan;
import com.example.qlquancoffe.utils.DialogUtils;      // ✅ SỬA: Thêm import
import com.example.qlquancoffe.utils.SceneSwitcher;
import com.example.qlquancoffe.utils.ValidationUtil;  // ✅ SỬA: Thêm import
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

    @FXML
    public void initialize() {
        System.out.println("🔐 Khởi tạo LoginController");
        taiKhoanDAO = new TaiKhoanDAO();
        lblError.setVisible(false);

        setupEnterKeyEvent();
        setupShowPasswordToggle();
        setupButtonAnimation();

        Platform.runLater(() -> txtUsername.requestFocus());
    }

    @FXML
    private void handleLogin() {
        hideError();

        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (ValidationUtil.isEmpty(username)) {
            showError("❌ Vui lòng nhập tên đăng nhập!");
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("❌ Vui lòng nhập mật khẩu!");
            txtPassword.requestFocus();
            return;
        }

        btnLogin.setDisable(true);
        btnLogin.setText("Đang đăng nhập...");

        new Thread(() -> {
            try {
                TaiKhoan user = taiKhoanDAO.checkLogin(username.trim(), password);

                Platform.runLater(() -> {
                    if (user != null) {
                        System.out.println("✅ Đăng nhập thành công: " + user.getHoTen());

                        SceneSwitcher.setCurrentUser(user);

                        showSuccess("Đăng nhập thành công!");

                        // Chuyển sang màn hình chính sau 0.5s
                        new Thread(() -> {
                            try {
                                Thread.sleep(500);
                                Platform.runLater(SceneSwitcher::switchToMain);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                    } else {
                        System.out.println("❌ Đăng nhập thất bại");
                        showError("❌ Sai tên đăng nhập hoặc mật khẩu!");
                        txtPassword.clear();
                        txtPassword.requestFocus();
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

    private void setupEnterKeyEvent() {
        txtUsername.setOnKeyPressed(this::handleEnterKey);
        txtPassword.setOnKeyPressed(this::handleEnterKey);
    }

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

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

    private void setupButtonAnimation() {
        btnLogin.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btnLogin);
            st.setToX(1.05); st.setToY(1.05);
            st.play();
        });
        btnLogin.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btnLogin);
            st.setToX(1.0); st.setToY(1.0);
            st.play();
        });
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.getStyleClass().setAll("label-error");
        lblError.setVisible(true);

        FadeTransition fade = new FadeTransition(Duration.millis(300), lblError);
        fade.setFromValue(0); fade.setToValue(1);
        fade.play();
    }

    private void showSuccess(String message) {
        lblError.setText("✅ " + message);
        lblError.getStyleClass().setAll("label-success");
        lblError.setVisible(true);

        FadeTransition fade = new FadeTransition(Duration.millis(300), lblError);
        fade.setFromValue(0); fade.setToValue(1);
        fade.play();
    }

    private void hideError() {
        lblError.setVisible(false);
    }

    @FXML
    private void handleForgotPassword(){
        DialogUtils.showInfo(
                "Quên mật khẩu",
                "Vui lòng liên hệ quản trị viên để được hỗ trợ."
        );
    }
}