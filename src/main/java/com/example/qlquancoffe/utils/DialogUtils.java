package com.example.qlquancoffe.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utility class for showing dialogs
 */
public class DialogUtils {

    private static final String ICON_PATH = "/com/example/qlquancoffe/images/icon.png";

    /**
     * Hiển thị thông báo thông tin
     */
    public static void showInfo(String message) {
        showInfo("Thông báo", message);
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo lỗi
     */
    public static void showError(String message) {
        showError("Lỗi", message);
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo cảnh báo
     */
    public static void showWarning(String message) {
        showWarning("Cảnh báo", message);
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Hiển thị hộp thoại xác nhận
     */
    public static Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        return alert.showAndWait();
    }

    /**
     * Hiển thị hộp thoại xác nhận với Yes/No
     */
    public static boolean showYesNoConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType btnYes = new ButtonType("Có");
        ButtonType btnNo = new ButtonType("Không");
        alert.getButtonTypes().setAll(btnYes, btnNo);

        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnYes;
    }

    /**
     * Hiển thị hộp thoại nhập text
     */
    public static Optional<String> showInputDialog(String title, String headerText, String contentText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        styleAlert(dialog);
        return dialog.showAndWait();
    }

    /**
     * Hiển thị hộp thoại nhập text với giá trị mặc định
     */
    public static Optional<String> showInputDialog(String title, String headerText, String contentText, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        styleAlert(dialog);
        return dialog.showAndWait();
    }

    /**
     * Hiển thị thông báo thành công (với icon SUCCESS)
     */
    public static void showSuccess(String message) {
        showSuccess("Thành công", message);
    }

    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✓ " + title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo với custom buttons
     */
    public static Optional<ButtonType> showCustomAlert(String title, String message, Alert.AlertType type, ButtonType... buttons) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(buttons);
        styleAlert(alert);
        return alert.showAndWait();
    }

    /**
     * Style cho alert
     */
    private static void styleAlert(Alert alert) {
        // Set icon nếu có
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            // Nếu có icon thì uncomment dòng dưới
            // stage.getIcons().add(new Image(DialogUtils.class.getResourceAsStream(ICON_PATH)));
        } catch (Exception e) {
            // Ignore if icon not found
        }

        // Apply CSS nếu cần
        alert.getDialogPane().getStylesheets().add(
                DialogUtils.class.getResource("/com/example/qlquancoffe/css/DialogStyles.css").toExternalForm()
        );
    }

    /**
     * Style cho TextInputDialog
     */
    private static void styleAlert(TextInputDialog dialog) {
        try {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            // Nếu có icon thì uncomment dòng dưới
            // stage.getIcons().add(new Image(DialogUtils.class.getResourceAsStream(ICON_PATH)));
        } catch (Exception e) {
            // Ignore if icon not found
        }

        // Apply CSS nếu cần
        dialog.getDialogPane().getStylesheets().add(
                DialogUtils.class.getResource("/com/example/qlquancoffe/css/DialogStyles.css").toExternalForm()
        );
    }

    /**
     * Hiển thị loading dialog (không block)
     */
    public static Alert showLoading(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đang xử lý");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().clear();
        styleAlert(alert);
        alert.show();
        return alert;
    }

    /**
     * Đóng loading dialog
     */
    public static void closeLoading(Alert loadingAlert) {
        if (loadingAlert != null) {
            loadingAlert.close();
        }
    }
}