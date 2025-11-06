package com.example.qlquancoffe.controllers.quanly;

import com.example.qlquancoffe.dao.TaiKhoanDAO;
import com.example.qlquancoffe.models.TaiKhoan;
import com.example.qlquancoffe.utils.DialogUtils;
import com.example.qlquancoffe.utils.PasswordUtil;
import com.example.qlquancoffe.utils.SceneSwitcher;
import com.example.qlquancoffe.utils.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class NhanVienController implements Initializable {

    // === FXML Fields ===
    @FXML private Label lblTongNhanVien;
    @FXML private TextField txtTimKiem;
    @FXML private ComboBox<String> cboLocVaiTro;
    @FXML private TableView<TaiKhoan> tableNhanVien;
    @FXML private TableColumn<TaiKhoan, Integer> colID;
    @FXML private TableColumn<TaiKhoan, String> colHoTen;
    @FXML private TableColumn<TaiKhoan, String> colTenDangNhap;
    @FXML private TableColumn<TaiKhoan, TaiKhoan.VaiTro> colVaiTro;

    // Form Fields
    @FXML private VBox formContainer;
    @FXML private Label lblFormTitle;
    @FXML private Label lblError;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtTenDangNhap;
    @FXML private ComboBox<TaiKhoan.VaiTro> cbVaiTroForm;
    @FXML private PasswordField txtMatKhau;

    @FXML private Button btnThem;
    @FXML private Button btnSua;
    @FXML private Button btnXoa;

    // === DAOs ===
    private TaiKhoanDAO taiKhoanDAO;

    // === Data Lists ===
    private ObservableList<TaiKhoan> masterList = FXCollections.observableArrayList();
    private FilteredList<TaiKhoan> filteredList;
    private TaiKhoan currentSelectedNhanVien;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taiKhoanDAO = new TaiKhoanDAO();

        setupTableColumns();
        setupFilters();
        setupSelectionListener();

        loadData();
        resetForm(); // Đặt form về trạng thái "Thêm mới"
    }

    /**
     * Tải dữ liệu ban đầu
     */
    private void loadData() {
        new Thread(() -> {
            List<TaiKhoan> data = taiKhoanDAO.getAll();
            Platform.runLater(() -> {
                masterList.setAll(data);
                lblTongNhanVien.setText("Tổng: " + masterList.size() + " NV");
                // Mặc định, bảng hiển thị tất cả
                filteredList = new FilteredList<>(masterList, p -> true);
                tableNhanVien.setItems(filteredList);
            });
        }).start();
    }

    /**
     * Cài đặt các cột TableView
     */
    private void setupTableColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("idNhanVien"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colTenDangNhap.setCellValueFactory(new PropertyValueFactory<>("tenDangNhap"));
        colVaiTro.setCellValueFactory(new PropertyValueFactory<>("vaiTro"));

        // Custom Cell Factory cho Vai Trò để hiển thị màu
        colVaiTro.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(TaiKhoan.VaiTro item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("cell-role-manager", "cell-role-staff");
                } else {
                    setText(item.getDisplayName());
                    if (item == TaiKhoan.VaiTro.QuanLy) {
                        getStyleClass().add("cell-role-manager");
                        getStyleClass().remove("cell-role-staff");
                    } else {
                        getStyleClass().add("cell-role-staff");
                        getStyleClass().remove("cell-role-manager");
                    }
                }
            }
        });
    }

    /**
     * Cài đặt bộ lọc (Tìm kiếm và ComboBox)
     */
    private void setupFilters() {
        // ComboBox Lọc Vai Trò
        cboLocVaiTro.getItems().addAll("Tất cả vai trò", "Quản lý", "Nhân viên");
        cboLocVaiTro.getSelectionModel().selectFirst();

        // ComboBox Form
        cbVaiTroForm.getItems().setAll(TaiKhoan.VaiTro.values());

        // Thêm listener cho 2 bộ lọc
        txtTimKiem.textProperty().addListener((obs, o, n) -> applyFilters());
        cboLocVaiTro.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    /**
     * Áp dụng bộ lọc cho FilteredList
     */
    private void applyFilters() {
        String keyword = txtTimKiem.getText().toLowerCase().trim();
        String vaiTroFilter = cboLocVaiTro.getValue();

        Predicate<TaiKhoan> keywordFilter = tk ->
                keyword.isEmpty() ||
                        tk.getHoTen().toLowerCase().contains(keyword) ||
                        tk.getTenDangNhap().toLowerCase().contains(keyword);

        Predicate<TaiKhoan> vaiTroPredicate = tk -> {
            if (vaiTroFilter == null || vaiTroFilter.equals("Tất cả vai trò")) {
                return true;
            }
            return tk.getVaiTro().getDisplayName().equals(vaiTroFilter);
        };

        filteredList.setPredicate(keywordFilter.and(vaiTroPredicate));
    }

    /**
     * Listener khi chọn một hàng trong bảng
     */
    private void setupSelectionListener() {
        tableNhanVien.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayTaiKhoanDetails(newSelection);
            } else {
                resetForm();
            }
        });
    }

    /**
     * Hiển thị chi tiết NV lên Form
     */
    private void displayTaiKhoanDetails(TaiKhoan tk) {
        currentSelectedNhanVien = tk;

        lblFormTitle.setText("Sửa Thông Tin (ID: " + tk.getIdNhanVien() + ")");
        txtHoTen.setText(tk.getHoTen());
        txtTenDangNhap.setText(tk.getTenDangNhap());
        cbVaiTroForm.setValue(tk.getVaiTro());

        txtMatKhau.clear();
        txtMatKhau.setPromptText("Để trống nếu không đổi");

        txtTenDangNhap.setDisable(true); // Không cho sửa tên đăng nhập

        btnThem.setDisable(true);
        btnSua.setDisable(false);
        btnXoa.setDisable(false);
        lblError.setVisible(false);
    }

    // ==================== HANDLERS ====================

    @FXML
    void handleThem(ActionEvent event) {
        if (!validateForm(true)) return; // Yêu cầu mật khẩu khi thêm mới

        // Hash mật khẩu
        String newPasswordHash = PasswordUtil.hashPassword(txtMatKhau.getText());

        TaiKhoan newTK = new TaiKhoan(
                txtHoTen.getText().trim(),
                txtTenDangNhap.getText().trim(),
                newPasswordHash,
                cbVaiTroForm.getValue()
        );

        new Thread(() -> {
            int newId = taiKhoanDAO.insert(newTK);
            Platform.runLater(() -> {
                if (newId > 0) {
                    newTK.setIdNhanVien(newId);
                    masterList.add(newTK);
                    DialogUtils.showSuccess("Thêm thành công", "Đã thêm tài khoản " + newTK.getHoTen());
                    resetForm();
                } else {
                    DialogUtils.showError("Lỗi", "Tên đăng nhập đã tồn tại.");
                }
            });
        }).start();
    }

    @FXML
    void handleSua(ActionEvent event) {
        if (currentSelectedNhanVien == null) return;
        if (!validateForm(false)) return; // Không yêu cầu mật khẩu

        // Cập nhật thông tin cơ bản
        currentSelectedNhanVien.setHoTen(txtHoTen.getText().trim());
        currentSelectedNhanVien.setVaiTro(cbVaiTroForm.getValue());

        String newPassword = txtMatKhau.getText();

        new Thread(() -> {
            boolean updateInfoSuccess = taiKhoanDAO.update(currentSelectedNhanVien);
            boolean updatePassSuccess = true;

            // Nếu người dùng nhập mật khẩu mới
            if (!ValidationUtil.isEmpty(newPassword)) {
                String newHash = PasswordUtil.hashPassword(newPassword);
                updatePassSuccess = taiKhoanDAO.resetPassword(currentSelectedNhanVien.getIdNhanVien(), newHash);
            }
            boolean a = updatePassSuccess;
            Platform.runLater(() -> {
                if (updateInfoSuccess && a) {
                    tableNhanVien.refresh(); // Cập nhật bảng
                    DialogUtils.showSuccess("Cập nhật thành công!");
                    resetForm();
                } else {
                    DialogUtils.showError("Lỗi", "Không thể cập nhật tài khoản.");
                }
            });
        }).start();
    }

    @FXML
    void handleXoa(ActionEvent event) {
        if (currentSelectedNhanVien == null) return;

        // Không cho xóa tài khoản admin (nếu đang dùng)
        if (currentSelectedNhanVien.getIdNhanVien() == SceneSwitcher.getCurrentUser().getIdNhanVien()) {
            DialogUtils.showWarning("Không thể xóa", "Bạn không thể xóa tài khoản của chính mình.");
            return;
        }

        boolean confirm = DialogUtils.showYesNoConfirmation(
                "Xác nhận xóa",
                "Bạn có chắc muốn XÓA VĨNH VIỄN tài khoản: " + currentSelectedNhanVien.getHoTen() + "?\n" +
                        "LƯU Ý: Nếu nhân viên này đã tạo hóa đơn, việc xóa có thể thất bại."
        );

        if (confirm) {
            new Thread(() -> {
                boolean success = taiKhoanDAO.delete(currentSelectedNhanVien.getIdNhanVien());
                Platform.runLater(() -> {
                    if (success) {
                        masterList.remove(currentSelectedNhanVien);
                        DialogUtils.showSuccess("Xóa thành công!");
                        resetForm();
                    } else {
                        DialogUtils.showError("Lỗi", "Không thể xóa tài khoản này.\nCó thể do nhân viên đã tạo hóa đơn.");
                    }
                });
            }).start();
        }
    }

    @FXML
    void handleLamMoi(ActionEvent event) {
        tableNhanVien.getSelectionModel().clearSelection();
        resetForm();
        loadData(); // Tải lại dữ liệu
    }

    /**
     * Reset Form về trạng thái "Thêm mới"
     */
    private void resetForm() {
        currentSelectedNhanVien = null;
        lblFormTitle.setText("Thêm Nhân Viên Mới");

        txtHoTen.clear();
        txtTenDangNhap.clear();
        txtMatKhau.clear();

        txtMatKhau.setPromptText("Nhập mật khẩu (bắt buộc)");
        cbVaiTroForm.setValue(TaiKhoan.VaiTro.NhanVien); // Mặc định

        txtTenDangNhap.setDisable(false);

        btnThem.setDisable(false);
        btnSua.setDisable(true);
        btnXoa.setDisable(true);
        lblError.setVisible(false);

        tableNhanVien.getSelectionModel().clearSelection();
    }

    /**
     * Kiểm tra nhập liệu của Form
     */
    private boolean validateForm(boolean isAdding) {
        lblError.setVisible(false);

        String hoTen = txtHoTen.getText();
        String tenDangNhap = txtTenDangNhap.getText();
        String matKhau = txtMatKhau.getText();

        if (ValidationUtil.isEmpty(hoTen)) {
            showError("Họ tên không được để trống.");
            return false;
        }
        if (ValidationUtil.isEmpty(tenDangNhap)) {
            showError("Tên đăng nhập không được để trống.");
            return false;
        }
        if (cbVaiTroForm.getValue() == null) {
            showError("Vui lòng chọn vai trò.");
            return false;
        }

        // Nếu là THÊM MỚI, mật khẩu là bắt buộc
        if (isAdding && ValidationUtil.isEmpty(matKhau)) {
            showError("Mật khẩu là bắt buộc khi thêm mới.");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}