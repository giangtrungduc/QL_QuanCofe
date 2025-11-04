package com.example.qlquancoffe.models;

import javafx.beans.property.*;

/**
 * Model cho bảng taikhoan
 */
public class TaiKhoan {

    // ==================== ENUM ====================

    public enum VaiTro {
        QuanLy("Quản lý"),
        NhanVien("Nhân viên");

        private final String displayName;

        VaiTro(String displayName) {
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

    private final IntegerProperty idNhanVien;
    private final StringProperty hoTen;
    private final StringProperty tenDangNhap;
    private final StringProperty matKhau;
    private final ObjectProperty<VaiTro> vaiTro;

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructor đầy đủ (từ DB)
     */
    public TaiKhoan(int idNhanVien, String hoTen, String tenDangNhap,
                    String matKhau, VaiTro vaiTro) {
        this.idNhanVien = new SimpleIntegerProperty(idNhanVien);
        this.hoTen = new SimpleStringProperty(hoTen);
        this.tenDangNhap = new SimpleStringProperty(tenDangNhap);
        this.matKhau = new SimpleStringProperty(matKhau);
        this.vaiTro = new SimpleObjectProperty<>(vaiTro);
    }

    /**
     * Constructor tạo mới (chưa có ID)
     */
    public TaiKhoan(String hoTen, String tenDangNhap, String matKhau, VaiTro vaiTro) {
        this(0, hoTen, tenDangNhap, matKhau, vaiTro);
    }

    /**
     * Constructor mặc định
     */
    public TaiKhoan() {
        this(0, "", "", "", VaiTro.NhanVien);
    }

    // ==================== GETTERS & SETTERS ====================

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

    // Họ tên
    public String getHoTen() {
        return hoTen.get();
    }

    public void setHoTen(String hoTen) {
        this.hoTen.set(hoTen);
    }

    public StringProperty hoTenProperty() {
        return hoTen;
    }

    // Tên đăng nhập
    public String getTenDangNhap() {
        return tenDangNhap.get();
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap.set(tenDangNhap);
    }

    public StringProperty tenDangNhapProperty() {
        return tenDangNhap;
    }

    // Mật khẩu
    public String getMatKhau() {
        return matKhau.get();
    }

    public void setMatKhau(String matKhau) {
        this.matKhau.set(matKhau);
    }

    public StringProperty matKhauProperty() {
        return matKhau;
    }

    // Vai trò
    public VaiTro getVaiTro() {
        return vaiTro.get();
    }

    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro.set(vaiTro);
    }

    public ObjectProperty<VaiTro> vaiTroProperty() {
        return vaiTro;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Kiểm tra là quản lý
     */
    public boolean isManager() {
        return vaiTro.get() == VaiTro.QuanLy;
    }

    @Override
    public String toString() {
        return hoTen.get() + " (" + tenDangNhap.get() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TaiKhoan other = (TaiKhoan) obj;
        // Chỉ so sánh khi id > 0 (đã có trong DB)
        if (this.idNhanVien.get() == 0 || other.idNhanVien.get() == 0) {
            return false;
        }
        return idNhanVien.get() == other.idNhanVien.get();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idNhanVien.get());
    }
}