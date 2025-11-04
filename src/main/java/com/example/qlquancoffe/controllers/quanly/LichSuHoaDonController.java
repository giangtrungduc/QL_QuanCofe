package com.example.qlquancoffe.controllers.quanly;

import com.example.qlquancoffe.dao.ChiTietHoaDonDAO;
import com.example.qlquancoffe.dao.HoaDonDAO;
import com.example.qlquancoffe.dao.TaiKhoanDAO;
import com.example.qlquancoffe.models.ChiTietHoaDon;
import com.example.qlquancoffe.models.HoaDon;
import com.example.qlquancoffe.models.TaiKhoan;
import com.example.qlquancoffe.utils.CurrencyUtil;
import com.example.qlquancoffe.utils.DateTimeUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class LichSuHoaDonController implements Initializable {

    // === FXML Fields ===
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<TaiKhoan> cboNhanVien;
    @FXML private ComboBox<String> cboTrangThai; // Dùng String để có "Tất cả"

    @FXML private Label lblChiTietMaHD;
    @FXML private Label lblTongDoanhThu;
    @FXML private Label lblTongDonPaid;
    @FXML private Label lblTongDonPending;
    @FXML private Label lblTongDonCancelled;

    @FXML private TableView<HoaDon> tableHoaDon;
    @FXML private TableColumn<HoaDon, Integer> colMaHD;
    @FXML private TableColumn<HoaDon, String> colNhanVien;
    @FXML private TableColumn<HoaDon, LocalDateTime> colNgayTao;
    @FXML private TableColumn<HoaDon, HoaDon.TrangThai> colTrangThai;
    @FXML private TableColumn<HoaDon, BigDecimal> colTongTien;

    @FXML private TableView<ChiTietHoaDon> tableChiTiet;
    @FXML private TableColumn<ChiTietHoaDon, String> colTenSP;
    @FXML private TableColumn<ChiTietHoaDon, Integer> colSoLuong;
    @FXML private TableColumn<ChiTietHoaDon, BigDecimal> colDonGia;
    @FXML private TableColumn<ChiTietHoaDon, BigDecimal> colThanhTien;

    // === DAOs ===
    private HoaDonDAO hoaDonDAO;
    private ChiTietHoaDonDAO chiTietHoaDonDAO;
    private TaiKhoanDAO taiKhoanDAO;

    // === Data Lists ===
    private ObservableList<HoaDon> masterHoaDonList = FXCollections.observableArrayList();
    private FilteredList<HoaDon> filteredHoaDonList;
    private ObservableList<ChiTietHoaDon> dsChiTiet = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hoaDonDAO = new HoaDonDAO();
        chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        taiKhoanDAO = new TaiKhoanDAO();

        setupTables();
        setupFilters();

        loadData();
    }

    /**
     * Tải dữ liệu ban đầu (Tất cả hóa đơn, tất cả nhân viên)
     */
    private void loadData() {
        new Thread(() -> {
            // Tải danh sách HĐ (chỉ 1 lần)
            masterHoaDonList.setAll(hoaDonDAO.getAllHoaDon_Admin());

            // Tải danh sách NV (chỉ 1 lần)
            ObservableList<TaiKhoan> nhanVienList = taiKhoanDAO.getAll();

            Platform.runLater(() -> {
                // Setup ComboBox Nhân viên
                cboNhanVien.getItems().add(null); // "Tất cả nhân viên"
                cboNhanVien.getItems().addAll(nhanVienList);
                cboNhanVien.getSelectionModel().selectFirst(); // Chọn "Tất cả"

                // Setup FilteredList
                filteredHoaDonList = new FilteredList<>(masterHoaDonList, p -> true);
                tableHoaDon.setItems(filteredHoaDonList);

                // Áp dụng bộ lọc lần đầu
                handleFilterToday(null);
            });
        }).start();
    }

    /**
     * Cài đặt các cột TableView
     */
    private void setupTables() {
        // === Bảng Hóa Đơn ===
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("idHoaDon"));
        colNhanVien.setCellValueFactory(new PropertyValueFactory<>("tenNhanVien"));
        colNgayTao.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTien"));

        // (Format các cột tương tự LichSuBanController)
        colNgayTao.setCellFactory(col -> new TableCell<HoaDon, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : DateTimeUtil.formatDateTime(item));
            }
        });

        colTrangThai.setCellFactory(col -> new TableCell<HoaDon, HoaDon.TrangThai>() {
            @Override
            protected void updateItem(HoaDon.TrangThai item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item.getDisplayName());
                    String color = switch (item) {
                        case PAID -> "#27ae60";
                        case CANCELLED -> "#e74c3c";
                        case PENDING -> "#f39c12";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colTongTien.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : CurrencyUtil.formatVND(item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        tableHoaDon.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            loadChiTiet(newVal);
        });

        // === Bảng Chi Tiết ===
        // (Giống hệt LichSuBanController)
        tableChiTiet.setItems(dsChiTiet);
        colTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));

        colDonGia.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : CurrencyUtil.formatNumber(item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        colThanhTien.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : CurrencyUtil.formatVND(item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        colSoLuong.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Cài đặt các bộ lọc
     */
    private void setupFilters() {
        // ComboBox Trạng thái
        cboTrangThai.getItems().add("Tất cả trạng thái");
        cboTrangThai.getItems().add(HoaDon.TrangThai.PAID.name());
        cboTrangThai.getItems().add(HoaDon.TrangThai.PENDING.name());
        cboTrangThai.getItems().add(HoaDon.TrangThai.CANCELLED.name());
        cboTrangThai.getSelectionModel().selectFirst();

        // Thêm listener cho cả 3 bộ lọc
        datePicker.valueProperty().addListener((obs, o, n) -> applyFilters());
        cboNhanVien.valueProperty().addListener((obs, o, n) -> applyFilters());
        cboTrangThai.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    /**
     * Lọc danh sách HĐ dựa trên 3 ComboBox
     */
    private void applyFilters() {
        if (filteredHoaDonList == null) return;

        LocalDate date = datePicker.getValue();
        TaiKhoan nhanVien = cboNhanVien.getValue();
        String trangThaiStr = cboTrangThai.getValue();

        // 1. Lọc theo Ngày
        Predicate<HoaDon> dateFilter = h ->
                (date == null) || h.getNgayTao().toLocalDate().equals(date);

        // 2. Lọc theo Nhân viên
        Predicate<HoaDon> employeeFilter = h ->
                (nhanVien == null) || h.getIdNhanVien() == nhanVien.getIdNhanVien();

        // 3. Lọc theo Trạng thái
        Predicate<HoaDon> statusFilter = h ->
                (trangThaiStr == null || trangThaiStr.equals("Tất cả trạng thái")) ||
                        h.getTrangThai().name().equals(trangThaiStr);

        // Áp dụng
        filteredHoaDonList.setPredicate(dateFilter.and(employeeFilter).and(statusFilter));

        // Cập nhật thống kê
        updateSummary();
    }

    /**
     * Cập nhật thanh thống kê
     */
    private void updateSummary() {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long paidCount = 0;
        long pendingCount = 0;
        long cancelledCount = 0;

        // Tính toán dựa trên danh sách ĐÃ LỌC
        for (HoaDon h : filteredHoaDonList) {
            switch (h.getTrangThai()) {
                case PAID:
                    totalRevenue = totalRevenue.add(h.getTongTien());
                    paidCount++;
                    break;
                case PENDING:
                    pendingCount++;
                    break;
                case CANCELLED:
                    cancelledCount++;
                    break;
            }
        }

        lblTongDoanhThu.setText(CurrencyUtil.formatVND(totalRevenue));
        lblTongDonPaid.setText(String.valueOf(paidCount));
        lblTongDonPending.setText(String.valueOf(pendingCount));
        lblTongDonCancelled.setText(String.valueOf(cancelledCount));
    }

    /**
     * Tải chi tiết của hóa đơn đã chọn
     */
    private void loadChiTiet(HoaDon hoaDon) {
        dsChiTiet.clear();
        if (hoaDon == null) {
            lblChiTietMaHD.setText("#---");
            return;
        }

        lblChiTietMaHD.setText("#" + hoaDon.getIdHoaDon());
        new Thread(() -> {
            List<ChiTietHoaDon> items = chiTietHoaDonDAO.getChiTietByHoaDon(hoaDon.getIdHoaDon());
            Platform.runLater(() -> {
                dsChiTiet.setAll(items);
            });
        }).start();
    }

    @FXML
    void handleFilterToday(ActionEvent event) {
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    void handleFilterYesterday(ActionEvent event) {
        datePicker.setValue(LocalDate.now().minusDays(1));
    }

    @FXML
    void handleLamMoi(ActionEvent event) {
        cboNhanVien.getSelectionModel().selectFirst();
        cboTrangThai.getSelectionModel().selectFirst();
        datePicker.setValue(LocalDate.now());
        // (Listener sẽ tự động gọi applyFilters)
    }
}