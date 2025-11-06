package com.example.qlquancoffe.controllers.quanly;

import com.example.qlquancoffe.dao.DanhMucDAO;
import com.example.qlquancoffe.dao.SanPhamDAO;
import com.example.qlquancoffe.models.DanhMuc;
import com.example.qlquancoffe.models.SanPham;
import com.example.qlquancoffe.utils.CurrencyUtil;
import com.example.qlquancoffe.utils.DialogUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane; // ✅ SỬA
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane; // ✅ THÊM
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SanPhamController implements Initializable {

    // === FXML Fields ===
    @FXML private Label lblTongSanPham;
    @FXML private Button btnThem;
    @FXML private Button btnSua;
    @FXML private Button btnXoa;
    @FXML private ComboBox<DanhMuc> cboDanhMuc;
    @FXML private ComboBox<SanPham.TrangThai> cboTrangThai;
    @FXML private TextField txtTimKiem;
    @FXML private FlowPane productFlowPane; // ✅ SỬA: Đổi từ GridPane
    @FXML private VBox detailContainer;
    @FXML private VBox detailPlaceholder;
    @FXML private VBox detailForm;
    @FXML private ImageView imgDetail;
    @FXML private Label lblDetailTen;
    @FXML private Label lblDetailGia;
    @FXML private Label lblDetailKho;
    @FXML private Label lblDetailDanhMuc;
    @FXML private Label lblDetailTrangThai;

    // === DAOs ===
    private SanPhamDAO sanPhamDAO;
    private DanhMucDAO danhMucDAO;

    // === Data Lists ===
    private ObservableList<SanPham> masterList = FXCollections.observableArrayList();
    private ObservableList<DanhMuc> danhMucList = FXCollections.observableArrayList();
    private SanPham currentSelectedSanPham;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sanPhamDAO = new SanPhamDAO();
        danhMucDAO = new DanhMucDAO();

        setupFilters();
        showDetailPlaceholder(true); // Ẩn form chi tiết ban đầu
        loadData();
    }

    /**
     * Tải dữ liệu chính (Sản phẩm, Danh mục) từ CSDL
     */
    private void loadData() {
        new Thread(() -> {
            masterList.setAll(sanPhamDAO.getAll());
            danhMucList.setAll(danhMucDAO.getAllDanhMuc());

            Platform.runLater(() -> {
                // Cập nhật ComboBox Danh mục
                cboDanhMuc.getItems().clear();
                cboDanhMuc.getItems().add(0, new DanhMuc(0, "Tất cả danh mục"));
                cboDanhMuc.getItems().addAll(danhMucList);
                cboDanhMuc.getSelectionModel().selectFirst();

                // Cập nhật ComboBox Trạng thái
                cboTrangThai.getItems().clear();
                cboTrangThai.getItems().add(null); // "Tất cả trạng thái"
                cboTrangThai.getItems().addAll(SanPham.TrangThai.values());
                cboTrangThai.getSelectionModel().selectFirst();

                // Hiển thị sản phẩm
                filterAndDisplayProducts();
                lblTongSanPham.setText("Tổng: " + masterList.size() + " SP");
            });
        }).start();
    }

    /**
     * Cài đặt bộ lọc
     */
    private void setupFilters() {
        // Listener cho 3 bộ lọc
        txtTimKiem.textProperty().addListener((obs, old, val) -> filterAndDisplayProducts());
        cboDanhMuc.valueProperty().addListener((obs, old, val) -> filterAndDisplayProducts());
        cboTrangThai.valueProperty().addListener((obs, old, val) -> filterAndDisplayProducts());
    }

    /**
     * Lọc và hiển thị sản phẩm lên lưới
     */
    private void filterAndDisplayProducts() {
        String keyword = txtTimKiem.getText().toLowerCase().trim();
        DanhMuc category = cboDanhMuc.getValue();
        SanPham.TrangThai status = cboTrangThai.getValue();

        // Lọc
        Predicate<SanPham> keywordFilter = sp ->
                keyword.isEmpty() || sp.getTenSanPham().toLowerCase().contains(keyword);
        Predicate<SanPham> categoryFilter = sp ->
                category == null || category.getIdDanhMuc() == 0 || sp.getIdDanhMuc() == category.getIdDanhMuc();
        Predicate<SanPham> statusFilter = sp ->
                status == null || sp.getTrangThai() == status;

        List<SanPham> filteredList = masterList.stream()
                .filter(keywordFilter.and(categoryFilter).and(statusFilter))
                .collect(Collectors.toList());

        // Sắp xếp (có thể thêm sau)

        // Hiển thị
        displayProducts(filteredList);
    }

    /**
     * ✅ SỬA: Hiển thị danh sách sản phẩm lên FlowPane
     */
    private void displayProducts(List<SanPham> products) {
        productFlowPane.getChildren().clear();

        for (SanPham sp : products) {
            Node card = createProductCard(sp);
            productFlowPane.getChildren().add(card);
        }
    }

    /**
     * Tạo một thẻ VBox đại diện cho sản phẩm
     */
    private Node createProductCard(SanPham sp) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(180); // Kích thước thẻ

        // === IMAGE ===
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-card-image");
        imageContainer.setPrefHeight(120.0);

        String imagePath = sp.getAnhSanPham();
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                Image img = new Image(getClass().getResourceAsStream(imagePath));
                ImageView imageView = new ImageView(img);
                imageView.setFitHeight(120.0);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("image-view");
                imageContainer.getChildren().add(imageView);
            } else { throw new Exception("No path"); }
        } catch (Exception e) {
            Label icon = new Label("📦");
            icon.getStyleClass().add("label-icon");
            imageContainer.getChildren().add(icon);
        }

        // === INFO ===
        VBox infoBox = new VBox(5);
        infoBox.getStyleClass().add("product-card-info");

        Label name = new Label(sp.getTenSanPham());
        name.getStyleClass().add("product-card-name");

        Label price = new Label(CurrencyUtil.formatVND(sp.getGiaBan()));
        price.getStyleClass().add("product-card-price");

        HBox stockBox = new HBox(5);
        stockBox.setAlignment(Pos.CENTER_LEFT);
        Label stockLabel = new Label("Kho: " + sp.getSoLuongTonKho());
        stockLabel.getStyleClass().add("product-card-stock");

        boolean isAvailable = sp.getTrangThai() == SanPham.TrangThai.ConHang && sp.getSoLuongTonKho() > 0;

        if (isAvailable) {
            // HÀNG CÒN
            stockLabel.setText("Kho: " + sp.getSoLuongTonKho());
            stockLabel.getStyleClass().add("product-card-stock"); // Style xanh
        } else {
            // HÀNG HẾT HOẶC NGỪNG
            card.getStyleClass().add("product-card-out-of-stock"); // Thêm class mờ + đỏ
            stockLabel.getStyleClass().add("product-card-stock-out"); // Chữ đỏ

            if (sp.getTrangThai() == SanPham.TrangThai.NgungKinhDoanh) {
                stockLabel.setText("Ngừng KD");
            } else {
                stockLabel.setText("Hết hàng (Kho: " + sp.getSoLuongTonKho() + ")");
            }
        }

        stockBox.getChildren().add(stockLabel);
        infoBox.getChildren().addAll(name, price, stockBox);
        card.getChildren().addAll(imageContainer, infoBox);

        // === INTERACTION ===
        card.setOnMouseClicked(e -> showProductDetails(sp));

        return card;
    }

    /**
     * Hiển thị chi tiết sản phẩm đã chọn
     */
    private void showProductDetails(SanPham sp) {
        if (sp == null) {
            showDetailPlaceholder(true);
            return;
        }

        currentSelectedSanPham = sp;
        showDetailPlaceholder(false); // Ẩn placeholder, hiện form

        // Load ảnh
        try {
            String imagePath = sp.getAnhSanPham();
            if (imagePath != null && !imagePath.isEmpty()) {
                imgDetail.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            } else {
                imgDetail.setImage(null); // Xóa ảnh cũ
            }
        } catch (Exception e) {
            imgDetail.setImage(null); // Lỗi cũng xóa ảnh
        }

        // Load thông tin
        lblDetailTen.setText(sp.getTenSanPham());
        lblDetailGia.setText(CurrencyUtil.formatVND(sp.getGiaBan()));
        lblDetailKho.setText(String.valueOf(sp.getSoLuongTonKho()));

        // Lấy tên danh mục
        String tenDM = danhMucList.stream()
                .filter(dm -> dm.getIdDanhMuc() == sp.getIdDanhMuc())
                .map(DanhMuc::getTenDanhMuc)
                .findFirst()
                .orElse("N/A");
        lblDetailDanhMuc.setText(tenDM);

        // Trạng thái
        lblDetailTrangThai.setText(sp.getTrangThai().getDisplayName());
        String statusColor = switch (sp.getTrangThai()) {
            case ConHang -> "#27ae60";
            case HetHang -> "#e74c3c";
            case NgungKinhDoanh -> "#95a5a6";
        };
        lblDetailTrangThai.setStyle("-fx-text-fill: " + statusColor);

        // Bật nút
        btnSua.setDisable(false);
        btnXoa.setDisable(false);
    }

    /**
     * Ẩn/hiện placeholder
     */
    private void showDetailPlaceholder(boolean show) {
        detailPlaceholder.setVisible(show);
        detailPlaceholder.setManaged(show);
        detailForm.setVisible(!show);
        detailForm.setManaged(!show);

        // Luôn tắt nút khi không chọn gì
        if (show) {
            currentSelectedSanPham = null;
            btnSua.setDisable(true);
            btnXoa.setDisable(true);
        }
    }

    // ==================== HANDLERS ====================

    @FXML
    private void handleThem(ActionEvent event) {
        // Mở Dialog
        SanPham result = showSanPhamDialog(new SanPham(), "Thêm Sản Phẩm");
        if(sanPhamDAO.isTenSanPhamTonTai(result.getTenSanPham())){
            DialogUtils.showError("Sản phẩm đã tồn tại", "Không thể thêm sản phẩm.");
            return;
        }
        if (result != null) {
            new Thread(() -> {
                int id = sanPhamDAO.insert(result);
                if (id > 0) {
                    result.setIdSanPham(id);
                    Platform.runLater(() -> {
                        masterList.add(result);
                        filterAndDisplayProducts();
                        showProductDetails(result); // Hiển thị chi tiết SP vừa thêm
                        DialogUtils.showSuccess("Đã thêm sản phẩm " + result.getTenSanPham());
                    });
                } else {
                    Platform.runLater(() -> DialogUtils.showError("Lỗi", "Không thể thêm sản phẩm."));
                }
            }).start();
        }
    }

    @FXML
    private void handleSua(ActionEvent event) {
        if (currentSelectedSanPham == null) return;

        // Mở dialog với thông tin sản phẩm đã chọn
        SanPham result = showSanPhamDialog(currentSelectedSanPham, "Sửa Sản Phẩm");

        if (result != null) {
            new Thread(() -> {
                boolean success = sanPhamDAO.update(result);
                Platform.runLater(() -> {
                    if (success) {
                        int index = masterList.indexOf(currentSelectedSanPham);
                        if (index != -1) masterList.set(index, result);

                        filterAndDisplayProducts();
                        showProductDetails(result); // Cập nhật lại chi tiết
                        DialogUtils.showSuccess("Cập nhật thành công!");
                    } else {
                        DialogUtils.showError("Lỗi", "Không thể cập nhật sản phẩm.");
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleXoa(ActionEvent event) {
        if (currentSelectedSanPham == null) return;

        boolean confirm = DialogUtils.showYesNoConfirmation(
                "Xác nhận xóa",
                "Bạn có chắc muốn xóa sản phẩm: " + currentSelectedSanPham.getTenSanPham() + "?\n" +
                        "Lưu ý: Không thể xóa nếu sản phẩm đã có trong hóa đơn."
        );

        if (confirm) {
            new Thread(() -> {
                boolean success = sanPhamDAO.delete(currentSelectedSanPham.getIdSanPham());
                Platform.runLater(() -> {
                    if (success) {
                        masterList.remove(currentSelectedSanPham);
                        filterAndDisplayProducts();
                        showDetailPlaceholder(true); // Quay về placeholder
                        DialogUtils.showSuccess("Đã xóa sản phẩm.");
                    } else {
                        DialogUtils.showError(
                                "Lỗi",
                                "Không thể xóa sản phẩm.\nSản phẩm có thể đã được sử dụng trong một hóa đơn."
                        );
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleLamMoi(ActionEvent event) {
        txtTimKiem.clear();
        cboDanhMuc.getSelectionModel().selectFirst();
        cboTrangThai.getSelectionModel().selectFirst();
        showDetailPlaceholder(true);
        loadData();
    }

    /**
     * Hàm trợ giúp: Mở Dialog Thêm/Sửa Sản phẩm
     * (Tái sử dụng logic từ controller trước)
     */
    private SanPham showSanPhamDialog(SanPham sanPham, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/qlquancoffe/views/quanly/SanPhamDialog.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            // ✅ SỬA: Lấy Scene từ detailContainer (vì rootPane không có trong FXML này)
            dialogStage.initOwner(detailContainer.getScene().getWindow());
            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);

            SanPhamDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.loadData(sanPham, danhMucList);

            dialogStage.showAndWait();

            return controller.getSanPhamResult();

        } catch (Exception e) {
            e.printStackTrace();
            DialogUtils.showError("Lỗi", "Không thể mở dialog: " + e.getMessage());
            return null;
        }
    }
}