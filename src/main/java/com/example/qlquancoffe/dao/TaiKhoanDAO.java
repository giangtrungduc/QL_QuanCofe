package com.example.qlquancoffe.dao;

import com.example.qlquancoffe.models.TaiKhoan;
import com.example.qlquancoffe.models.TaiKhoan.VaiTro;
import com.example.qlquancoffe.utils.DatabaseConnection;
import com.example.qlquancoffe.utils.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * DAO xử lý thao tác CSDL cho bảng taikhoan
 */
public class TaiKhoanDAO {

    // ==================== AUTHENTICATION ====================

    /**
     * Kiểm tra đăng nhập
     */
    public TaiKhoan checkLogin(String username, String password) {
        // Đã bỏ check trang_thai vì cột này không còn
        String sql = "SELECT * FROM taikhoan WHERE ten_dang_nhap = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("mat_khau");

                // Kiểm tra mật khẩu
                if (PasswordUtil.checkPassword(password, hashedPassword)) {
                    TaiKhoan tk = extractFromResultSet(rs);
                    System.out.println("✅ Đăng nhập thành công: " + username);
                    return tk;
                } else {
                    System.out.println("❌ Sai mật khẩu");
                }
            } else {
                System.out.println("❌ Tài khoản không tồn tại");
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi kiểm tra đăng nhập: " + e.getMessage());
        }

        return null;
    }

    // ==================== INSERT & UPDATE ====================

    /**
     * Thêm tài khoản mới
     */
    public int insert(TaiKhoan tk) {
        String sql = """
            INSERT INTO taikhoan(ho_ten, ten_dang_nhap, mat_khau, vai_tro)
            VALUES(?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, tk.getHoTen());
            pstmt.setString(2, tk.getTenDangNhap());
            pstmt.setString(3, tk.getMatKhau()); // Phải đã hash trước
            pstmt.setString(4, tk.getVaiTro().name());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    tk.setIdNhanVien(id);
                    System.out.println("✅ Thêm tài khoản #" + id + ": " + tk.getTenDangNhap());
                    return id;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm tài khoản: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("💡 Tên đăng nhập đã tồn tại!");
            }
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Cập nhật thông tin tài khoản
     */
    public boolean update(TaiKhoan tk) {
        String sql = """
            UPDATE taikhoan 
            SET ho_ten = ?, vai_tro = ?
            WHERE id_nhanvien = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tk.getHoTen());
            pstmt.setString(2, tk.getVaiTro().name());
            pstmt.setInt(3, tk.getIdNhanVien());

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Cập nhật tài khoản #" + tk.getIdNhanVien());
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật tài khoản: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Reset mật khẩu (chỉ dành cho admin)
     */
    public boolean resetPassword(int idNhanVien, String newPasswordHash) {
        String sql = "UPDATE taikhoan SET mat_khau = ? WHERE id_nhanvien = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, idNhanVien);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Reset mật khẩu #" + idNhanVien);
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi reset mật khẩu: " + e.getMessage());
        }

        return false;
    }

    /**
     * Hard delete - xóa vĩnh viễn
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM taikhoan WHERE id_nhanvien = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            boolean success = pstmt.executeUpdate() > 0;

            if (success) {
                System.out.println("✅ Xóa vĩnh viễn tài khoản #" + id);
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi xóa tài khoản: " + e.getMessage());
            System.err.println("💡 Tài khoản có thể có hóa đơn liên quan (ON DELETE RESTRICT)");
        }

        return false;
    }

    // ==================== RETRIEVE (SELECT) ====================

    /**
     * Lấy tất cả tài khoản
     */
    public ObservableList<TaiKhoan> getAll() {
        ObservableList<TaiKhoan> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM taikhoan ORDER BY ho_ten";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }

            System.out.println("✅ Load " + list.size() + " tài khoản");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy tài khoản: " + e.getMessage());
        }

        return list;
    }

    /**
     * Lấy tài khoản theo ID
     */
    public TaiKhoan getById(int id) {
        String sql = "SELECT * FROM taikhoan WHERE id_nhanvien = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy tài khoản: " + e.getMessage());
        }

        return null;
    }

    /**
     * Lấy theo username
     */
    public TaiKhoan getByUsername(String username) {
        String sql = "SELECT * FROM taikhoan WHERE ten_dang_nhap = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy tài khoản: " + e.getMessage());
        }

        return null;
    }

    // ==================== UTILITIES ====================

    /**
     * Kiểm tra username đã tồn tại
     */
    public boolean isUsernameExist(String username) {
        return isUsernameExist(username, 0);
    }

    /**
     * Kiểm tra username đã tồn tại (loại trừ ID khi update)
     */
    public boolean isUsernameExist(String username, int excludeId) {
        String sql = "SELECT COUNT(*) FROM taikhoan WHERE ten_dang_nhap = ? AND id_nhanvien != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, excludeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi kiểm tra username: " + e.getMessage());
        }

        return false;
    }

    // ==================== HELPER METHOD ====================

    /**
     * Trích xuất TaiKhoan từ ResultSet
     */
    private TaiKhoan extractFromResultSet(ResultSet rs) throws SQLException {
        return new TaiKhoan(
                rs.getInt("id_nhanvien"),
                rs.getString("ho_ten"),
                rs.getString("ten_dang_nhap"),
                rs.getString("mat_khau"),
                VaiTro.valueOf(rs.getString("vai_tro"))
        );
    }
}