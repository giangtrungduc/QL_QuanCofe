package com.example.qlquancoffe.controllers.quanly;

import com.example.qlquancoffe.models.DanhMuc;
import com.example.qlquancoffe.models.SanPham;
import com.example.qlquancoffe.utils.DialogUtils;
import com.example.qlquancoffe.utils.ValidationUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SanPhamDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtTenSP;
    @FXML private ComboBox<DanhMuc> cboDanhMuc;
    @FXML private TextField txtGiaBan;
    @FXML private TextField txtSoLuong;
    @FXML private ComboBox<SanPham.TrangThai> cboTrangThai;
    @FXML private TextField txtAnhSanPham;
    @FXML private Label lblError;
    // (Không cần @FXML cho btnChonAnh, btnHuy, btnLuu vì FXML đã gán onAction)

    private Stage dialogStage;
    private SanPham sanPham;
    private boolean isSaved = false;

    @FXML
    public void initialize() {
        // Tải các trạng thái vào ComboBox
        cboTrangThai.getItems().setAll(SanPham.TrangThai.values());
    }

    /**
     * Tải dữ liệu của SanPham vào Form
     */
    public void loadData(SanPham sanPham, ObservableList<DanhMuc> danhMucList) {
        this.sanPham = sanPham;

        // Tải danh sách danh mục
        cboDanhMuc.setItems(danhMucList);

        if (sanPham.getIdSanPham() > 0) {
            // Chế độ "Sửa"
            lblTitle.setText("Sửa Sản Phẩm");
            txtTenSP.setText(sanPham.getTenSanPham());
            txtGiaBan.setText(sanPham.getGiaBan().toString());
            txtSoLuong.setText(String.valueOf(sanPham.getSoLuongTonKho()));
            txtAnhSanPham.setText(sanPham.getAnhSanPham());

            cboDanhMuc.getSelectionModel().select(
                    danhMucList.stream()
                            .filter(dm -> dm.getIdDanhMuc() == sanPham.getIdDanhMuc())
                            .findFirst()
                            .orElse(null)
            );
            cboTrangThai.getSelectionModel().select(sanPham.getTrangThai());

        } else {
            // Chế độ "Thêm mới"
            lblTitle.setText("Thêm Sản Phẩm Mới");
            cboTrangThai.getSelectionModel().select(SanPham.TrangThai.ConHang);
        }
    }

    /**
     * Xử lý lưu
     */
    @FXML
    private void handleSave() {
        if (isValidInput()) {
            // Cập nhật dữ liệu từ form vào đối tượng sanPham
            sanPham.setTenSanPham(txtTenSP.getText().trim());
            sanPham.setGiaBan(new BigDecimal(txtGiaBan.getText()));
            sanPham.setSoLuongTonKho(Integer.parseInt(txtSoLuong.getText()));
            sanPham.setIdDanhMuc(cboDanhMuc.getValue().getIdDanhMuc());
            sanPham.setTrangThai(cboTrangThai.getValue());

            // ✅ SỬA LỖI NullPointerException
            String anhSanPhamPath = txtAnhSanPham.getText();
            if (ValidationUtil.isEmpty(anhSanPhamPath)) {
                sanPham.setAnhSanPham(null);
            } else {
                sanPham.setAnhSanPham(anhSanPhamPath.trim());
            }

            isSaved = true;
            dialogStage.close();
        }
    }

    /**
     * ✅ TÍNH NĂNG MỚI: Mở FileChooser
     */
    @FXML
    private void handleChonAnh() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn Ảnh Sản Phẩm");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Bắt đầu từ thư mục "src/main/resources/com/example/qlquancoffe"
        // (Điều này chỉ hoạt động tốt trong môi trường Development)
        try {
            Path resourcePath = Paths.get("src", "main", "resources", "com", "example", "qlquancoffe", "images");
            File initialDir = resourcePath.toFile();
            if (initialDir.exists()) {
                fileChooser.setInitialDirectory(initialDir);
            }
        } catch (Exception e) {
            // Bỏ qua nếu không tìm thấy thư mục
        }

        File selectedFile = fileChooser.showOpenDialog(dialogStage);

        if (selectedFile != null) {
            // Cố gắng chuyển đổi đường dẫn tuyệt đối thành đường dẫn resource
            try {
                Path resourcesRoot = Paths.get("src/main/resources").toAbsolutePath();
                Path selectedPath = selectedFile.toPath();

                if (selectedPath.startsWith(resourcesRoot)) {
                    Path relativePath = resourcesRoot.relativize(selectedPath);
                    // Chuyển đổi sang định dạng resource (dùng / )
                    String resourcePath = "/" + relativePath.toString().replace("\\", "/");
                    txtAnhSanPham.setText(resourcePath);
                } else {
                    DialogUtils.showWarning("Lỗi đường dẫn", "Vui lòng chọn ảnh nằm trong thư mục 'src/main/resources' của project.");
                }
            } catch (Exception e) {
                DialogUtils.showError("Lỗi", "Không thể lấy đường dẫn tệp: " + e.getMessage());
            }
        }
    }


    /**
     * Kiểm tra dữ liệu nhập
     */
    private boolean isValidInput() {
        String tenSP = txtTenSP.getText();
        String giaBan = txtGiaBan.getText();
        String soLuong = txtSoLuong.getText();

        if (ValidationUtil.isEmpty(tenSP)) {
            showError("Tên sản phẩm không được để trống.");
            return false;
        }
        if (cboDanhMuc.getValue() == null || cboDanhMuc.getValue().getIdDanhMuc() == 0) {
            showError("Vui lòng chọn một danh mục.");
            return false;
        }

        // Sửa: Số lượng có thể là 0 (nếu Hết hàng)
        try {
            int sl = Integer.parseInt(soLuong);
            if (sl < 0) {
                showError("Số lượng không được âm.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Số lượng phải là một số nguyên.");
            return false;
        }

        if (!ValidationUtil.isValidMoney(giaBan)) { // isValidMoney đã kiểm tra > 0
            showError("Giá bán phải là một số dương.");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    // --- Getters and Setters ---
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public SanPham getSanPhamResult() {
        return isSaved ? sanPham : null;
    }
}