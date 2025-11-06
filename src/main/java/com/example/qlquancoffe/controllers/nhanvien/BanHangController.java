package com.example.qlquancoffe.controllers.nhanvien;

import com.example.qlquancoffe.dao.ChiTietHoaDonDAO;
import com.example.qlquancoffe.dao.DanhMucDAO;
import com.example.qlquancoffe.dao.HoaDonDAO;
import com.example.qlquancoffe.dao.SanPhamDAO;
import com.example.qlquancoffe.models.*;
import com.example.qlquancoffe.utils.CurrencyUtil;
import com.example.qlquancoffe.utils.DateTimeUtil;
import com.example.qlquancoffe.utils.DialogUtils;
import com.example.qlquancoffe.utils.SceneSwitcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller cho màn hình Bán hàng (POS)
 * (Cập nhật: Đã thêm tính năng tự động tải lại tồn kho sau mọi thao tác)
 */
public class BanHangController {

    // ==================== FXML FIELDS ====================
    @FXML private Label lblInvoiceCode;
    @FXML private Label lblEmployeeName;
    @FXML private Label lblInvoiceDate;
    @FXML private TextField txtNote;
    @FXML private TableView<ChiTietHoaDon> tableInvoiceItems;
    @FXML private TableColumn<ChiTietHoaDon, Integer> colItemNo;
    @FXML private TableColumn<ChiTietHoaDon, String> colItemName;
    @FXML private TableColumn<ChiTietHoaDon, BigDecimal> colItemPrice;
    @FXML private TableColumn<ChiTietHoaDon, Integer> colItemQuantity;
    @FXML private TableColumn<ChiTietHoaDon, BigDecimal> colItemTotal;
    @FXML private Label lblItemCount;
    @FXML private Label lblGrandTotal;
    @FXML private Button btnNewInvoice;
    @FXML private Button btnClearInvoice;
    @FXML private Button btnPayment;
    @FXML private Button btnCancelInvoice;
    @FXML private TextField txtSearchProduct;
    @FXML private ComboBox<DanhMuc> cboCategory;
    @FXML private ComboBox<String> cboSortBy;
    @FXML private Label lblProductCount;
    @FXML private GridPane productGrid;
    @FXML private ListView<HoaDon> listPendingInvoices;
    @FXML private Button btnLoadPending;
    @FXML private Button btnDeletePending;
    @FXML private Label lblPendingCount;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblTodayPaid;
    @FXML private Label lblTodayPending;
    @FXML private TextField txtCustomerName;

    @FXML private BorderPane rootPane;

    // ==================== STATE & DAOs ====================
    private TaiKhoan currentUser;
    private HoaDon currentInvoice;
    private SanPhamDAO sanPhamDAO;
    private DanhMucDAO danhMucDAO;
    private HoaDonDAO hoaDonDAO;
    private ChiTietHoaDonDAO chiTietHoaDonDAO;
    private ObservableList<SanPham> masterProductList = FXCollections.observableArrayList();
    private ObservableList<ChiTietHoaDon> currentInvoiceItems = FXCollections.observableArrayList();
    private ObservableList<HoaDon> pendingInvoiceList = FXCollections.observableArrayList();
    private ObservableList<DanhMuc> categoryList = FXCollections.observableArrayList();

    // ==================== INITIALIZATION ====================
    @FXML
    public void initialize() {
        currentUser = SceneSwitcher.getCurrentUser();
        if (currentUser == null) { return; }

        sanPhamDAO = new SanPhamDAO();
        danhMucDAO = new DanhMucDAO();
        hoaDonDAO = new HoaDonDAO();
        chiTietHoaDonDAO = new ChiTietHoaDonDAO();

        setupInvoiceTable();
        setupPendingList();
        setupFilters();

        loadDataAsync();
        Platform.runLater(this::clearInvoiceUI);
    }

    private void loadDataAsync() {
        lblEmployeeName.setText(currentUser.getHoTen());
        // Sử dụng reloadProductList cho lần tải đầu tiên luôn để thống nhất logic
        reloadProductList();
        new Thread(() -> {
            if(categoryList.isEmpty()) categoryList.setAll(danhMucDAO.getAllDanhMuc());
            refreshPendingAndStatsInternal();
            Platform.runLater(() -> {
                if(cboCategory.getItems().isEmpty()) {
                    cboCategory.getItems().add(0, new DanhMuc(0, "Tất cả danh mục"));
                    cboCategory.getItems().addAll(categoryList);
                    cboCategory.getSelectionModel().selectFirst();
                }
            });
        }).start();
    }

    /**
     * ✅ TÍNH NĂNG MỚI: Tải lại danh sách sản phẩm và cập nhật UI
     * Giúp hiển thị đúng tồn kho mới nhất từ CSDL.
     */
    private void reloadProductList() {
        new Thread(() -> {
            // Lấy dữ liệu mới nhất từ DB
            List<SanPham> updatedList = sanPhamDAO.getAvailableForSale();
            Platform.runLater(() -> {
                // Cập nhật master list và refresh giao diện
                masterProductList.setAll(updatedList);
                filterAndDisplayProducts();
            });
        }).start();
    }

    private void refreshPendingAndStats() {
        new Thread(this::refreshPendingAndStatsInternal).start();
    }

    private void refreshPendingAndStatsInternal() {
        List<HoaDon> pending = hoaDonDAO.getPendingInvoicesByNhanVien(currentUser.getIdNhanVien());
        LocalDate today = LocalDate.now();
        BigDecimal revenue = hoaDonDAO.getTongDoanhThuNhanVien(currentUser.getIdNhanVien(), today);
        int paidCount = hoaDonDAO.countHoaDonNhanVien(currentUser.getIdNhanVien(), today);
        Platform.runLater(() -> {
            pendingInvoiceList.setAll(pending);
            lblPendingCount.setText(String.valueOf(pending.size()));
            lblTodayRevenue.setText(CurrencyUtil.formatVND(revenue));
            lblTodayPaid.setText(paidCount + " đơn");
            lblTodayPending.setText(pending.size() + " đơn");
        });
    }

    // ==================== SETUP UI ====================
    private void setupInvoiceTable() {
        tableInvoiceItems.setItems(currentInvoiceItems);
        colItemName.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colItemQuantity.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colItemTotal.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
        colItemNo.setCellFactory(col -> {
            TableCell<ChiTietHoaDon, Integer> cell = new TableCell<>();
            cell.textProperty().bind(cell.indexProperty().add(1).asString());
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        colItemPrice.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : CurrencyUtil.formatNumber(price));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        colItemTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty ? null : CurrencyUtil.formatVND(total));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        colItemQuantity.setCellFactory(col -> new QuantityCell());
    }

    private void setupPendingList() {
        listPendingInvoices.setItems(pendingInvoiceList);
        listPendingInvoices.setCellFactory(lv -> new PendingInvoiceCell());
        listPendingInvoices.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean isSelected = (selected != null);
            btnLoadPending.setDisable(!isSelected);
            btnDeletePending.setDisable(!isSelected);
        });
    }

    private void setupFilters() {
        txtSearchProduct.textProperty().addListener((obs, old, val) -> filterAndDisplayProducts());
        cboCategory.valueProperty().addListener((obs, old, val) -> filterAndDisplayProducts());
        cboSortBy.getSelectionModel().selectFirst();
        cboSortBy.valueProperty().addListener((obs, old, val) -> filterAndDisplayProducts());
    }

    // ==================== PRODUCT LIST & CARD ====================
    private void filterAndDisplayProducts() {
        String keyword = txtSearchProduct.getText().toLowerCase().trim();
        DanhMuc selectedCategory = cboCategory.getSelectionModel().getSelectedItem();
        String sortType = cboSortBy.getSelectionModel().getSelectedItem();
        List<SanPham> filteredList = masterProductList.stream()
                .filter(sp -> sp.getTenSanPham().toLowerCase().contains(keyword))
                .filter(sp -> selectedCategory == null || selectedCategory.getIdDanhMuc() == 0 || sp.getIdDanhMuc() == selectedCategory.getIdDanhMuc())
                .collect(Collectors.toList());
        if(sortType != null) {
            switch (sortType) {
                case "Tên Z-A" -> filteredList.sort((sp1, sp2) -> sp2.getTenSanPham().compareToIgnoreCase(sp1.getTenSanPham()));
                case "Giá tăng dần" -> filteredList.sort((sp1, sp2) -> sp1.getGiaBan().compareTo(sp2.getGiaBan()));
                case "Giá giảm dần" -> filteredList.sort((sp1, sp2) -> sp2.getGiaBan().compareTo(sp1.getGiaBan()));
                default -> filteredList.sort((sp1, sp2) -> sp1.getTenSanPham().compareToIgnoreCase(sp1.getTenSanPham()));
            }
        }
        displayProducts(filteredList);
    }

    private void displayProducts(List<SanPham> products) {
        productGrid.getChildren().clear();
        int col = 0, row = 0;
        for (SanPham sp : products) {
            productGrid.add(createProductCard(sp), col++, row);
            if (col == 3) { col = 0; row++; }
        }
        lblProductCount.setText(products.size() + " sản phẩm");
    }

    private Node createProductCard(SanPham sp) {
        VBox card = new VBox(5); card.getStyleClass().add("product-card");
        StackPane imageContainer = new StackPane(); imageContainer.getStyleClass().add("product-image");
        try {
            if (sp.getAnhSanPham() != null && !sp.getAnhSanPham().isEmpty()) {
                ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(sp.getAnhSanPham())));
                imageView.setFitWidth(100); imageView.setFitHeight(80); imageView.setPreserveRatio(true);
                imageContainer.getChildren().add(imageView);
            } else throw new Exception();
        } catch (Exception e) {
            Label icon = new Label("☕"); icon.setStyle("-fx-font-size: 40px; -fx-text-fill: #bdc3c7;");
            imageContainer.getChildren().add(icon);
        }
        Label name = new Label(sp.getTenSanPham()); name.getStyleClass().add("product-name"); name.setMinHeight(Region.USE_PREF_SIZE);
        Label price = new Label(CurrencyUtil.formatVND(sp.getGiaBan())); price.getStyleClass().add("product-price");
        Label stock = new Label("Kho: " + sp.getSoLuongTonKho()); stock.getStyleClass().add("product-stock");

        if (sp.getSoLuongTonKho() <= 0) {
            stock.setText("Hết hàng"); stock.getStyleClass().add("product-stock-out"); card.getStyleClass().add("product-card-out-stock");
        } else {
            card.setOnMouseClicked(e -> {
                Optional<String> result = DialogUtils.showInputDialog("Nhập số lượng", sp.getTenSanPham(), "Vui lòng nhập số lượng:", "1");
                if (result.isPresent() && !result.get().isEmpty()) {
                    try {
                        int qty = Integer.parseInt(result.get());
                        if (qty > 0) addProductToInvoice(sp, qty);
                    } catch (NumberFormatException ignored) {}
                }
            });
        }
        card.getChildren().addAll(imageContainer, name, price, stock);
        return card;
    }

    @FXML void handleClearSearch(ActionEvent event) { txtSearchProduct.clear(); cboCategory.getSelectionModel().selectFirst(); }

    // ==================== INVOICE LOGIC ====================
    private void addProductToInvoice(SanPham sp, int quantityToAdd) {
        int currentStock = getProductStock(sp.getIdSanPham());
        if (quantityToAdd > currentStock) { DialogUtils.showWarning("Không đủ hàng", "Tồn kho chỉ còn: " + currentStock); return; }
        new Thread(() -> {
            if (currentInvoice.getIdHoaDon() == 0) {
                currentInvoice.setTrangThai(HoaDon.TrangThai.PENDING);
                int newId = hoaDonDAO.insert(currentInvoice);
                if (newId > 0) {
                    currentInvoice.setIdHoaDon(newId);
                    Platform.runLater(() -> { lblInvoiceCode.setText("HĐ #" + newId); btnCancelInvoice.setDisable(false); });
                } else return;
            }
            Optional<ChiTietHoaDon> existing = currentInvoiceItems.stream().filter(i -> i.getIdSanPham() == sp.getIdSanPham()).findFirst();
            if (existing.isPresent()) {
                ChiTietHoaDon item = existing.get();
                int newQty = item.getSoLuong() + quantityToAdd;
                if (newQty > currentStock) { Platform.runLater(() -> DialogUtils.showWarning("Không đủ hàng", "Tổng số lượng vượt quá tồn kho.")); return; }
                item.setSoLuong(newQty); chiTietHoaDonDAO.update(item);
            } else {
                ChiTietHoaDon newItem = new ChiTietHoaDon(currentInvoice.getIdHoaDon(), sp.getIdSanPham(), sp.getTenSanPham(), quantityToAdd, sp.getGiaBan());
                newItem.setIdChiTietHoaDon(chiTietHoaDonDAO.insert(newItem));
                Platform.runLater(() -> currentInvoiceItems.add(newItem));
            }
            Platform.runLater(() -> {
                tableInvoiceItems.refresh();
                updateSummary();
                refreshPendingAndStats();
                reloadProductList(); // ✅ Tải lại tồn kho sau khi thêm
            });
        }).start();
    }

    private void updateSummary() {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (ChiTietHoaDon item : currentInvoiceItems) { total = total.add(item.getThanhTien()); count++; }
        lblGrandTotal.setText(CurrencyUtil.formatVND(total)); lblItemCount.setText(count + " món");
        if (currentInvoice != null) {
            currentInvoice.setTongTien(total);
            if (currentInvoice.getIdHoaDon() > 0) new Thread(() -> hoaDonDAO.update(currentInvoice)).start();
        }
        btnPayment.setDisable(total.compareTo(BigDecimal.ZERO) <= 0);
    }

    @FXML void handleNewInvoice(ActionEvent event) {
        if (currentInvoiceItems.size() > 0 && currentInvoice.getIdHoaDon() == 0) checkAndSavePending(this::clearInvoiceUI);
        else clearInvoiceUI();
    }

    @FXML void handleClearInvoice(ActionEvent event) {
        if (currentInvoiceItems.isEmpty()) return;
        if (DialogUtils.showYesNoConfirmation("Xác nhận", "Bạn có chắc muốn xóa tất cả sản phẩm khỏi hóa đơn này?")) {
            if (currentInvoice.getIdHoaDon() > 0) {
                new Thread(() -> {
                    if (chiTietHoaDonDAO.deleteByHoaDon(currentInvoice.getIdHoaDon())) {
                        Platform.runLater(() -> {
                            currentInvoiceItems.clear();
                            updateSummary();
                            reloadProductList(); // ✅ Tải lại tồn kho sau khi xóa hết
                        });
                    }
                }).start();
            } else {
                currentInvoiceItems.clear(); updateSummary();
            }
        }
    }

    private void clearInvoiceUI() {
        currentInvoice = new HoaDon(currentUser.getIdNhanVien(), "");
        currentInvoiceItems.clear();
        lblInvoiceCode.setText("HĐ MỚI");
        lblInvoiceDate.setText(DateTimeUtil.formatDateTime(currentInvoice.getNgayTao()));
        txtNote.clear(); txtCustomerName.clear();
        btnCancelInvoice.setDisable(true); btnLoadPending.setDisable(true); btnDeletePending.setDisable(true);
        listPendingInvoices.getSelectionModel().clearSelection();
        updateSummary();
    }

    // ==================== PAYMENT & PRINTING LOGIC ====================
    @FXML
    void handlePayment(ActionEvent event) {
        if (!validateInvoice()) return;
        boolean confirm = DialogUtils.showYesNoConfirmation("Xác nhận thanh toán",
                "Tổng tiền thanh toán: " + CurrencyUtil.formatVND(currentInvoice.getTongTien()) + "\n\nXác nhận thanh toán hóa đơn này?");
        if (!confirm) return;

        String note = txtNote.getText().trim();
        if (!txtCustomerName.getText().trim().isEmpty()) note = "Khách: " + txtCustomerName.getText().trim() + ". " + note;
        currentInvoice.setGhiChu(note);

        final HoaDon invoiceToPrint = currentInvoice;
        final List<ChiTietHoaDon> itemsToPrint = new ArrayList<>(currentInvoiceItems);
        final TaiKhoan cashierToPrint = currentUser;

        new Thread(() -> {
            if (currentInvoice.getIdHoaDon() == 0) {
                int newId = hoaDonDAO.insert(currentInvoice);
                if (newId > 0) {
                    currentInvoice.setIdHoaDon(newId);
                    invoiceToPrint.setIdHoaDon(newId);
                    for(ChiTietHoaDon ct : currentInvoiceItems) ct.setIdHoaDon(newId);
                    chiTietHoaDonDAO.insertBatch(currentInvoiceItems);
                }
            }
            boolean success = hoaDonDAO.completePayment(currentInvoice.getIdHoaDon());
            Platform.runLater(() -> {
                if (success) {
                    boolean wantToPrint = DialogUtils.showYesNoConfirmation("In hóa đơn", "Thanh toán thành công!\nBạn có muốn xuất hóa đơn ra file không?");
                    if (wantToPrint) { exportInvoiceToFile(invoiceToPrint, itemsToPrint, cashierToPrint); }
                    clearInvoiceUI();
                    refreshPendingAndStats();
                    // Không cần reloadProductList ở đây nếu logic của bạn là trừ tồn kho ngay khi thêm vào giỏ
                    // Nếu trừ tồn kho khi thanh toán thì cần thêm reloadProductList() tại đây.
                } else {
                    DialogUtils.showError("Lỗi", "Thanh toán thất bại.");
                }
            });
        }).start();
    }

    private void exportInvoiceToFile(HoaDon invoice, List<ChiTietHoaDon> items, TaiKhoan cashier) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu hóa đơn");
        fileChooser.setInitialFileName("HoaDon_" + invoice.getIdHoaDon() + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(btnPayment.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("========== HÓA ĐƠN BÁN HÀNG ==========\n");
                writer.write("Mã hóa đơn: #" + invoice.getIdHoaDon() + "\n");
                writer.write("Ngày: " + DateTimeUtil.formatDateTime(LocalDateTime.now()) + "\n");
                writer.write("Thu ngân: " + cashier.getHoTen() + "\n");
                writer.write("--------------------------------------\n");
                writer.write(String.format("%-20s %5s %12s\n", "Mặt hàng", "SL", "Thành tiền"));
                writer.write("--------------------------------------\n");
                for (ChiTietHoaDon item : items) {
                    writer.write(String.format("%-20s %5d %12s\n", truncate(item.getTenSanPham(), 20), item.getSoLuong(), CurrencyUtil.formatNumber(item.getThanhTien())));
                }
                writer.write("--------------------------------------\n");
                writer.write(String.format("%-26s %12s\n", "TỔNG TIỀN:", CurrencyUtil.formatVND(invoice.getTongTien())));
                writer.write("======================================\n");
                writer.write("\nCảm ơn quý khách và hẹn gặp lại!\n");
                DialogUtils.showSuccess("Đã xuất hóa đơn", "File đã được lưu tại:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                DialogUtils.showError("Lỗi xuất file", "Không thể lưu file hóa đơn: " + e.getMessage());
            }
        }
    }

    private String truncate(String str, int len) {
        return (str.length() > len) ? str.substring(0, len - 3) + "..." : str;
    }

    @FXML void handleLoadPending(ActionEvent event) {
        HoaDon selected = listPendingInvoices.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (currentInvoiceItems.size() > 0 && currentInvoice.getIdHoaDon() == 0) checkAndSavePending(() -> loadPendingInvoice(selected));
        else loadPendingInvoice(selected);
    }
    private void loadPendingInvoice(HoaDon hoaDon) {
        new Thread(() -> {
            List<ChiTietHoaDon> items = chiTietHoaDonDAO.getChiTietByHoaDon(hoaDon.getIdHoaDon());
            Platform.runLater(() -> {
                currentInvoice = hoaDon; currentInvoiceItems.setAll(items);
                lblInvoiceCode.setText("HĐ #" + hoaDon.getIdHoaDon());
                lblInvoiceDate.setText(DateTimeUtil.formatDateTime(hoaDon.getNgayTao()));
                txtNote.setText(hoaDon.getGhiChu());
                btnCancelInvoice.setDisable(false); updateSummary();
            });
        }).start();
    }
    private void checkAndSavePending(Runnable onComplete) {
        if (currentInvoiceItems.isEmpty() || currentInvoice.getIdHoaDon() > 0) { if (onComplete != null) Platform.runLater(onComplete); return; }
        if (!DialogUtils.showYesNoConfirmation("Lưu hóa đơn tạm?", "Hóa đơn hiện tại chưa được lưu. Bạn có muốn LƯU TẠM hóa đơn này?")) {
            if (onComplete != null) Platform.runLater(onComplete); return;
        }
        String note = txtNote.getText().trim();
        if (!txtCustomerName.getText().trim().isEmpty()) note = "Khách: " + txtCustomerName.getText().trim() + ". " + note;
        currentInvoice.setGhiChu(note);
        new Thread(() -> {
            int newId = hoaDonDAO.insert(currentInvoice);
            if (newId > 0) {
                currentInvoice.setIdHoaDon(newId);
                for(ChiTietHoaDon ct : currentInvoiceItems) ct.setIdHoaDon(newId);
                if (chiTietHoaDonDAO.insertBatch(currentInvoiceItems)) {
                    Platform.runLater(() -> { DialogUtils.showInfo("Đã lưu tạm", "Hóa đơn cũ đã được lưu vào danh sách chờ."); refreshPendingAndStats(); if (onComplete != null) onComplete.run(); });
                } else Platform.runLater(() -> DialogUtils.showError("Lỗi", "Không thể lưu hóa đơn tạm."));
            }
        }).start();
    }

    /**
     * ✅ CẬP NHẬT: Hủy hóa đơn và xóa chi tiết để hoàn kho
     */
    @FXML void handleCancelInvoice(ActionEvent event) {
        if (currentInvoice.getIdHoaDon() == 0) return;
        if (!DialogUtils.showYesNoConfirmation("Hủy đơn", "Hủy hóa đơn #" + currentInvoice.getIdHoaDon() + "?")) return;
        new Thread(() -> {
            // 1. Hủy hóa đơn (đổi trạng thái)
            if (hoaDonDAO.cancelInvoice(currentInvoice.getIdHoaDon())) {
                // 2. Xóa chi tiết để kích hoạt trigger hoàn kho DB (như yêu cầu)
                chiTietHoaDonDAO.deleteByHoaDon(currentInvoice.getIdHoaDon());
                Platform.runLater(() -> {
                    DialogUtils.showSuccess("Đã hủy hóa đơn!");
                    clearInvoiceUI();
                    refreshPendingAndStats();
                    reloadProductList(); // 3. Tải lại tồn kho
                });
            } else {
                Platform.runLater(() -> DialogUtils.showError("Lỗi", "Không thể hủy hóa đơn."));
            }
        }).start();
    }

    /**
     * Xóa đơn chờ (cũng cần hoàn kho nếu đơn chờ đã giữ chỗ hàng)
     */
    @FXML void handleDeletePending(ActionEvent event) {
        HoaDon selected = listPendingInvoices.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (DialogUtils.showYesNoConfirmation("Xóa đơn chờ", "Bạn có chắc muốn xóa đơn chờ #" + selected.getIdHoaDon() + "?")) {
            new Thread(() -> {
                chiTietHoaDonDAO.deleteByHoaDon(selected.getIdHoaDon());
                if (hoaDonDAO.delete(selected.getIdHoaDon())) {
                    Platform.runLater(() -> {
                        refreshPendingAndStats();
                        reloadProductList();
                    });
                }
            }).start();
        }
    }

    @FXML void handleRefreshPending(ActionEvent event) { refreshPendingAndStats(); }

    // ==================== HELPERS & INNER CLASSES ====================
    private int getProductStock(int id) { return masterProductList.stream().filter(sp -> sp.getIdSanPham() == id).map(SanPham::getSoLuongTonKho).findFirst().orElse(0); }
    private boolean validateInvoice() { if (currentInvoiceItems.isEmpty()) { DialogUtils.showWarning("Hóa đơn trống", "Vui lòng thêm sản phẩm vào hóa đơn."); return false; } return true; }

    private class QuantityCell extends TableCell<ChiTietHoaDon, Integer> {
        private final HBox container = new HBox(5);
        private final Button btnDec = new Button("-"), btnInc = new Button("+"), btnDel = new Button("x");
        private final Label lblQty = new Label();
        public QuantityCell() {
            container.setAlignment(Pos.CENTER); container.getChildren().addAll(btnDec, lblQty, btnInc, btnDel);
            btnDec.getStyleClass().add("btn-xs"); btnInc.getStyleClass().add("btn-xs"); btnDel.getStyleClass().addAll("btn-xs", "btn-danger");
            btnDec.setOnAction(e -> changeQty(-1)); btnInc.setOnAction(e -> changeQty(1)); btnDel.setOnAction(e -> deleteItem());
        }
        private void changeQty(int delta) {
            ChiTietHoaDon item = getTableView().getItems().get(getIndex());
            int newQty = item.getSoLuong() + delta;
            if (newQty < 1) deleteItem();
            else if (newQty > getProductStock(item.getIdSanPham())) DialogUtils.showWarning("Hết hàng", "Không đủ tồn kho.");
            else updateItemQtyInDB(item, newQty);
        }
        private void updateItemQtyInDB(ChiTietHoaDon item, int qty) {
            new Thread(() -> {
                item.setSoLuong(qty);
                if (chiTietHoaDonDAO.update(item)) {
                    Platform.runLater(() -> {
                        tableInvoiceItems.refresh();
                        updateSummary();
                        reloadProductList();
                    });
                }
            }).start();
        }
        private void deleteItem() {
            ChiTietHoaDon item = getTableView().getItems().get(getIndex());
            new Thread(() -> {
                if (chiTietHoaDonDAO.delete(item.getIdChiTietHoaDon())) {
                    Platform.runLater(() -> {
                        currentInvoiceItems.remove(item);
                        updateSummary();
                        reloadProductList();
                    });
                }
            }).start();
        }
        @Override protected void updateItem(Integer qty, boolean empty) { super.updateItem(qty, empty); if (empty || qty == null) setGraphic(null); else { lblQty.setText(String.valueOf(qty)); setGraphic(container); } }
    }

    private class PendingInvoiceCell extends ListCell<HoaDon> {
        private final VBox card = new VBox(5); private final HBox header = new HBox(10);
        private final Label lblId = new Label(), lblTime = new Label(), lblNote = new Label(), lblTotal = new Label();
        public PendingInvoiceCell() {
            card.getStyleClass().add("pending-card"); lblId.getStyleClass().add("pending-id"); lblTime.getStyleClass().add("pending-time"); lblNote.getStyleClass().add("pending-items"); lblTotal.getStyleClass().add("pending-total");
            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS); header.getChildren().addAll(lblId, lblTime, spacer, lblTotal); card.getChildren().addAll(header, lblNote);
        }
        @Override protected void updateItem(HoaDon h, boolean e) { super.updateItem(h, e); if (e || h == null) setGraphic(null); else { lblId.setText("HĐ #" + h.getIdHoaDon()); lblTime.setText(DateTimeUtil.getRelativeTime(h.getNgayTao())); lblNote.setText(h.getGhiChu() == null || h.getGhiChu().isEmpty() ? "Không có ghi chú" : h.getGhiChu()); lblTotal.setText(CurrencyUtil.formatVND(h.getTongTien())); setGraphic(card); } }
    }
}