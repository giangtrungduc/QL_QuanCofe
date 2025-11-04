package com.example.qlquancoffe.dao;

import com.example.qlquancoffe.models.SanPham;
import com.example.qlquancoffe.models.SanPham.TrangThai;
import com.example.qlquancoffe.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * DAO xử lý thao tác CSDL cho bảng sanpham
 */
public class SanPhamDAO {

    // ==================== INSERT & UPDATE ====================

    /**
     * Thêm sản phẩm mới
     */
    public int insert(SanPham sp) {
        String sql = """
            INSERT INTO sanpham(ten_sanpham, gia_ban, so_luong_ton_kho, id_danhmuc, anh_san_pham)
            VALUES(?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, sp.getTenSanPham());
            pstmt.setBigDecimal(2, sp.getGiaBan());
            pstmt.setInt(3, sp.getSoLuongTonKho());
            pstmt.setInt(4, sp.getIdDanhMuc());
            pstmt.setString(5, sp.getAnhSanPham());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    sp.setIdSanPham(id);
                    System.out.println("✅ Thêm sản phẩm #" + id + ": " + sp.getTenSanPham());
                    return id;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Cập nhật sản phẩm
     */
    public boolean update(SanPham sp) {
        String sql = """
            UPDATE sanpham 
            SET ten_sanpham = ?, gia_ban = ?,
                so_luong_ton_kho = ?, id_danhmuc = ?, trang_thai = ?, anh_san_pham = ?
            WHERE id_sanpham = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sp.getTenSanPham());
            pstmt.setBigDecimal(2, sp.getGiaBan());
            pstmt.setInt(3, sp.getSoLuongTonKho());
            pstmt.setInt(4, sp.getIdDanhMuc());
            pstmt.setString(5, sp.getTrangThai().name());
            pstmt.setString(6, sp.getAnhSanPham());
            pstmt.setInt(7, sp.getIdSanPham());

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Cập nhật sản phẩm #" + sp.getIdSanPham());
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật chỉ tồn kho (Dùng nội bộ hoặc khi nhập hàng)
     */
    public boolean updateTonKho(int idSanPham, int soLuongMoi) {
        String sql = "UPDATE sanpham SET so_luong_ton_kho = ? WHERE id_sanpham = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, soLuongMoi);
            pstmt.setInt(2, idSanPham);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Cập nhật tồn kho SP #" + idSanPham + ": " + soLuongMoi);
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật tồn kho: " + e.getMessage());
        }

        return false;
    }

    /**
     * Xóa sản phẩm
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM sanpham WHERE id_sanpham = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            boolean success = pstmt.executeUpdate() > 0;

            if (success) {
                System.out.println("✅ Xóa sản phẩm #" + id);
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi xóa sản phẩm: " + e.getMessage());
            System.err.println("💡 Sản phẩm có thể đang có trong hóa đơn (ON DELETE RESTRICT)");
        }

        return false;
    }

    // ==================== RETRIEVE (SELECT) ====================

    /**
     * Lấy tất cả sản phẩm
     */
    public ObservableList<SanPham> getAll() {
        ObservableList<SanPham> list = FXCollections.observableArrayList();

        String sql = """
            SELECT s.*, d.ten_danhmuc 
            FROM sanpham s 
            LEFT JOIN danhmuc d ON s.id_danhmuc = d.id_danhmuc
            ORDER BY s.ten_sanpham
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SanPham sp = extractFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

            System.out.println("✅ Load " + list.size() + " sản phẩm");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy sản phẩm theo ID
     */
    public SanPham getById(int id) {
        String sql = """
            SELECT s.*, d.ten_danhmuc 
            FROM sanpham s 
            LEFT JOIN danhmuc d ON s.id_danhmuc = d.id_danhmuc
            WHERE s.id_sanpham = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SanPham sp = extractFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                return sp;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy sản phẩm: " + e.getMessage());
        }

        return null;
    }

    /**
     * Lấy sản phẩm theo danh mục
     */
    public ObservableList<SanPham> getByDanhMuc(int idDanhMuc) {
        ObservableList<SanPham> list = FXCollections.observableArrayList();

        String sql = """
            SELECT s.*, d.ten_danhmuc 
            FROM sanpham s 
            LEFT JOIN danhmuc d ON s.id_danhmuc = d.id_danhmuc
            WHERE s.id_danhmuc = ?
            ORDER BY s.ten_sanpham
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idDanhMuc);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                SanPham sp = extractFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy sản phẩm theo danh mục: " + e.getMessage());
        }

        return list;
    }

    /**
     * Tìm kiếm sản phẩm (chỉ theo tên)
     */
    public ObservableList<SanPham> search(String keyword) {
        ObservableList<SanPham> list = FXCollections.observableArrayList();

        String sql = """
            SELECT s.*, d.ten_danhmuc 
            FROM sanpham s 
            LEFT JOIN danhmuc d ON s.id_danhmuc = d.id_danhmuc
            WHERE s.ten_sanpham LIKE ?
            ORDER BY s.ten_sanpham
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                SanPham sp = extractFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tìm kiếm: " + e.getMessage());
        }

        return list;
    }

    /**
     * Lấy sản phẩm còn hàng (có thể bán)
     */
    public ObservableList<SanPham> getAvailableForSale() {
        ObservableList<SanPham> list = FXCollections.observableArrayList();

        String sql = """
            SELECT s.*, d.ten_danhmuc 
            FROM sanpham s 
            LEFT JOIN danhmuc d ON s.id_danhmuc = d.id_danhmuc
            WHERE s.trang_thai = 'ConHang' AND s.so_luong_ton_kho > 0
            ORDER BY s.ten_sanpham
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SanPham sp = extractFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy sản phẩm: " + e.getMessage());
        }

        return list;
    }

    // ==================== UTILITIES ====================

    /**
     * Kiểm tra tên sản phẩm đã tồn tại
     */
    public boolean isTenSanPhamTonTai(String tenSanPham) {
        return isTenSanPhamTonTai(tenSanPham, 0);
    }

    /**
     * Kiểm tra tên sản phẩm đã tồn tại
     */
    public boolean isTenSanPhamTonTai(String tenSanPham, int excludeId) {
        String sql = "SELECT COUNT(*) FROM sanpham WHERE ten_sanpham = ? AND id_sanpham != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tenSanPham);
            pstmt.setInt(2, excludeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi kiểm tra tên: " + e.getMessage());
        }

        return false;
    }

    // ==================== HELPER METHOD ====================

    /**
     * Trích xuất SanPham từ ResultSet
     */
    private SanPham extractFromResultSet(ResultSet rs) throws SQLException {
        // Parse trạng thái
        TrangThai trangThai = TrangThai.ConHang;
        try {
            String ttStr = rs.getString("trang_thai");
            if (ttStr != null) {
                trangThai = TrangThai.valueOf(ttStr);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Trạng thái không hợp lệ: " + rs.getString("trang_thai"));
        }

        return new SanPham(
                rs.getInt("id_sanpham"),
                rs.getString("ten_sanpham"),
                rs.getBigDecimal("gia_ban"),
                rs.getInt("so_luong_ton_kho"),
                rs.getInt("id_danhmuc"),
                trangThai,
                rs.getString("anh_san_pham")
        );
    }
}