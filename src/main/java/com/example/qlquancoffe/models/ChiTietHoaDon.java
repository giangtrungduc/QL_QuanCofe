package com.example.qlquancoffe.models;

import javafx.beans.property.*;

import java.math.BigDecimal;

/**
 * Model cho bảng chitiethoadon
 */
public class ChiTietHoaDon {

    private final IntegerProperty idChiTietHoaDon;
    private final IntegerProperty idHoaDon;
    private final IntegerProperty idSanPham;
    private final StringProperty tenSanPham;
    private final IntegerProperty soLuong;
    private final ObjectProperty<BigDecimal> donGia;
    private final ObjectProperty<BigDecimal> thanhTien;
    private final StringProperty ghiChu;

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructor đầy đủ (từ DB)
     */
    public ChiTietHoaDon(int idChiTietHoaDon, int idHoaDon, int idSanPham,
                         String tenSanPham, int soLuong, BigDecimal donGia,
                         BigDecimal thanhTien, String ghiChu) {
        this.idChiTietHoaDon = new SimpleIntegerProperty(idChiTietHoaDon);
        this.idHoaDon = new SimpleIntegerProperty(idHoaDon);
        this.idSanPham = new SimpleIntegerProperty(idSanPham);
        this.tenSanPham = new SimpleStringProperty(tenSanPham);
        this.soLuong = new SimpleIntegerProperty(soLuong);
        this.donGia = new SimpleObjectProperty<>(donGia);
        this.thanhTien = new SimpleObjectProperty<>(thanhTien);
        this.ghiChu = new SimpleStringProperty(ghiChu);
    }

    /**
     * Constructor tạo mới (chưa có ID)
     */
    public ChiTietHoaDon(int idHoaDon, int idSanPham, String tenSanPham,
                         int soLuong, BigDecimal donGia) {
        this(0, idHoaDon, idSanPham, tenSanPham, soLuong, donGia,
                donGia.multiply(BigDecimal.valueOf(soLuong)), "");
    }

    /**
     * Constructor tự tính thành tiền (legacy)
     */
    public ChiTietHoaDon(int idHoaDon, int idSanPham, int soLuong, BigDecimal donGia) {
        this(0, idHoaDon, idSanPham, "", soLuong, donGia,
                donGia.multiply(BigDecimal.valueOf(soLuong)), "");
    }

    /**
     * Constructor mặc định
     */
    public ChiTietHoaDon() {
        this(0, 0, 0, "", 0, BigDecimal.ZERO, BigDecimal.ZERO, "");
    }

    // ==================== GETTERS & SETTERS ====================

    // ID Chi tiết hóa đơn
    public int getIdChiTietHoaDon() {
        return idChiTietHoaDon.get();
    }

    public void setIdChiTietHoaDon(int idChiTietHoaDon) {
        this.idChiTietHoaDon.set(idChiTietHoaDon);
    }

    public IntegerProperty idChiTietHoaDonProperty() {
        return idChiTietHoaDon;
    }

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

    // ID Sản phẩm
    public int getIdSanPham() {
        return idSanPham.get();
    }

    public void setIdSanPham(int idSanPham) {
        this.idSanPham.set(idSanPham);
    }

    public IntegerProperty idSanPhamProperty() {
        return idSanPham;
    }

    // Tên sản phẩm
    public String getTenSanPham() {
        return tenSanPham.get();
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham.set(tenSanPham);
    }

    public StringProperty tenSanPhamProperty() {
        return tenSanPham;
    }

    // Số lượng
    public int getSoLuong() {
        return soLuong.get();
    }

    public void setSoLuong(int soLuong) {
        this.soLuong.set(soLuong);
        // Tự động tính lại thành tiền
        recalculateThanhTien();
    }

    public IntegerProperty soLuongProperty() {
        return soLuong;
    }

    // Đơn giá
    public BigDecimal getDonGia() {
        return donGia.get();
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia.set(donGia);
        // Tự động tính lại thành tiền
        recalculateThanhTien();
    }

    public ObjectProperty<BigDecimal> donGiaProperty() {
        return donGia;
    }

    // Thành tiền
    public BigDecimal getThanhTien() {
        return thanhTien.get();
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien.set(thanhTien);
    }

    public ObjectProperty<BigDecimal> thanhTienProperty() {
        return thanhTien;
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

    // ==================== HELPER METHODS ====================

    /**
     * Tính lại thành tiền
     */
    private void recalculateThanhTien() {
        if (donGia.get() != null && soLuong.get() > 0) {
            BigDecimal newThanhTien = donGia.get().multiply(BigDecimal.valueOf(soLuong.get()));
            setThanhTien(newThanhTien);
        }
    }

    /**
     * Tăng số lượng
     */
    public void increaseQuantity(int amount) {
        setSoLuong(getSoLuong() + amount);
    }

    /**
     * Giảm số lượng
     */
    public void decreaseQuantity(int amount) {
        int newQuantity = getSoLuong() - amount;
        if (newQuantity > 0) {
            setSoLuong(newQuantity);
        }
    }

    @Override
    public String toString() {
        return String.format("%s x%d = %s ₫",
                tenSanPham.get(),
                soLuong.get(),
                thanhTien.get()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiTietHoaDon other = (ChiTietHoaDon) obj;
        return idChiTietHoaDon.get() == other.idChiTietHoaDon.get();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idChiTietHoaDon.get());
    }
}