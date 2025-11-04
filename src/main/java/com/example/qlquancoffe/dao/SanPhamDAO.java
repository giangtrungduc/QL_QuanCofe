package com.example.qlquancoffe.dao;

import com.example.qlquancoffe.models.SanPham;
import com.example.qlquancoffe.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.math.BigDecimal;

/**
 * DAO xử lý thao tác CSDL cho bảng sanpham
 */
public class SanPhamDAO {

    /**
     * Lấy tất cả sản phẩm
     */
    public ObservableList<SanPham> getAllSanPham() {
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
                SanPham sp = extractSanPhamFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

            System.out.println("✅ Đã load " + list.size() + " sản phẩm");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy sản phẩm theo ID
     */
    public SanPham getSanPhamById(int id) {
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
                SanPham sp = extractSanPhamFromResultSet(rs);
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
    public ObservableList<SanPham> getSanPhamByDanhMuc(int idDanhMuc) {
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
                SanPham sp = extractSanPhamFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy sản phẩm theo danh mục: " + e.getMessage());
        }

        return list;
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    public ObservableList<SanPham> searchSanPham(String keyword, int idDanhMuc) {
        ObservableList<SanPham> list = FXCollections.observableArrayList();

        String sql = """
        SELECT s.*, d.ten_danhmuc 
        FROM sanpham s 
        LEFT JOIN danhmuc d ON s.id_danhmuc = d.id_danhmuc
        WHERE s.ten_sanpham LIKE ? AND s.id_danhmuc = ?
        ORDER BY s.ten_sanpham
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Gán giá trị cho các dấu ? trong câu SQL
            pstmt.setString(1, "%" + keyword + "%"); // từ khóa tìm kiếm
            pstmt.setInt(2, idDanhMuc);              // chỉ tìm trong danh mục cụ thể

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                SanPham sp = extractSanPhamFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tìm kiếm sản phẩm: " + e.getMessage());
        }

        return list;
    }

    /**
     * Lấy sản phẩm còn hàng
     */
    public ObservableList<SanPham> getSanPhamConHang() {
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
                SanPham sp = extractSanPhamFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy sản phẩm còn hàng: " + e.getMessage());
        }

        return list;
    }

    /**
     * Lấy sản phẩm sắp hết hàng (tồn kho < threshold)
     */
    public ObservableList<SanPham> getSanPhamSapHet(int threshold) {
        ObservableList<SanPham> list = FXCollections.observableArrayList();

        String sql = """
            SELECT s.*, d.ten_danhmuc 
            FROM sanpham s 
            LEFT JOIN danhmuc d ON s.id_danhmuc = d.id_danhmuc
            WHERE s.so_luong_ton_kho > 0 AND s.so_luong_ton_kho < ?
            ORDER BY s.so_luong_ton_kho ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, threshold);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                SanPham sp = extractSanPhamFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy sản phẩm sắp hết: " + e.getMessage());
        }

        return list;
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    public ObservableList<SanPham> searchSanPham(String keyword) {
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

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                SanPham sp = extractSanPhamFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tìm kiếm sản phẩm: " + e.getMessage());
        }

        return list;
    }

    /**
     * Kiểm tra tên sản phẩm trước khi thêm
     * @param tenSanPham
     * @return
     */
    public boolean isTenSanPhamTonTai(String tenSanPham) {
        String sql = "SELECT COUNT(*) FROM sanpham WHERE ten_sanpham = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tenSanPham);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // true nếu tồn tại
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi kiểm tra tên sản phẩm: " + e.getMessage());
        }

        return false;
    }

    /**
     * Thêm sản phẩm mới
     */
    public boolean addSanPham(SanPham sp) {
        String sql = "INSERT INTO sanpham(ten_sanpham, gia_ban, so_luong_ton_kho, anh_san_pham, id_danhmuc) " +
                "VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, sp.getTenSanPham());
            pstmt.setBigDecimal(2, sp.getGiaBan());
            pstmt.setInt(3, sp.getSoLuongTonKho());
            pstmt.setString(4, sp.getAnhSanPham());
            pstmt.setInt(5, sp.getIdDanhMuc());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    sp.setIdSanPham(keys.getInt(1));
                }
                System.out.println("✅ Thêm sản phẩm thành công: " + sp.getTenSanPham());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm sản phẩm: " + e.getMessage());
        }

        return false;
    }

    /**
     * Cập nhật sản phẩm
     */
    public boolean updateSanPham(SanPham sp) {
        String sql = "UPDATE sanpham SET ten_sanpham=?, gia_ban=?, so_luong_ton_kho=?, " +
                "anh_san_pham=?, id_danhmuc=? WHERE id_sanpham=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sp.getTenSanPham());
            pstmt.setBigDecimal(2, sp.getGiaBan());
            pstmt.setInt(3, sp.getSoLuongTonKho());
            pstmt.setString(4, sp.getAnhSanPham());
            pstmt.setInt(5, sp.getIdDanhMuc());
            pstmt.setInt(6, sp.getIdSanPham());

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Cập nhật sản phẩm thành công");
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật sản phẩm: " + e.getMessage());
        }

        return false;
    }

    /**
     * Cập nhật chỉ tồn kho (dùng khi nhập hàng)
     */
    public boolean updateTonKho(int idSanPham, int soLuongMoi) {
        String sql = "UPDATE sanpham SET so_luong_ton_kho = ? WHERE id_sanpham = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, soLuongMoi);
            pstmt.setInt(2, idSanPham);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Cập nhật tồn kho thành công");
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật tồn kho: " + e.getMessage());
        }

        return false;
    }

    /**
     * Trừ tồn kho khi bán hàng
     * @param idSanPham ID sản phẩm
     * @param soLuong Số lượng cần trừ
     * @return true nếu thành công
     */
    public boolean giamTonKho(int idSanPham, int soLuong) {
        String sql = "UPDATE sanpham SET so_luong_ton_kho = so_luong_ton_kho - ? WHERE id_sanpham = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, soLuong);
            pstmt.setInt(2, idSanPham);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi giảm tồn kho: " + e.getMessage());
        }

        return false;
    }

    /**
     * Tăng tồn kho (khi hủy đơn hoặc nhập hàng)
     */
    public boolean tangTonKho(int idSanPham, int soLuong) {
        String sql = "UPDATE sanpham SET so_luong_ton_kho = so_luong_ton_kho + ? WHERE id_sanpham = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, soLuong);
            pstmt.setInt(2, idSanPham);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tăng tồn kho: " + e.getMessage());
        }

        return false;
    }

    /**
     * Xóa sản phẩm
     */
    public boolean deleteSanPham(int id) {
        String sql = "DELETE FROM sanpham WHERE id_sanpham = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            boolean success = pstmt.executeUpdate() > 0;

            if (success) {
                System.out.println("✅ Xóa sản phẩm thành công");
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi xóa sản phẩm: " + e.getMessage());
            System.err.println("💡 Sản phẩm có thể đang có trong hóa đơn");
        }

        return false;
    }

    /**
     * Lấy top sản phẩm bán chạy
     */
    public ObservableList<SanPham> getTopBanChay(int limit) {
        ObservableList<SanPham> list = FXCollections.observableArrayList();

        String sql = """
            SELECT s.*, d.ten_danhmuc, SUM(c.so_luong) as tong_ban
            FROM sanpham s
            LEFT JOIN danhmuc d ON s.id_danhmuc = d.id_danhmuc
            LEFT JOIN chitiethoadon c ON s.id_sanpham = c.id_sanpham
            GROUP BY s.id_sanpham
            ORDER BY tong_ban DESC
            LIMIT ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                SanPham sp = extractSanPhamFromResultSet(rs);
                sp.setTenDanhMuc(rs.getString("ten_danhmuc"));
                list.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy top bán chạy: " + e.getMessage());
        }

        return list;
    }

    // ==================== HELPER METHOD ====================

    /**
     * Trích xuất đối tượng SanPham từ ResultSet
     */
    private SanPham extractSanPhamFromResultSet(ResultSet rs) throws SQLException {
        return new SanPham(
                rs.getInt("id_sanpham"),
                rs.getString("ten_sanpham"),
                rs.getBigDecimal("gia_ban"),
                rs.getInt("so_luong_ton_kho"),
                rs.getString("anh_san_pham"),
                rs.getInt("id_danhmuc"),
                SanPham.TrangThai.valueOf(rs.getString("trang_thai")),
                rs.getTimestamp("ngay_tao").toLocalDateTime(),
                rs.getTimestamp("ngay_cap_nhat").toLocalDateTime()
        );
    }
}