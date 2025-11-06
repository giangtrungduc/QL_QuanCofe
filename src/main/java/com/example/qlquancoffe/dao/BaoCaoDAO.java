package com.example.qlquancoffe.dao;

import com.example.qlquancoffe.models.BaoCaoData; // ✅ THÊM IMPORT
import com.example.qlquancoffe.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoDAO {
    /**
     * Lấy báo cáo doanh thu trong khoảng thời gian, CÓ LỌC THEO DANH MỤC
     * @param idDanhMuc 0 nếu muốn lấy tất cả
     */
    public List<BaoCaoData> getBaoCao(LocalDate fromDate, LocalDate toDate, int idDanhMuc) {
        List<BaoCaoData> results = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                d.ten_danhmuc, 
                ct.ten_sanpham, 
                SUM(ct.so_luong) as tong_so_luong, 
                SUM(ct.thanh_tien) as tong_thanh_tien
            FROM chitiethoadon ct
            JOIN hoadon h ON ct.id_hoadon = h.id_hoadon
            JOIN sanpham sp ON ct.id_sanpham = sp.id_sanpham
            JOIN danhmuc d ON sp.id_danhmuc = d.id_danhmuc
            WHERE 
                h.trang_thai = 'PAID' 
                AND DATE(h.ngay_thanh_toan) BETWEEN ? AND ?
        """);

        // Nếu có chọn danh mục cụ thể
        if (idDanhMuc > 0) {
            sql.append(" AND d.id_danhmuc = ? ");
        }

        sql.append(" GROUP BY d.ten_danhmuc, ct.ten_sanpham ORDER BY d.ten_danhmuc, tong_thanh_tien DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setDate(1, java.sql.Date.valueOf(fromDate));
            pstmt.setDate(2, java.sql.Date.valueOf(toDate));

            if (idDanhMuc > 0) {
                pstmt.setInt(3, idDanhMuc);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(new BaoCaoData(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Lấy Top 3 sản phẩm bán chạy nhất theo tháng
     */
    public List<BaoCaoData> getTop3SanPham(int year, int month) {
        List<BaoCaoData> results = new ArrayList<>();
        String sql = """
            SELECT 
                'Top 3' as ten_danhmuc, -- Giả lập cột danh mục
                ct.ten_sanpham, 
                SUM(ct.so_luong) as tong_so_luong,
                SUM(ct.thanh_tien) as tong_thanh_tien
            FROM chitiethoadon ct
            JOIN hoadon h ON ct.id_hoadon = h.id_hoadon
            WHERE 
                h.trang_thai = 'PAID' 
                AND YEAR(h.ngay_thanh_toan) = ?
                AND MONTH(h.ngay_thanh_toan) = ?
            GROUP BY ct.ten_sanpham
            ORDER BY tong_so_luong DESC
            LIMIT 3
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(new BaoCaoData(rs)); // ✅ SỬA
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}