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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller cho màn hình Bán hàng (POS)
 * (Hoàn thiện - Đã thêm logic Dialog nhập số lượng)
 */
public class BanHangController {

    // ==================== FXML FIELDS ====================

    // === Hóa đơn hiện tại ===
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

    // === Danh sách sản phẩm ===
    @FXML private TextField txtSearchProduct;
    @FXML private ComboBox<DanhMuc> cboCategory;
    @FXML private ComboBox<String> cboSortBy;
    @FXML private Label lblProductCount;
    @FXML private GridPane productGrid;

    // === Hóa đơn tạm (Pending) ===
    @FXML private ListView<HoaDon> listPendingInvoices;
    @FXML private Button btnLoadPending;
    @FXML private Button btnDeletePending;
    @FXML private Label lblPendingCount;

    // === Thống kê Nhân viên ===
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblTodayPaid;
    @FXML private Label lblTodayPending;

    // === Thông tin Khách (từ FXML) ===
    @FXML private TextField txtCustomerName;

    // ==================== STATE & DAOs ====================
    private TaiKhoan currentUser;
    private HoaDon currentInvoice; // Hóa đơn đang thao tác

    // DAOs
    private SanPhamDAO sanPhamDAO;
    private DanhMucDAO danhMucDAO;
    private HoaDonDAO hoaDonDAO;
    private ChiTietHoaDonDAO chiTietHoaDonDAO;

    // Data Lists
    private ObservableList<SanPham> masterProductList = FXCollections.observableArrayList();
    private ObservableList<ChiTietHoaDon> currentInvoiceItems = FXCollections.observableArrayList();
    private ObservableList<HoaDon> pendingInvoiceList = FXCollections.observableArrayList();
    private ObservableList<DanhMuc> categoryList = FXCollections.observableArrayList();

    // ==================== INITIALIZATION ====================

    @FXML
    public void initialize() {
        currentUser = SceneSwitcher.getCurrentUser();
        if (currentUser == null) {
            DialogUtils.showError("Lỗi", "Không thể lấy thông tin nhân viên.");
            return;
        }

        // Khởi tạo DAOs
        sanPhamDAO = new SanPhamDAO();
        danhMucDAO = new DanhMucDAO();
        hoaDonDAO = new HoaDonDAO();
        chiTietHoaDonDAO = new ChiTietHoaDonDAO();

        // Thiết lập giao diện
        setupInvoiceTable();
        setupPendingList();
        setupFilters();

        // Tải dữ liệu
        loadDataAsync();

        // Bắt đầu với hóa đơn mới
        Platform.runLater(this::clearInvoiceUI);
    }

    /**
     * Tải tất cả dữ liệu (sản phẩm, danh mục, HĐ tạm, thống kê)
     * một cách bất đồng bộ
     */
    private void loadDataAsync() {
        lblEmployeeName.setText(currentUser.getHoTen());

        new Thread(() -> {
            // Tải sản phẩm (chỉ tải 1 lần)
            if(masterProductList.isEmpty()) {
                masterProductList.setAll(sanPhamDAO.getAvailableForSale());
            }
            // Tải danh mục (chỉ tải 1 lần)
            if(categoryList.isEmpty()) {
                categoryList.setAll(danhMucDAO.getAllDanhMuc());
            }

            // Tải HĐ tạm
            List<HoaDon> pending = hoaDonDAO.getPendingInvoicesByNhanVien(currentUser.getIdNhanVien());

            // Tải thống kê
            LocalDate today = LocalDate.now();
            BigDecimal revenue = hoaDonDAO.getTongDoanhThuNhanVien(currentUser.getIdNhanVien(), today);
            int paidCount = hoaDonDAO.countHoaDonNhanVien(currentUser.getIdNhanVien(), today);

            Platform.runLater(() -> {
                // Hiển thị sản phẩm
                filterAndDisplayProducts();
                lblProductCount.setText(masterProductList.size() + " sản phẩm");

                // Hiển thị danh mục (nếu chưa có)
                if(cboCategory.getItems().isEmpty()) {
                    cboCategory.getItems().add(0, new DanhMuc(0, "Tất cả danh mục"));
                    cboCategory.getItems().addAll(categoryList);
                    cboCategory.getSelectionModel().selectFirst();
                }

                // Hiển thị HĐ tạm
                pendingInvoiceList.setAll(pending);
                lblPendingCount.setText(String.valueOf(pending.size()));

                // Hiển thị thống kê
                lblTodayRevenue.setText(CurrencyUtil.formatVND(revenue));
                lblTodayPaid.setText(paidCount + " đơn");
                lblTodayPending.setText(pending.size() + " đơn");
            });
        }).start();
    }

    /**
     * Tải lại chỉ HĐ tạm và thống kê
     */
    private void refreshPendingAndStats() {
        new Thread(() -> {
            // Tải HĐ tạm
            List<HoaDon> pending = hoaDonDAO.getPendingInvoicesByNhanVien(currentUser.getIdNhanVien());

            // Tải thống kê
            LocalDate today = LocalDate.now();
            BigDecimal revenue = hoaDonDAO.getTongDoanhThuNhanVien(currentUser.getIdNhanVien(), today);
            int paidCount = hoaDonDAO.countHoaDonNhanVien(currentUser.getIdNhanVien(), today);

            Platform.runLater(() -> {
                // Hiển thị HĐ tạm
                pendingInvoiceList.setAll(pending);
                lblPendingCount.setText(String.valueOf(pending.size()));

                // Hiển thị thống kê
                lblTodayRevenue.setText(CurrencyUtil.formatVND(revenue));
                lblTodayPaid.setText(paidCount + " đơn");
                lblTodayPending.setText(pending.size() + " đơn");
            });
        }).start();
    }

    // ==================== SETUP UI ====================

    /**
     * Cài đặt các cột cho bảng Chi tiết Hóa đơn
     */
    private void setupInvoiceTable() {
        tableInvoiceItems.setItems(currentInvoiceItems);
        colItemName.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colItemQuantity.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colItemTotal.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));

        // Cột STT (tự động đếm)
        colItemNo.setCellFactory(col -> {
            TableCell<ChiTietHoaDon, Integer> cell = new TableCell<>();
            cell.textProperty().bind(cell.indexProperty().add(1).asString());
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        // Format Cột Giá
        colItemPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : CurrencyUtil.formatNumber(price));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        // Format Cột Thành Tiền
        colItemTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty ? null : CurrencyUtil.formatVND(total));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        // Cột Số lượng (với nút +/- và Xóa)
        colItemQuantity.setCellFactory(col -> new QuantityCell());

        // Set placeholder (FXML đã có)
    }

    /**
     * Cài đặt hiển thị cho ListView Hóa đơn tạm
     */
    private void setupPendingList() {
        listPendingInvoices.setItems(pendingInvoiceList);
        listPendingInvoices.setCellFactory(lv -> new PendingInvoiceCell());

        // Khi chọn 1 HĐ tạm, bật nút "Mở" và "Xóa"
        listPendingInvoices.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean isSelected = (selected != null);
            btnLoadPending.setDisable(!isSelected);
            btnDeletePending.setDisable(!isSelected);
        });

        // Set placeholder (FXML đã có)
    }

    /**
     * Cài đặt bộ lọc (Tìm kiếm và Danh mục)
     */
    private void setupFilters() {
        // Lọc khi gõ
        txtSearchProduct.textProperty().addListener((obs, old, val) -> filterAndDisplayProducts());
        // Lọc khi chọn
        cboCategory.valueProperty().addListener((obs, old, val) -> filterAndDisplayProducts());

        // Sắp xếp
        cboSortBy.getSelectionModel().selectFirst();
        cboSortBy.valueProperty().addListener((obs, old, val) -> filterAndDisplayProducts());
    }

    // ==================== PRODUCT LIST LOGIC ====================

    /**
     * Lọc và hiển thị sản phẩm dựa trên các bộ lọc
     */
    private void filterAndDisplayProducts() {
        String keyword = txtSearchProduct.getText().toLowerCase().trim();
        DanhMuc selectedCategory = cboCategory.getSelectionModel().getSelectedItem();
        String sortType = cboSortBy.getSelectionModel().getSelectedItem();

        List<SanPham> filteredList = masterProductList.stream()
                .filter(sp -> sp.getTenSanPham().toLowerCase().contains(keyword))
                .filter(sp -> selectedCategory == null || selectedCategory.getIdDanhMuc() == 0 || sp.getIdDanhMuc() == selectedCategory.getIdDanhMuc())
                .collect(Collectors.toList());

        // Sắp xếp
        if(sortType != null) {
            switch (sortType) {
                case "Tên Z-A":
                    filteredList.sort((sp1, sp2) -> sp2.getTenSanPham().compareToIgnoreCase(sp1.getTenSanPham()));
                    break;
                case "Giá tăng dần":
                    filteredList.sort((sp1, sp2) -> sp1.getGiaBan().compareTo(sp2.getGiaBan()));
                    break;
                case "Giá giảm dần":
                    filteredList.sort((sp1, sp2) -> sp2.getGiaBan().compareTo(sp1.getGiaBan()));
                    break;
                case "Tên A-Z":
                default:
                    filteredList.sort((sp1, sp2) -> sp1.getTenSanPham().compareToIgnoreCase(sp1.getTenSanPham()));
                    break;
            }
        }

        // Hiển thị lên lưới
        displayProducts(filteredList);
    }

    /**
     * Hiển thị danh sách sản phẩm lên GridPane
     */
    private void displayProducts(List<SanPham> products) {
        productGrid.getChildren().clear();
        int col = 0;
        int row = 0;

        for (SanPham sp : products) {
            Node card = createProductCard(sp);
            productGrid.add(card, col, row);
            col++;
            if (col == 3) { // 3 cột
                col = 0;
                row++;
            }
        }
        lblProductCount.setText(products.size() + " sản phẩm");
    }

    /**
     * Tạo một thẻ VBox đại diện cho sản phẩm
     */
    private Node createProductCard(SanPham sp) {
        VBox card = new VBox(5);
        card.getStyleClass().add("product-card");

        StackPane imageContainer = new StackPane();
        // Lấy style từ .product-image của CSS
        imageContainer.getStyleClass().add("product-image");

        String imagePath = sp.getAnhSanPham();

        try {
            if (imagePath != null && !imagePath.isEmpty()) {

                ImageView imageView = new ImageView();
                // Tải ảnh từ resources
                Image img = new Image(getClass().getResourceAsStream(imagePath));
                imageView.setImage(img);
                imageView.setFitWidth(100);  // Kích thước từ CSS
                imageView.setFitHeight(80); // Kích thước từ CSS
                imageView.setPreserveRatio(true); // Giữ tỷ lệ

                imageContainer.getChildren().add(imageView);
            } else {
                throw new Exception("Image path is null or empty."); // Nhảy xuống catch
            }
        } catch (Exception e) {
            // Lỗi hoặc không có ảnh -> Dùng icon mặc định
            Label icon = new Label("☕");
            icon.setStyle("-fx-font-size: 40px; -fx-text-fill: #bdc3c7;");
            imageContainer.getChildren().add(icon);
        }

        Label name = new Label(sp.getTenSanPham());
        name.getStyleClass().add("product-name");
        name.setMinHeight(Region.USE_PREF_SIZE);

        Label price = new Label(CurrencyUtil.formatVND(sp.getGiaBan()));
        price.getStyleClass().add("product-price");

        Label stock = new Label("Kho: " + sp.getSoLuongTonKho());
        stock.getStyleClass().add("product-stock");

        if (sp.getSoLuongTonKho() <= 0) {
            stock.setText("Hết hàng");
            stock.getStyleClass().add("product-stock-out");
            card.getStyleClass().add("product-card-out-stock");
        } else {
            card.setOnMouseClicked(e -> {
                Optional<String> result = DialogUtils.showInputDialog(
                        "Nhập số lượng",
                        sp.getTenSanPham(),
                        "Vui lòng nhập số lượng:",
                        "1"
                );

                if (result.isPresent() && !result.get().isEmpty()) {
                    try {
                        int quantity = Integer.parseInt(result.get());
                        if (quantity <= 0) {
                            DialogUtils.showError("Số lượng không hợp lệ", "Số lượng phải lớn hơn 0.");
                            return;
                        }
                        if (quantity > sp.getSoLuongTonKho()) {
                            DialogUtils.showWarning("Không đủ hàng",
                                    "Số lượng tồn kho chỉ còn: " + sp.getSoLuongTonKho());
                            return;
                        }
                        addProductToInvoice(sp, quantity);
                    } catch (NumberFormatException nfe) {
                        DialogUtils.showError("Lỗi", "Vui lòng nhập một số hợp lệ.");
                    }
                }
            });
        }
        card.getChildren().addAll(imageContainer, name, price, stock);
        return card;
    }

    /**
     * Xử lý nút xóa tìm kiếm
     */
    @FXML
    void handleClearSearch(ActionEvent event) {
        txtSearchProduct.clear();
        cboCategory.getSelectionModel().selectFirst();
    }

    // ==================== INVOICE LOGIC ====================

    /**
     * Thêm sản phẩm vào hóa đơn (với số lượng cụ thể)
     * (ĐÃ SỬA: Logic cộng dồn số lượng)
     */
    private void addProductToInvoice(SanPham sp, int quantityToAdd) {
        if (currentInvoice == null) {
            DialogUtils.showError("Lỗi", "Chưa tạo hóa đơn. Vui lòng nhấn 'Hóa đơn mới'.");
            return;
        }

        // Kiểm tra xem sản phẩm đã có trong hóa đơn chưa
        Optional<ChiTietHoaDon> existingItem = currentInvoiceItems.stream()
                .filter(item -> item.getIdSanPham() == sp.getIdSanPham())
                .findFirst();

        if (existingItem.isPresent()) {
            // Đã có, CỘNG DỒN số lượng
            ChiTietHoaDon item = existingItem.get();
            int newQuantity = item.getSoLuong() + quantityToAdd;

            // Kiểm tra tồn kho
            int currentStock = getProductStock(sp.getIdSanPham());
            if (newQuantity > currentStock) {
                DialogUtils.showWarning("Không đủ hàng",
                        "Bạn muốn thêm " + quantityToAdd + " (đã có " + item.getSoLuong() + ").\n" +
                                "Tổng số lượng (" + newQuantity + ") vượt quá tồn kho (" + currentStock + ").");
            } else {
                item.setSoLuong(newQuantity);
            }
        } else {
            // Chưa có, thêm mới với số lượng đã nhập
            ChiTietHoaDon newItem = new ChiTietHoaDon(
                    currentInvoice.getIdHoaDon(),
                    sp.getIdSanPham(),
                    sp.getTenSanPham(),
                    quantityToAdd, // Dùng số lượng từ dialog
                    sp.getGiaBan()
            );
            currentInvoiceItems.add(newItem);
        }

        // Cập nhật lại UI
        tableInvoiceItems.refresh();
        updateSummary();
    }

    /**
     * Cập nhật tổng tiền, số lượng
     */
    private void updateSummary() {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (ChiTietHoaDon item : currentInvoiceItems) {
            total = total.add(item.getThanhTien());
            count++;
        }

        lblGrandTotal.setText(CurrencyUtil.formatVND(total));
        lblItemCount.setText(count + " món");

        // Cập nhật đối tượng Hóa đơn trong bộ nhớ
        if (currentInvoice != null) {
            currentInvoice.setTongTien(total);
        }

        // Chỉ cho phép thanh toán khi có tiền
        btnPayment.setDisable(total.compareTo(BigDecimal.ZERO) <= 0);
    }

    /**
     * Bắt đầu một hóa đơn mới
     */
    @FXML
    void handleNewInvoice(ActionEvent event) {
        if (currentInvoiceItems.size() > 0 && currentInvoice.getIdHoaDon() == 0) {
            checkAndSavePending(this::clearInvoiceUI);
        } else {
            // HĐ rỗng, hoặc là HĐ đang sửa (ID > 0), cứ clear
            clearInvoiceUI();
        }
    }

    /**
     * Xóa các sản phẩm trong hóa đơn (chỉ trên UI)
     */
    @FXML
    void handleClearInvoice(ActionEvent event) {
        if (currentInvoiceItems.isEmpty()) return;

        if (DialogUtils.showYesNoConfirmation("Xác nhận", "Bạn có chắc muốn xóa tất cả sản phẩm khỏi hóa đơn này?")) {
            currentInvoiceItems.clear();
            updateSummary();
        }
    }

    /**
     * Xóa sạch giao diện hóa đơn
     */
    private void clearInvoiceUI() {
        // Tạo HĐ mới trong bộ nhớ
        currentInvoice = new HoaDon(currentUser.getIdNhanVien(), "");

        currentInvoiceItems.clear();
        tableInvoiceItems.refresh();
        updateSummary();

        lblInvoiceCode.setText("HĐ MỚI");
        lblInvoiceDate.setText(DateTimeUtil.formatDateTime(currentInvoice.getNgayTao()));
        txtNote.clear();
        txtCustomerName.clear(); // Xóa tên khách

        // Vô hiệu hóa các nút Hủy/Tải/Xóa
        btnCancelInvoice.setDisable(true);
        btnLoadPending.setDisable(true);
        btnDeletePending.setDisable(true);
        listPendingInvoices.getSelectionModel().clearSelection();
    }


    // ==================== PAYMENT & PENDING LOGIC ====================

    /**
     * Xử lý thanh toán (Lưu HĐ là PAID)
     */
    @FXML
    void handlePayment(ActionEvent event) {
        if (!validateInvoice()) return;

        boolean confirm = DialogUtils.showYesNoConfirmation("Xác nhận thanh toán",
                "Tổng tiền thanh toán: " + CurrencyUtil.formatVND(currentInvoice.getTongTien()) + "\n\nXác nhận thanh toán hóa đơn này?");
        if (!confirm) return;

        // Lưu ghi chú (và tên khách nếu có)
        String ghiChu = txtNote.getText().trim();
        String customer = txtCustomerName.getText().trim();
        if(!customer.isEmpty()) {
            ghiChu = "Khách: " + customer + ". " + ghiChu;
        }
        currentInvoice.setGhiChu(ghiChu);

        new Thread(() -> {
            boolean success = false;

            // Nếu HĐ là HĐ mới (ID = 0)
            if (currentInvoice.getIdHoaDon() == 0) {
                // 1. Insert Hóa đơn
                int newId = hoaDonDAO.insert(currentInvoice);
                if (newId > 0) {
                    currentInvoice.setIdHoaDon(newId);
                    // 2. Cập nhật ID cho chi tiết
                    for(ChiTietHoaDon ct : currentInvoiceItems) {
                        ct.setIdHoaDon(newId);
                    }
                    // 3. Insert chi tiết
                    chiTietHoaDonDAO.insertBatch(currentInvoiceItems);
                }
            }

            // 4. Đánh dấu là đã thanh toán
            success = hoaDonDAO.completePayment(currentInvoice.getIdHoaDon());

            boolean finalSuccess = success;

            // 5. Cập nhật UI
            Platform.runLater(() -> {
                if (finalSuccess) {
                    DialogUtils.showSuccess("Thanh toán thành công!");
                    clearInvoiceUI();         // Bắt đầu HĐ mới
                    refreshPendingAndStats(); // Tải lại HĐ tạm và thống kê
                } else {
                    DialogUtils.showError("Lỗi", "Không thể hoàn tất thanh toán.");
                }
            });
        }).start();
    }

    /**
     * Xử lý nút "Mở Hóa đơn"
     */
    @FXML
    void handleLoadPending(ActionEvent event) {
        HoaDon selectedHoaDon = listPendingInvoices.getSelectionModel().getSelectedItem();
        if (selectedHoaDon == null) return;

        if (currentInvoiceItems.size() > 0 && currentInvoice.getIdHoaDon() == 0) {
            checkAndSavePending(() -> loadPendingInvoice(selectedHoaDon));
        } else {
            // HĐ rỗng, cứ tải HĐ mới
            loadPendingInvoice(selectedHoaDon);
        }
    }

    /**
     * Tải chi tiết của một Hóa đơn tạm
     */
    private void loadPendingInvoice(HoaDon hoaDon) {
        new Thread(() -> {
            List<ChiTietHoaDon> items = chiTietHoaDonDAO.getChiTietByHoaDon(hoaDon.getIdHoaDon());

            Platform.runLater(() -> {
                currentInvoice = hoaDon; // Đặt HĐ tạm làm HĐ hiện tại
                currentInvoiceItems.setAll(items);
                tableInvoiceItems.refresh();
                updateSummary();

                lblInvoiceCode.setText("HĐ #" + hoaDon.getIdHoaDon());
                lblInvoiceDate.setText(DateTimeUtil.formatDateTime(hoaDon.getNgayTao()));
                txtNote.setText(hoaDon.getGhiChu());
                // (Không set tên khách vì nó nằm trong ghi chú)

                // Kích hoạt các nút
                btnCancelInvoice.setDisable(false);
            });
        }).start();
    }

    /**
     * ✅ HÀM MỚI: KIỂM TRA VÀ LƯU HÓA ĐƠN TẠM
     * Helper: Kiểm tra HĐ hiện tại (nếu là HĐ mới) và hỏi lưu tạm.
     * Chạy bất đồng bộ.
     * @param onComplete Tác vụ (Runnable) sẽ được gọi trên FX Thread sau khi hoàn tất.
     */
    private void checkAndSavePending(Runnable onComplete) {
        // 1. Nếu HĐ không có gì (rỗng), chỉ cần chạy callback (vd: clearUI)
        if (!validateInvoice()) {
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
            return;
        }

        // 2. Nếu HĐ là HĐ đã lưu (ID > 0), không cần làm gì, chỉ chạy callback
        if (currentInvoice.getIdHoaDon() != 0) {
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
            return;
        }

        // 3. HĐ có đồ và chưa lưu (ID == 0) -> HỎI
        boolean confirm = DialogUtils.showYesNoConfirmation(
                "Lưu hóa đơn tạm?",
                "Hóa đơn hiện tại chưa được lưu. Bạn có muốn LƯU TẠM hóa đơn này?"
        );

        if (!confirm) {
            // Người dùng chọn KHÔNG LƯU. Vẫn chạy callback (để clearUI)
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
            return;
        }

        // 4. Bắt đầu LƯU TẠM (Bất đồng bộ)
        String ghiChu = txtNote.getText().trim();
        String customer = txtCustomerName.getText().trim();
        if(!customer.isEmpty()) {
            ghiChu = "Khách: " + customer + ". " + ghiChu;
        }
        currentInvoice.setGhiChu(ghiChu);

        new Thread(() -> {
            boolean success = false;
            int newId = hoaDonDAO.insert(currentInvoice); // Insert PENDING
            if (newId > 0) {
                currentInvoice.setIdHoaDon(newId);
                for(ChiTietHoaDon ct : currentInvoiceItems) {
                    ct.setIdHoaDon(newId);
                }
                success = chiTietHoaDonDAO.insertBatch(currentInvoiceItems);
            }

            boolean finalSuccess = success;
            Platform.runLater(() -> {
                if (finalSuccess) {
                    DialogUtils.showInfo("Đã lưu tạm", "Hóa đơn cũ đã được lưu vào danh sách chờ.");
                    refreshPendingAndStats(); // Luôn tải lại stats
                    if (onComplete != null) {
                        onComplete.run(); // Chạy callback (ví dụ: clearUI hoặc load HĐ khác)
                    }
                } else {
                    DialogUtils.showError("Lỗi", "Không thể lưu hóa đơn tạm. Vui lòng thử lại.");
                    // KHÔNG chạy callback nếu lưu lỗi
                }
            });
        }).start();
    }

    /**
     * Hủy hóa đơn (chuyển PENDING -> CANCELLED)
     */
    @FXML
    void handleCancelInvoice(ActionEvent event) {
        if (currentInvoice == null || currentInvoice.getIdHoaDon() == 0) {
            DialogUtils.showError("Lỗi", "Đây là hóa đơn mới, không thể hủy.");
            return;
        }

        boolean confirm = DialogUtils.showYesNoConfirmation("Xác nhận HỦY",
                "Bạn có chắc muốn HỦY Hóa đơn #" + currentInvoice.getIdHoaDon() + "?\n(Sản phẩm sẽ được hoàn lại kho)");
        if (!confirm) return;

        new Thread(() -> {
            // DB Trigger sẽ tự động hoàn kho
            boolean success = hoaDonDAO.cancelInvoice(currentInvoice.getIdHoaDon());


            Platform.runLater(() -> {
                if (success) {
                    DialogUtils.showSuccess("Đã hủy hóa đơn!");
                    clearInvoiceUI();     // Bắt đầu HĐ mới
                    refreshPendingAndStats();    // Tải lại
                } else {
                    DialogUtils.showError("Lỗi", "Không thể hủy hóa đơn.");
                }
            });
        }).start();
    }

    /**
     * Xóa vĩnh viễn một hóa đơn PENDING
     */
    @FXML
    void handleDeletePending(ActionEvent event) {
        HoaDon selectedHoaDon = listPendingInvoices.getSelectionModel().getSelectedItem();
        if (selectedHoaDon == null) {
            DialogUtils.showWarning("Chưa chọn", "Vui lòng chọn một hóa đơn tạm để xóa.");
            return;
        }

        boolean confirm = DialogUtils.showYesNoConfirmation("Xác nhận XÓA",
                "Xóa vĩnh viễn HĐ #" + selectedHoaDon.getIdHoaDon() + "?\n(Hành động này không thể hoàn tác, sản phẩm sẽ được hoàn kho)");
        if (!confirm) return;

        new Thread(() -> {
            // DB Trigger (ON DELETE CASCADE) sẽ xóa ChiTietHoaDon
            // và Trigger (AFTER DELETE CTHD) sẽ hoàn kho
            boolean success = hoaDonDAO.delete(selectedHoaDon.getIdHoaDon());

            Platform.runLater(() -> {
                if (success) {
                    DialogUtils.showSuccess("Đã xóa hóa đơn tạm!");
                    refreshPendingAndStats(); // Tải lại danh sách
                    // Nếu HĐ bị xóa đang được mở, dọn dẹp UI
                    if (currentInvoice != null && currentInvoice.getIdHoaDon() == selectedHoaDon.getIdHoaDon()) {
                        clearInvoiceUI();
                    }
                } else {
                    DialogUtils.showError("Lỗi", "Không thể xóa hóa đơn.");
                }
            });
        }).start();
    }

    /**
     * Xử lý nút refresh HĐ tạm
     */
    @FXML
    void handleRefreshPending(ActionEvent event) {
        refreshPendingAndStats();
        DialogUtils.showInfo("Làm mới", "Đã cập nhật danh sách hóa đơn tạm và thống kê.");
    }

    // ==================== HELPER METHODS & CLASSES ====================

    /**
     * Lấy tồn kho hiện tại của sản phẩm
     */
    private int getProductStock(int sanPhamId) {
        return masterProductList.stream()
                .filter(sp -> sp.getIdSanPham() == sanPhamId)
                .map(SanPham::getSoLuongTonKho)
                .findFirst()
                .orElse(0);
    }

    /**
     * Kiểm tra hóa đơn hợp lệ trước khi lưu
     */
    private boolean validateInvoice() {
        if (currentInvoiceItems.isEmpty()) {
            DialogUtils.showWarning("Hóa đơn trống", "Vui lòng thêm sản phẩm vào hóa đơn.");
            return false;
        }
        return true;
    }

    /**
     * Tạo một VBox placeholder chung
     */
    private Node createPlaceholder(String text) {
        VBox placeholder = new VBox(10);
        placeholder.setAlignment(Pos.CENTER);

        String[] parts = text.split("\n");
        Label icon = new Label(parts[0]);
        icon.getStyleClass().add("empty-icon-small");
        Label title = new Label(parts[1]);
        title.getStyleClass().add("empty-title");
        placeholder.getChildren().addAll(icon, title);

        if(parts.length > 2) {
            Label subtitle = new Label(parts[2]);
            subtitle.getStyleClass().add("empty-subtitle");
            placeholder.getChildren().add(subtitle);
        }

        return placeholder;
    }

    /**
     * Class nội bộ cho ô điều khiển Số lượng trong TableView
     */
    private class QuantityCell extends TableCell<ChiTietHoaDon, Integer> {
        private final HBox container = new HBox(5);
        private final Button btnDec = new Button("−");
        private final Button btnInc = new Button("+");
        private final Button btnDel = new Button("x");
        private final Label lblQuantity = new Label();

        public QuantityCell() {
            // (Bạn có thể thêm style cho các nút này trong CSS)
            String btnStyle = "-fx-font-weight: bold; -fx-pref-width: 25; -fx-cursor: hand; -fx-background-color: #eee; -fx-border-color: #ccc; -fx-border-radius: 4; -fx-background-radius: 4;";
            btnDec.setStyle(btnStyle);
            btnInc.setStyle(btnStyle);
            btnDel.setStyle(btnStyle + "-fx-text-fill: red;");

            lblQuantity.setMinWidth(25);
            lblQuantity.setAlignment(Pos.CENTER);
            lblQuantity.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            container.setAlignment(Pos.CENTER);
            container.getChildren().addAll(btnDec, lblQuantity, btnInc, btnDel);

            // Giảm số lượng
            btnDec.setOnAction(e -> {
                ChiTietHoaDon item = getTableView().getItems().get(getIndex());
                if (item.getSoLuong() > 1) {
                    item.setSoLuong(item.getSoLuong() - 1);
                } else {
                    // Nếu giảm về 0 -> Xóa
                    currentInvoiceItems.remove(item);
                }
                tableInvoiceItems.refresh();
                updateSummary();
            });

            // Tăng số lượng
            btnInc.setOnAction(e -> {
                ChiTietHoaDon item = getTableView().getItems().get(getIndex());
                int currentStock = getProductStock(item.getIdSanPham());
                if (item.getSoLuong() < currentStock) {
                    item.setSoLuong(item.getSoLuong() + 1);
                } else {
                    DialogUtils.showWarning("Hết hàng", "Không đủ số lượng tồn kho.");
                }
                tableInvoiceItems.refresh();
                updateSummary();
            });

            // Xóa item
            btnDel.setOnAction(e -> {
                currentInvoiceItems.remove(getTableView().getItems().get(getIndex()));
                updateSummary();
            });
        }

        @Override
        protected void updateItem(Integer quantity, boolean empty) {
            super.updateItem(quantity, empty);
            if (empty || quantity == null) {
                setGraphic(null);
            } else {
                lblQuantity.setText(String.valueOf(quantity));
                setGraphic(container);
            }
        }
    }

    /**
     * Class nội bộ cho ô hiển thị Hóa đơn tạm
     */
    private class PendingInvoiceCell extends ListCell<HoaDon> {
        private final VBox card = new VBox(5);
        private final HBox header = new HBox(10);
        private final Label lblId = new Label();
        private final Label lblTime = new Label();
        private final Label lblNote = new Label();
        private final Label lblTotal = new Label();

        public PendingInvoiceCell() {
            card.getStyleClass().add("pending-card");
            lblId.getStyleClass().add("pending-id");
            lblTime.getStyleClass().add("pending-time");
            lblNote.getStyleClass().add("pending-items");
            lblTotal.getStyleClass().add("pending-total");

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            header.getChildren().addAll(lblId, lblTime, spacer, lblTotal);
            card.getChildren().addAll(header, lblNote);
        }

        @Override
        protected void updateItem(HoaDon hoaDon, boolean empty) {
            super.updateItem(hoaDon, empty);
            if (empty || hoaDon == null) {
                setGraphic(null);
            } else {
                lblId.setText("HĐ #" + hoaDon.getIdHoaDon());
                lblTime.setText(DateTimeUtil.getRelativeTime(hoaDon.getNgayTao()));
                String ghiChu = hoaDon.getGhiChu();
                lblNote.setText("Ghi chú: " + (ghiChu == null || ghiChu.isEmpty() ? "Không có" : ghiChu));
                lblTotal.setText(CurrencyUtil.formatVND(hoaDon.getTongTien()));
                setGraphic(card);
            }
        }
    }
}