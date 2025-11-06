package com.example.qlquancoffe.controllers.quanly;

import com.example.qlquancoffe.dao.BaoCaoDAO;
import com.example.qlquancoffe.dao.DanhMucDAO;
import com.example.qlquancoffe.models.BaoCaoData;
import com.example.qlquancoffe.models.BaoCaoRow;
import com.example.qlquancoffe.models.DanhMuc;
import com.example.qlquancoffe.utils.CurrencyUtil;
import com.example.qlquancoffe.utils.DialogUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BaoCaoController implements Initializable {

    // === FXML Fields ===
    @FXML private DatePicker dpTuNgay;
    @FXML private DatePicker dpDenNgay;
    @FXML private ComboBox<DanhMuc> cboDanhMuc;

    @FXML private Label lblTongSoLuong;
    @FXML private Label lblTongTien;
    @FXML private TreeTableView<BaoCaoRow> treeBaoCao;
    @FXML private TreeTableColumn<BaoCaoRow, String> colTen;
    @FXML private TreeTableColumn<BaoCaoRow, Integer> colSoLuong;
    @FXML private TreeTableColumn<BaoCaoRow, BigDecimal> colDonGia;
    @FXML private TreeTableColumn<BaoCaoRow, BigDecimal> colDoanhThu;

    // Panel Top 3
    @FXML private VBox topProductsList;
    @FXML private Label lblTopProductsPlaceholder;

    private BaoCaoDAO baoCaoDAO;
    private DanhMucDAO danhMucDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.baoCaoDAO = new BaoCaoDAO();
        this.danhMucDAO = new DanhMucDAO();

        setupTreeTable();

        LocalDate today = LocalDate.now();
        dpTuNgay.setValue(today.withDayOfMonth(1));
        dpDenNgay.setValue(today);

        loadDanhMuc();
    }

    private void setupTreeTable() {
        colTen.setCellValueFactory(new TreeItemPropertyValueFactory<>("ten"));
        colSoLuong.setCellValueFactory(new TreeItemPropertyValueFactory<>("soLuong"));
        colDonGia.setCellValueFactory(new TreeItemPropertyValueFactory<>("donGiaTB"));
        colDoanhThu.setCellValueFactory(new TreeItemPropertyValueFactory<>("thanhTien"));

        formatMoneyColumn(colDonGia);
        formatMoneyColumn(colDoanhThu);
        styleCategoryRow(treeBaoCao);
    }

    /**
     * Tải danh mục vào ComboBox
     */
    private void loadDanhMuc() {
        new Thread(() -> {
            List<DanhMuc> list = danhMucDAO.getAllDanhMuc();
            Platform.runLater(() -> {
                cboDanhMuc.getItems().add(new DanhMuc(0, "Tất cả danh mục"));
                cboDanhMuc.getItems().addAll(list);
                cboDanhMuc.getSelectionModel().selectFirst();

                // Sau khi tải danh mục xong mới tải báo cáo lần đầu
                handleXemBaoCao();
            });
        }).start();
    }

    @FXML
    void handleXemBaoCao() {
        LocalDate from = dpTuNgay.getValue();
        LocalDate to = dpDenNgay.getValue();
        DanhMuc selectedCategory = cboDanhMuc.getValue();

        if (from == null || to == null) {
            DialogUtils.showWarning("Chưa chọn ngày", "Vui lòng chọn đầy đủ 'Từ ngày' và 'Đến ngày'.");
            return;
        }
        if (from.isAfter(to)) {
            DialogUtils.showWarning("Ngày không hợp lệ", "'Từ ngày' phải nhỏ hơn hoặc bằng 'Đến ngày'.");
            return;
        }

        // Lấy ID danh mục (0 nếu chọn "Tất cả")
        int idDanhMuc = (selectedCategory != null) ? selectedCategory.getIdDanhMuc() : 0;

        loadBaoCao(from, to, idDanhMuc);
        loadTop3();
    }

    private void loadBaoCao(LocalDate from, LocalDate to, int idDanhMuc) {
        new Thread(() -> {
            List<BaoCaoData> data = baoCaoDAO.getBaoCao(from, to, idDanhMuc);
            Platform.runLater(() -> buildTree(data));
        }).start();
    }

    private void loadTop3() {
        topProductsList.getChildren().clear();
        lblTopProductsPlaceholder.setVisible(false);
        lblTopProductsPlaceholder.setManaged(false);
        LocalDate nowDate = LocalDate.now();
        new Thread(() -> {
            List<BaoCaoData> topData = baoCaoDAO.getTop3SanPham(nowDate.getYear(), nowDate.getMonthValue());
            Platform.runLater(() -> {
                if (topData.isEmpty()) {
                    lblTopProductsPlaceholder.setVisible(true);
                    lblTopProductsPlaceholder.setManaged(true);
                } else {
                    int rank = 1;
                    for (BaoCaoData row : topData) {
                        Node card = createTopProductCard(rank, row.getTenSanPham(), row.getSoLuong());
                        topProductsList.getChildren().add(card);
                        rank++;
                    }
                }
            });
        }).start();
    }

    private void buildTree(List<BaoCaoData> data) {
        TreeItem<BaoCaoRow> root = new TreeItem<>();

        int tongSoLuong = 0;
        BigDecimal tongThanhTien = BigDecimal.ZERO;

        for (BaoCaoData item : data) {
            BaoCaoRow productRow = new BaoCaoRow(item.getTenSanPham(), item.getSoLuong(), item.getThanhTien());
            TreeItem<BaoCaoRow> productItem = new TreeItem<>(productRow);

            root.getChildren().add(productItem);

            tongSoLuong += item.getSoLuong();
            tongThanhTien = tongThanhTien.add(item.getThanhTien());
        }

        treeBaoCao.setRoot(root);
        lblTongSoLuong.setText("Tổng SL: " + tongSoLuong);
        lblTongTien.setText("Tổng tiền: " + CurrencyUtil.formatVND(tongThanhTien));
    }

    private Node createTopProductCard(int rank, String tenSanPham, int soLuong) {
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

    private void formatMoneyColumn(TreeTableColumn<BaoCaoRow, BigDecimal> column) {
        column.setCellFactory(col -> new TreeTableCell<BaoCaoRow, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : CurrencyUtil.formatNumber(item));
            }
        });
    }

    private void styleCategoryRow(TreeTableView<BaoCaoRow> tree) {
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