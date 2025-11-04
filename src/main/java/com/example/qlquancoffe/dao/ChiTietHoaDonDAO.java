package com.example.qlquancoffe.dao;

import com.example.qlquancoffe.models.ChiTietHoaDon;
import com.example.qlquancoffe.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.math.BigDecimal;

/**
 * DAO xử lý thao tác CSDL cho bảng chitiethoadon
 * Trigger tự động: cập nhật tồn kho, tổng tiền hóa đơn
 */
public class ChiTietHoaDonDAO {

    // ==================== INSERT ====================

    /**
     * Thêm chi tiết hóa đơn mới
     * Trigger tự động: trừ tồn kho, cập nhật tổng tiền
     */
    public int insert(ChiTietHoaDon chiTiet) {
        String sql = """
            INSERT INTO chitiethoadon(id_hoadon, id_sanpham, ten_sanpham, 
                                      so_luong, don_gia, thanh_tien, ghi_chu)
            VALUES(?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, chiTiet.getIdHoaDon());
            pstmt.setInt(2, chiTiet.getIdSanPham());
            pstmt.setString(3, chiTiet.getTenSanPham());
            pstmt.setInt(4, chiTiet.getSoLuong());
            pstmt.setBigDecimal(5, chiTiet.getDonGia());
            pstmt.setBigDecimal(6, chiTiet.getThanhTien());
            pstmt.setString(7, chiTiet.getGhiChu());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    chiTiet.setIdChiTietHoaDon(id);
                    System.out.println("✅ Thêm chi tiết #" + id + ": " + chiTiet.getTenSanPham());
                    return id;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm chi tiết: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("💡 Sản phẩm đã có trong hóa đơn!");
            }
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Thêm nhiều chi tiết cùng lúc (batch insert)
     */
    public boolean insertBatch(ObservableList<ChiTietHoaDon> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            return false;
        }

        String sql = """
            INSERT INTO chitiethoadon(id_hoadon, id_sanpham, ten_sanpham, 
                                      so_luong, don_gia, thanh_tien, ghi_chu)
            VALUES(?, ?, ?, ?, ?, ?, ?)
        """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            for (ChiTietHoaDon ct : danhSach) {
                pstmt.setInt(1, ct.getIdHoaDon());
                pstmt.setInt(2, ct.getIdSanPham());
                pstmt.setString(3, ct.getTenSanPham());
                pstmt.setInt(4, ct.getSoLuong());
                pstmt.setBigDecimal(5, ct.getDonGia());
                pstmt.setBigDecimal(6, ct.getThanhTien());
                pstmt.setString(7, ct.getGhiChu());
                pstmt.addBatch();
            }

            pstmt.executeBatch();

            // Lấy generated keys
            ResultSet keys = pstmt.getGeneratedKeys();
            int index = 0;
            while (keys.next() && index < danhSach.size()) {
                danhSach.get(index).setIdChiTietHoaDon(keys.getInt(1));
                index++;
            }

            conn.commit(); // Commit transaction
            System.out.println("✅ Thêm " + danhSach.size() + " chi tiết thành công");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi batch insert: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("🔄 Đã rollback transaction");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    // ==================== UPDATE ====================

    /**
     * Cập nhật chi tiết hóa đơn
     * Trigger tự động: cập nhật tồn kho, tổng tiền
     */
    public boolean update(ChiTietHoaDon chiTiet) {
        String sql = """
            UPDATE chitiethoadon 
            SET so_luong = ?, 
                don_gia = ?,
                thanh_tien = ?,
                ghi_chu = ?
            WHERE id_chitiethoadon = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, chiTiet.getSoLuong());
            pstmt.setBigDecimal(2, chiTiet.getDonGia());
            pstmt.setBigDecimal(3, chiTiet.getThanhTien());
            pstmt.setString(4, chiTiet.getGhiChu());
            pstmt.setInt(5, chiTiet.getIdChiTietHoaDon());

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Cập nhật chi tiết #" + chiTiet.getIdChiTietHoaDon());
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật chi tiết: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật chỉ số lượng (đơn giản hơn)
     */
    public boolean updateSoLuong(int idChiTietHoaDon, int soLuongMoi) {
        String sql = """
            UPDATE chitiethoadon 
            SET so_luong = ?, 
                thanh_tien = don_gia * ?
            WHERE id_chitiethoadon = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, soLuongMoi);
            pstmt.setInt(2, soLuongMoi);
            pstmt.setInt(3, idChiTietHoaDon);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Cập nhật số lượng chi tiết #" + idChiTietHoaDon);
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật số lượng: " + e.getMessage());
        }

        return false;
    }

    // ==================== DELETE ====================

    /**
     * Xóa chi tiết hóa đơn theo ID
     * Trigger tự động: hoàn tồn kho, cập nhật tổng tiền
     */
    public boolean delete(int idChiTietHoaDon) {
        String sql = "DELETE FROM chitiethoadon WHERE id_chitiethoadon = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idChiTietHoaDon);
            boolean success = pstmt.executeUpdate() > 0;

            if (success) {
                System.out.println("✅ Xóa chi tiết #" + idChiTietHoaDon);
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi xóa chi tiết: " + e.getMessage());
        }

        return false;
    }

    /**
     * Xóa tất cả chi tiết của hóa đơn
     * Trigger tự động: hoàn tồn kho, set tổng tiền = 0
     */
    public boolean deleteByHoaDon(int idHoaDon) {
        String sql = "DELETE FROM chitiethoadon WHERE id_hoadon = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idHoaDon);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Xóa " + affected + " chi tiết của hóa đơn #" + idHoaDon);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi xóa chi tiết: " + e.getMessage());
        }

        return false;
    }

    // ==================== RETRIEVE (SELECT) ====================

    /**
     * Lấy chi tiết hóa đơn theo ID hóa đơn
     */
    public ObservableList<ChiTietHoaDon> getChiTietByHoaDon(int idHoaDon) {
        ObservableList<ChiTietHoaDon> list = FXCollections.observableArrayList();

        String sql = """
            SELECT * FROM chitiethoadon 
            WHERE id_hoadon = ?
            ORDER BY id_chitiethoadon
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idHoaDon);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ChiTietHoaDon ct = extractFromResultSet(rs);
                list.add(ct);
            }

            System.out.println("✅ Load " + list.size() + " chi tiết HĐ #" + idHoaDon);

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy chi tiết: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy chi tiết theo ID
     */
    public ChiTietHoaDon getById(int idChiTietHoaDon) {
        String sql = "SELECT * FROM chitiethoadon WHERE id_chitiethoadon = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idChiTietHoaDon);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy chi tiết: " + e.getMessage());
        }

        return null;
    }

    /**
     * Lấy chi tiết cụ thể (hóa đơn + sản phẩm)
     */
    public ChiTietHoaDon getByHoaDonAndSanPham(int idHoaDon, int idSanPham) {
        String sql = """
            SELECT * FROM chitiethoadon 
            WHERE id_hoadon = ? AND id_sanpham = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idHoaDon);
            pstmt.setInt(2, idSanPham);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy chi tiết: " + e.getMessage());
        }

        return null;
    }

    // ==================== UTILITIES ====================

    /**
     * Kiểm tra sản phẩm đã có trong hóa đơn chưa
     */
    public boolean exists(int idHoaDon, int idSanPham) {
        String sql = """
            SELECT COUNT(*) FROM chitiethoadon 
            WHERE id_hoadon = ? AND id_sanpham = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idHoaDon);
            pstmt.setInt(2, idSanPham);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi kiểm tra chi tiết: " + e.getMessage());
        }

        return false;
    }

    /**
     * Đếm số sản phẩm trong hóa đơn
     */
    public int countByHoaDon(int idHoaDon) {
        String sql = "SELECT COUNT(*) FROM chitiethoadon WHERE id_hoadon = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idHoaDon);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi đếm chi tiết: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Tính tổng tiền của hóa đơn
     */
    public BigDecimal getTongTien(int idHoaDon) {
        String sql = """
            SELECT COALESCE(SUM(thanh_tien), 0) 
            FROM chitiethoadon 
            WHERE id_hoadon = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idHoaDon);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tính tổng: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    /**
     * Đếm số lượng sản phẩm (LEGACY - để tương thích code cũ)
     */
    @Deprecated
    public int countSanPham(int idHoaDon) {
        return countByHoaDon(idHoaDon);
    }

    /**
     * Tính tổng tiền (LEGACY - để tương thích code cũ)
     */
    @Deprecated
    public BigDecimal calculateTongTien(int idHoaDon) {
        return getTongTien(idHoaDon);
    }

    // ==================== HELPER METHOD ====================

    /**
     * Trích xuất ChiTietHoaDon từ ResultSet
     */
    private ChiTietHoaDon extractFromResultSet(ResultSet rs) throws SQLException {
        return new ChiTietHoaDon(
                rs.getInt("id_chitiethoadon"),
                rs.getInt("id_hoadon"),
                rs.getInt("id_sanpham"),
                rs.getString("ten_sanpham"),
                rs.getInt("so_luong"),
                rs.getBigDecimal("don_gia"),
                rs.getBigDecimal("thanh_tien"),
                rs.getString("ghi_chu")
        );
    }
}