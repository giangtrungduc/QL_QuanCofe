package com.example.qlquancoffe.controllers.quanly;

import com.example.qlquancoffe.dao.BaoCaoDAO;
import com.example.qlquancoffe.models.BaoCaoData; // ✅ SỬA: Import model mới
import com.example.qlquancoffe.models.BaoCaoRow; // Model từ bước trước
import com.example.qlquancoffe.utils.CurrencyUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

// ❌ SỬA: Xóa 'record BaoCaoData' lồng nhau

public class BaoCaoController implements Initializable {

    // === FXML Fields ===
    @FXML private DatePicker dpNgay;
    @FXML private DatePicker dpThang;
    @FXML private TabPane tabPane;
    @FXML private Tab tabNgay;
    @FXML private Tab tabThang;

    // Tab Ngày
    @FXML private Label lblTongSoLuongNgay;
    @FXML private Label lblTongTienNgay;
    @FXML private TreeTableView<BaoCaoRow> treeNgay;
    @FXML private TreeTableColumn<BaoCaoRow, String> colTenNgay;
    @FXML private TreeTableColumn<BaoCaoRow, Integer> colSoLuongNgay;
    @FXML private TreeTableColumn<BaoCaoRow, BigDecimal> colDonGiaNgay;
    @FXML private TreeTableColumn<BaoCaoRow, BigDecimal> colDoanhThuNgay;

    // Tab Tháng
    @FXML private Label lblTongSoLuongThang;
    @FXML private Label lblTongTienThang;
    @FXML private TreeTableView<BaoCaoRow> treeThang;
    @FXML private TreeTableColumn<BaoCaoRow, String> colTenThang;
    @FXML private TreeTableColumn<BaoCaoRow, Integer> colSoLuongThang;
    @FXML private TreeTableColumn<BaoCaoRow, BigDecimal> colDonGiaThang;
    @FXML private TreeTableColumn<BaoCaoRow, BigDecimal> colDoanhThuThang;

    // Panel Top 3
    @FXML private VBox topProductsPane;
    @FXML private VBox topProductsList;
    @FXML private Label lblTopProductsPlaceholder;

    private BaoCaoDAO baoCaoDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.baoCaoDAO = new BaoCaoDAO();

        setupTreeTables();
        setupTabListener();

        dpNgay.setValue(LocalDate.now());
        dpThang.setValue(LocalDate.now());

        loadBaoCaoNgay();
        loadBaoCaoThang();
        loadTop3();
    }

    /**
     * Cài đặt các cột cho cả 2 TreeTableView
     */
    private void setupTreeTables() {
        // (Giữ nguyên)
        colTenNgay.setCellValueFactory(new TreeItemPropertyValueFactory<>("ten"));
        colSoLuongNgay.setCellValueFactory(new TreeItemPropertyValueFactory<>("soLuong"));
        colDonGiaNgay.setCellValueFactory(new TreeItemPropertyValueFactory<>("donGiaTB"));
        colDoanhThuNgay.setCellValueFactory(new TreeItemPropertyValueFactory<>("thanhTien"));
        colTenThang.setCellValueFactory(new TreeItemPropertyValueFactory<>("ten"));
        colSoLuongThang.setCellValueFactory(new TreeItemPropertyValueFactory<>("soLuong"));
        colDonGiaThang.setCellValueFactory(new TreeItemPropertyValueFactory<>("donGiaTB"));
        colDoanhThuThang.setCellValueFactory(new TreeItemPropertyValueFactory<>("thanhTien"));
        formatMoneyColumn(colDonGiaNgay);
        formatMoneyColumn(colDoanhThuNgay);
        formatMoneyColumn(colDonGiaThang);
        formatMoneyColumn(colDoanhThuThang);
        styleCategoryRow(treeNgay);
        styleCategoryRow(treeThang);
    }

    /**
     * Cài đặt listener để Ẩn/Hiện panel Top 3
     */
    private void setupTabListener() {
        // (Giữ nguyên)
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            boolean isThangTab = (newTab == tabThang);
            topProductsPane.setVisible(isThangTab);
            topProductsPane.setManaged(isThangTab);
            if (isThangTab) {
                loadTop3();
            }
        });
        topProductsPane.setVisible(false);
        topProductsPane.setManaged(false);
    }

    @FXML
    void onChangeNgay() {
        loadBaoCaoNgay();
    }

    @FXML
    void onChangeThang() {
        loadBaoCaoThang();
        loadTop3();
    }

    /**
     * Tải và xây dựng cây báo cáo theo ngày
     */
    private void loadBaoCaoNgay() {
        LocalDate selectedDate = dpNgay.getValue();
        if (selectedDate == null) return;

        new Thread(() -> {
            // ✅ SỬA: Lấy kiểu dữ liệu mới
            List<BaoCaoData> data = baoCaoDAO.getBaoCaoNgay(selectedDate);
            Platform.runLater(() -> buildTree(data, treeNgay, lblTongSoLuongNgay, lblTongTienNgay));
        }).start();
    }

    /**
     * Tải và xây dựng cây báo cáo theo tháng
     */
    private void loadBaoCaoThang() {
        LocalDate selectedDate = dpThang.getValue();
        if (selectedDate == null) return;

        new Thread(() -> {
            // ✅ SỬA: Lấy kiểu dữ liệu mới
            List<BaoCaoData> data = baoCaoDAO.getBaoCaoThang(selectedDate.getYear(), selectedDate.getMonthValue());
            Platform.runLater(() -> buildTree(data, treeThang, lblTongSoLuongThang, lblTongTienThang));
        }).start();
    }

    /**
     * Tải và hiển thị Top 3
     */
    private void loadTop3() {
        LocalDate selectedDate = dpThang.getValue();
        if (selectedDate == null) return;

        topProductsList.getChildren().clear();
        lblTopProductsPlaceholder.setVisible(false);
        lblTopProductsPlaceholder.setManaged(false);

        new Thread(() -> {
            // ✅ SỬA: Lấy kiểu dữ liệu mới
            List<BaoCaoData> topData = baoCaoDAO.getTop3SanPham(selectedDate.getYear(), selectedDate.getMonthValue());

            Platform.runLater(() -> {
                if (topData.isEmpty()) {
                    lblTopProductsPlaceholder.setVisible(true);
                    lblTopProductsPlaceholder.setManaged(true);
                } else {
                    int rank = 1;
                    for (BaoCaoData row : topData) {
                        // ✅ SỬA: Dùng getter
                        Node card = createTopProductCard(rank, row.getTenSanPham(), row.getSoLuong());
                        topProductsList.getChildren().add(card);
                        rank++;
                    }
                }
            });
        }).start();
    }


    /**
     * Xây dựng cấu trúc Tree (Category -> Product) từ danh sách
     */
    // ✅ SỬA: Nhận List<BaoCaoData>
    private void buildTree(List<BaoCaoData> data, TreeTableView<BaoCaoRow> tree, Label lblSL, Label lblTien) {
        TreeItem<BaoCaoRow> root = new TreeItem<>();
        Map<String, TreeItem<BaoCaoRow>> categoryMap = new HashMap<>();

        int tongSoLuong = 0;
        BigDecimal tongThanhTien = BigDecimal.ZERO;

        for (BaoCaoData item : data) { // ✅ SỬA: Dùng BaoCaoData
            // 1. Tạo hàng sản phẩm (lá)
            // ✅ SỬA: Dùng getter
            BaoCaoRow productRow = new BaoCaoRow(item.getTenSanPham(), item.getSoLuong(), item.getThanhTien());
            TreeItem<BaoCaoRow> productItem = new TreeItem<>(productRow);

            // 2. Tìm hoặc tạo node Danh mục (cha)
            // ✅ SỬA: Dùng getter
            String tenDanhMuc = item.getTenDanhMuc();
            TreeItem<BaoCaoRow> categoryItem = categoryMap.get(tenDanhMuc);

            if (categoryItem == null) {
                BaoCaoRow categoryRow = new BaoCaoRow(tenDanhMuc);
                categoryItem = new TreeItem<>(categoryRow);
                categoryMap.put(tenDanhMuc, categoryItem);
                root.getChildren().add(categoryItem);
                categoryItem.setExpanded(true);
            }

            // 3. Thêm sản phẩm (lá) vào danh mục (cha)
            categoryItem.getChildren().add(productItem);

            // 4. Cập nhật tổng cho danh mục (hàng cha)
            categoryItem.getValue().add(productRow);

            // 5. Cập nhật tổng chung
            tongSoLuong += item.getSoLuong(); // ✅ SỬA: Dùng getter
            tongThanhTien = tongThanhTien.add(item.getThanhTien()); // ✅ SỬA: Dùng getter
        }

        tree.setRoot(root);
        lblSL.setText("Tổng SL: " + tongSoLuong);
        lblTien.setText("Tổng tiền: " + CurrencyUtil.formatVND(tongThanhTien));
    }

    /**
     * Tạo giao diện cho 1 thẻ Top 3
     */
    private Node createTopProductCard(int rank, String tenSanPham, int soLuong) {
        // (Giữ nguyên)
        HBox card = new HBox(10);
        card.getStyleClass().add("top-product-card");
        card.getStyleClass().add("top-product-card-" + rank);
        Label lblRank = new Label(rank + ".");
        lblRank.getStyleClass().add("top-product-rank");
        VBox details = new VBox(2);
        details.getStyleClass().add("top-product-details");
        Label lblName = new Label(tenSanPham);
        lblName.getStyleClass().add("top-product-name");
        Label lblQty = new Label(soLuong + " lượt bán");
        lblQty.getStyleClass().add("top-product-qty");
        details.getChildren().addAll(lblName, lblQty);
        card.getChildren().addAll(lblRank, details);
        return card;
    }

    /**
     * Helper: Áp dụng định dạng tiền
     */
    private void formatMoneyColumn(TreeTableColumn<BaoCaoRow, BigDecimal> column) {
        // (Giữ nguyên)
        column.setCellFactory(col -> new TreeTableCell<BaoCaoRow, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : CurrencyUtil.formatNumber(item));
            }
        });
    }

    /**
     * Helper: Áp dụng style cho hàng Danh mục
     */
    private void styleCategoryRow(TreeTableView<BaoCaoRow> tree) {
        // (Giữ nguyên)
        tree.setRowFactory(tv -> new TreeTableRow<BaoCaoRow>() {
            @Override
            protected void updateItem(BaoCaoRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("category-row");
                if (item != null && !empty && item.isCategory()) {
                    getStyleClass().add("category-row");
                }
            }
        });
    }
}