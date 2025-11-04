package com.example.qlquancoffe.controllers.nhanvien;

import com.example.qlquancoffe.dao.ChiTietHoaDonDAO;
import com.example.qlquancoffe.dao.HoaDonDAO;
import com.example.qlquancoffe.models.ChiTietHoaDon;
import com.example.qlquancoffe.models.HoaDon;
import com.example.qlquancoffe.models.TaiKhoan;
import com.example.qlquancoffe.utils.CurrencyUtil;
import com.example.qlquancoffe.utils.DateTimeUtil;
import com.example.qlquancoffe.utils.SceneSwitcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime; // Cần cho cột colNgayTao
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller Lịch sử bán hàng
 * (HIỂN THỊ CẢ PAID VÀ CANCELLED)
 */
public class LichSuBanController {

    @FXML private BorderPane rootPane;
    @FXML private DatePicker datePicker;
    @FXML private Label lblEmployeeName;
    @FXML private Label lblChiTietMaHD;
    @FXML private Label lblTongDoanhThu;
    @FXML private Label lblTongDonPaid;
    @FXML private Label lblTongDonCancelled;
    @FXML private TableView<HoaDon> tableHoaDon;
    @FXML private TableColumn<HoaDon, Integer> colMaHD;
    @FXML private TableColumn<HoaDon, LocalDateTime> colNgayTao; // Sửa kiểu
    @FXML private TableColumn<HoaDon, HoaDon.TrangThai> colTrangThai; // Sửa kiểu
    @FXML private TableColumn<HoaDon, BigDecimal> colTongTien;
    @FXML private TableView<ChiTietHoaDon> tableChiTiet;
    @FXML private TableColumn<ChiTietHoaDon, String> colTenSP;
    @FXML private TableColumn<ChiTietHoaDon, Integer> colSoLuong;
    @FXML private TableColumn<ChiTietHoaDon, BigDecimal> colDonGia;
    @FXML private TableColumn<ChiTietHoaDon, BigDecimal> colThanhTien;

    private TaiKhoan currentUser;
    private HoaDonDAO hoaDonDAO;
    private ChiTietHoaDonDAO chiTietHoaDonDAO;

    private ObservableList<HoaDon> dsHoaDon = FXCollections.observableArrayList();
    private ObservableList<ChiTietHoaDon> dsChiTiet = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        currentUser = SceneSwitcher.getCurrentUser();
        if (currentUser == null) { return; }

        hoaDonDAO = new HoaDonDAO();
        chiTietHoaDonDAO = new ChiTietHoaDonDAO();

        lblEmployeeName.setText(currentUser.getHoTen());

        setupTables();

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadHoaDon(newVal);
            }
        });

        handleFilterToday(null);
    }

    /**
     * Cài đặt các cột cho cả 2 TableView
     */
    private void setupTables() {
        // === Bảng Hóa Đơn ===
        tableHoaDon.setItems(dsHoaDon);
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("idHoaDon"));
        colNgayTao.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTien"));

        // Format cột Thời gian (dùng ngayTao)
        colNgayTao.setCellFactory(col -> new TableCell<HoaDon, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : DateTimeUtil.formatDateTime(item));
            }
        });

        // ✅ SỬA: Format cột Trạng thái (hiển thị 2 màu)
        colTrangThai.setCellFactory(col -> new TableCell<HoaDon, HoaDon.TrangThai>() {
            @Override
            protected void updateItem(HoaDon.TrangThai item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getDisplayName());
                    if (item == HoaDon.TrangThai.PAID) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (item == HoaDon.TrangThai.CANCELLED) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Format cột Tổng tiền
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
        tableChiTiet.setItems(dsChiTiet);
        colTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));

        // (Format các cột tiền và số lượng của Bảng Chi Tiết)
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
     * Tải Hóa đơn (PAID và CANCELLED) theo ngày
     */
    private void loadHoaDon(LocalDate selectedDate) {
        dsHoaDon.clear();
        dsChiTiet.clear();
        lblChiTietMaHD.setText("#---");

        new Thread(() -> {
            // Lấy TẤT CẢ hóa đơn của nhân viên
            List<HoaDon> allInvoices = hoaDonDAO.getHoaDonByNhanVien(currentUser.getIdNhanVien());

            // ✅ SỬA: Lọc PAID và CANCELLED theo NGÀY TẠO (ngayTao)
            List<HoaDon> filteredList = allInvoices.stream()
                    .filter(h -> (h.getTrangThai() == HoaDon.TrangThai.PAID || h.getTrangThai() == HoaDon.TrangThai.CANCELLED) &&
                            h.getNgayTao().toLocalDate().equals(selectedDate)) // Lọc theo ngày tạo
                    .collect(Collectors.toList());

            // ✅ SỬA: Tính toán lại thống kê
            BigDecimal totalRevenue = filteredList.stream()
                    .filter(h -> h.getTrangThai() == HoaDon.TrangThai.PAID)
                    .map(HoaDon::getTongTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long paidCount = filteredList.stream()
                    .filter(h -> h.getTrangThai() == HoaDon.TrangThai.PAID)
                    .count();

            long cancelledCount = filteredList.stream()
                    .filter(h -> h.getTrangThai() == HoaDon.TrangThai.CANCELLED)
                    .count();

            // Cập nhật UI
            Platform.runLater(() -> {
                dsHoaDon.setAll(filteredList);
                lblTongDoanhThu.setText(CurrencyUtil.formatVND(totalRevenue));
                lblTongDonPaid.setText(String.valueOf(paidCount));
                lblTongDonCancelled.setText(String.valueOf(cancelledCount));
            });
        }).start();
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
}