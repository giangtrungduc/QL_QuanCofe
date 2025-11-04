package com.example.qlquancoffe.models;

import javafx.beans.property.*;

import java.math.BigDecimal;

/**
 * Model cho bảng sanpham
 */
public class SanPham {

    // ==================== ENUM ====================

    public enum TrangThai {
        ConHang("Còn hàng"),
        HetHang("Hết hàng"),
        NgungKinhDoanh("Ngừng kinh doanh");

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

    private final IntegerProperty idSanPham;
    private final StringProperty tenSanPham;
    private final ObjectProperty<BigDecimal> giaBan;
    private final IntegerProperty soLuongTonKho;
    private final IntegerProperty idDanhMuc;
    private final ObjectProperty<TrangThai> trangThai;
    private final StringProperty anhSanPham;

    // Property phụ (để hiển thị)
    private final StringProperty tenDanhMuc;

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructor đầy đủ (từ DB)
     */
    public SanPham(int idSanPham, String tenSanPham, BigDecimal giaBan,
                   int soLuongTonKho, int idDanhMuc, TrangThai trangThai, String anhSanPham) {
        this.idSanPham = new SimpleIntegerProperty(idSanPham);
        this.tenSanPham = new SimpleStringProperty(tenSanPham);
        this.giaBan = new SimpleObjectProperty<>(giaBan);
        this.soLuongTonKho = new SimpleIntegerProperty(soLuongTonKho);
        this.idDanhMuc = new SimpleIntegerProperty(idDanhMuc);
        this.trangThai = new SimpleObjectProperty<>(trangThai);
        this.anhSanPham = new SimpleStringProperty(anhSanPham);
        this.tenDanhMuc = new SimpleStringProperty("");
    }

    /**
     * Constructor tạo mới (chưa có ID)
     */
    public SanPham(String tenSanPham, BigDecimal giaBan, int soLuongTonKho, int idDanhMuc, String anhSanPham) {
        this(0, tenSanPham, giaBan, soLuongTonKho, idDanhMuc, TrangThai.ConHang, anhSanPham);
    }

    /**
     * Constructor mặc định
     */
    public SanPham() {
        this(0, "", BigDecimal.ZERO, 0, 0, TrangThai.ConHang, null);
    }

    // ==================== GETTERS & SETTERS ====================

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

    // Giá bán
    public BigDecimal getGiaBan() {
        return giaBan.get();
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan.set(giaBan);
    }

    public ObjectProperty<BigDecimal> giaBanProperty() {
        return giaBan;
    }

    // Số lượng tồn kho
    public int getSoLuongTonKho() {
        return soLuongTonKho.get();
    }

    public void setSoLuongTonKho(int soLuongTonKho) {
        this.soLuongTonKho.set(soLuongTonKho);
    }

    public IntegerProperty soLuongTonKhoProperty() {
        return soLuongTonKho;
    }

    // ID Danh mục
    public int getIdDanhMuc() {
        return idDanhMuc.get();
    }
    public void setIdDanhMuc(int idDanhMuc) {
        this.idDanhMuc.set(idDanhMuc);
    }

    public IntegerProperty idDanhMucProperty() {
        return idDanhMuc;
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

    // Ảnh sản phẩm
    public String getAnhSanPham() {
        return anhSanPham.get();
    }
    public void setAnhSanPham(String anhSanPham) {
        this.anhSanPham.set(anhSanPham);
    }
    public StringProperty anhSanPhamProperty() {
        return anhSanPham;
    }

    // Tên danh mục (phụ)
    public String getTenDanhMuc() {
        return tenDanhMuc.get();
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc.set(tenDanhMuc);
    }

    public StringProperty tenDanhMucProperty() {
        return tenDanhMuc;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Kiểm tra sản phẩm có sẵn để bán không
     */
    public boolean isAvailableForSale() {
        return trangThai.get() == TrangThai.ConHang && soLuongTonKho.get() > 0;
    }

    @Override
    public String toString() {
        return tenSanPham.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SanPham other = (SanPham) obj;
        // Chỉ so sánh khi id > 0 (đã có trong DB)
        if (this.idSanPham.get() == 0 || other.idSanPham.get() == 0) {
            return false;
        }
        return idSanPham.get() == other.idSanPham.get();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idSanPham.get());
    }
}