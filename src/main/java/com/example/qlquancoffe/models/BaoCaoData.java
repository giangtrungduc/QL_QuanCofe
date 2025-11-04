package com.example.qlquancoffe.models;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Model POJO (Lớp Java thuần) để DAO trả về dữ liệu báo cáo thô.
 * (Dùng class thông thường để đảm bảo tương thích)
 */
public class BaoCaoData {
    private final String tenDanhMuc;
    private final String tenSanPham;
    private final int soLuong;
    private final BigDecimal thanhTien;

    /**
     * Constructor phụ để tạo đối tượng từ ResultSet của SQL
     */
    public BaoCaoData(ResultSet rs) throws SQLException {
        this.tenDanhMuc = rs.getString("ten_danhmuc");
        this.tenSanPham = rs.getString("ten_sanpham");
        this.soLuong = rs.getInt("tong_so_luong");
        this.thanhTien = rs.getBigDecimal("tong_thanh_tien");
    }

    // Getters
    public String getTenDanhMuc() { return tenDanhMuc; }
    public String getTenSanPham() { return tenSanPham; }
    public int getSoLuong() { return soLuong; }
    public BigDecimal getThanhTien() { return thanhTien; }
}