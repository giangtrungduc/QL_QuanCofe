package com.example.qlquancoffe.models;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model cho bảng hoadon
 */
public class HoaDon {

    // ==================== ENUM ====================

    public enum TrangThai {
        PENDING("Chưa thanh toán"),
        PAID("Đã thanh toán"),
        CANCELLED("Đã hủy");

        private final String displayName;

        TrangThai(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // ==================== PROPERTIES ====================

    private final IntegerProperty idHoaDon;
    private final IntegerProperty idNhanVien;
    private final ObjectProperty<LocalDateTime> ngayTao;
    private final ObjectProperty<BigDecimal> tongTien;
    private final ObjectProperty<TrangThai> trangThai;
    private final StringProperty ghiChu;
    private final ObjectProperty<LocalDateTime> ngayThanhToan;
    private final ObjectProperty<LocalDateTime> ngayCapNhat;

    // Property phụ (để hiển thị)
    private final StringProperty tenNhanVien;

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructor đầy đủ
     */
    public HoaDon(int idHoaDon, int idNhanVien, LocalDateTime ngayTao,
                  BigDecimal tongTien, TrangThai trangThai,
                  String ghiChu, LocalDateTime ngayThanhToan,
                  LocalDateTime ngayCapNhat) {
        this.idHoaDon = new SimpleIntegerProperty(idHoaDon);
        this.idNhanVien = new SimpleIntegerProperty(idNhanVien);
        this.ngayTao = new SimpleObjectProperty<>(ngayTao);
        this.tongTien = new SimpleObjectProperty<>(tongTien);
        this.trangThai = new SimpleObjectProperty<>(trangThai);
        this.ghiChu = new SimpleStringProperty(ghiChu);
        this.ngayThanhToan = new SimpleObjectProperty<>(ngayThanhToan);
        this.ngayCapNhat = new SimpleObjectProperty<>(ngayCapNhat);
        this.tenNhanVien = new SimpleStringProperty("");
    }

    /**
     * Constructor đơn giản (tạo hóa đơn mới)
     */
    public HoaDon(int idNhanVien, String ghiChu) {
        this(0, idNhanVien, LocalDateTime.now(), BigDecimal.ZERO,
                TrangThai.PENDING, ghiChu, null, null);
    }

    /**
     * Constructor mặc định
     */
    public HoaDon() {
        this(0, "");
    }

    // ==================== GETTERS & SETTERS ====================

    // ID Hóa đơn
    public int getIdHoaDon() {
        return idHoaDon.get();
    }

    public void setIdHoaDon(int idHoaDon) {
        this.idHoaDon.set(idHoaDon);
    }

    public IntegerProperty idHoaDonProperty() {
        return idHoaDon;
    }

    // ID Nhân viên
    public int getIdNhanVien() {
        return idNhanVien.get();
    }

    public void setIdNhanVien(int idNhanVien) {
        this.idNhanVien.set(idNhanVien);
    }

    public IntegerProperty idNhanVienProperty() {
        return idNhanVien;
    }

    // Ngày tạo
    public LocalDateTime getNgayTao() {
        return ngayTao.get();
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao.set(ngayTao);
    }

    public ObjectProperty<LocalDateTime> ngayTaoProperty() {
        return ngayTao;
    }

    // Tổng tiền
    public BigDecimal getTongTien() {
        return tongTien.get();
    }
    public long getTongTienn(){
        return tongTien.get().longValue();
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien.set(tongTien);
    }

    public ObjectProperty<BigDecimal> tongTienProperty() {
        return tongTien;
    }

    // Trạng thái
    public TrangThai getTrangThai() {
        return trangThai.get();
    }

    public void setTrangThai(TrangThai trangThai) {
        this.trangThai.set(trangThai);
    }

    public ObjectProperty<TrangThai> trangThaiProperty() {
        return trangThai;
    }

    // Ghi chú
    public String getGhiChu() {
        return ghiChu.get();
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu.set(ghiChu);
    }

    public StringProperty ghiChuProperty() {
        return ghiChu;
    }

    // Ngày thanh toán
    public LocalDateTime getNgayThanhToan() {
        return ngayThanhToan.get();
    }

    public void setNgayThanhToan(LocalDateTime ngayThanhToan) {
        this.ngayThanhToan.set(ngayThanhToan);
    }

    public ObjectProperty<LocalDateTime> ngayThanhToanProperty() {
        return ngayThanhToan;
    }

    // Ngày cập nhật
    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat.get();
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat.set(ngayCapNhat);
    }

    public ObjectProperty<LocalDateTime> ngayCapNhatProperty() {
        return ngayCapNhat;
    }

    // Tên nhân viên (phụ)
    public String getTenNhanVien() {
        return tenNhanVien.get();
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien.set(tenNhanVien);
    }

    public StringProperty tenNhanVienProperty() {
        return tenNhanVien;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Kiểm tra hóa đơn có thể chỉnh sửa không
     */
    public boolean canEdit() {
        return trangThai.get() == TrangThai.PENDING;
    }

    /**
     * Kiểm tra hóa đơn có thể hủy không
     */
    public boolean canCancel() {
        return trangThai.get() == TrangThai.PENDING;
    }

    /**
     * Kiểm tra hóa đơn có thể thanh toán không
     */
    public boolean canPay() {
        return trangThai.get() == TrangThai.PENDING &&
                tongTien.get() != null &&
                tongTien.get().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return String.format("HĐ #%d - %s - %s",
                idHoaDon.get(),
                trangThai.get().getDisplayName(),
                tongTien.get() != null ? tongTien.get().toString() + " ₫" : "0 ₫"
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HoaDon other = (HoaDon) obj;
        // Chỉ so sánh khi id > 0 (đã có trong DB)
        if (this.idHoaDon.get() == 0 || other.idHoaDon.get() == 0) {
            return false;
        }
        return idHoaDon.get() == other.idHoaDon.get();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idHoaDon.get());
    }
}