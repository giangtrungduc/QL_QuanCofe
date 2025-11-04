package com.example.qlquancoffe.dao;

import com.example.qlquancoffe.models.HoaDon;
import com.example.qlquancoffe.models.HoaDon.TrangThai;
import com.example.qlquancoffe.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * DAO xử lý thao tác CSDL cho bảng hoadon (ĐÃ ĐIỀU CHỈNH)
 * Đã loại bỏ tien_khach_dua và tien_thua
 */
public class HoaDonDAO {

    // ==================== INSERT & UPDATE ====================

    /**
     * Thêm hóa đơn mới (trạng thái PENDING mặc định)
     * @return ID của hóa đơn vừa tạo, hoặc -1 nếu thất bại
     */
    public int insert(HoaDon hoaDon) {
        String sql = """
            INSERT INTO hoadon(id_nhanvien, tong_tien, trang_thai, ghi_chu)
            VALUES(?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, hoaDon.getIdNhanVien());
            pstmt.setBigDecimal(2, hoaDon.getTongTien()); // Ban đầu là 0
            pstmt.setString(3, hoaDon.getTrangThai().name()); // PENDING
            pstmt.setString(4, hoaDon.getGhiChu());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    hoaDon.setIdHoaDon(id);
                    System.out.println("✅ Tạo hóa đơn #" + id + " thành công");
                    return id;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tạo hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Cập nhật ghi chú hóa đơn (chỉ cho phép cập nhật ghi chú khi đang PENDING)
     */
    public boolean update(HoaDon hoaDon) {
        String sql = """
            UPDATE hoadon 
            SET ghi_chu = ?
            WHERE id_hoadon = ? AND trang_thai = 'PENDING'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hoaDon.getGhiChu());
            pstmt.setInt(2, hoaDon.getIdHoaDon());

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Cập nhật ghi chú HĐ #" + hoaDon.getIdHoaDon());
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa hóa đơn (CASCADE sẽ tự động xóa chi tiết)
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM hoadon WHERE id_hoadon = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            boolean success = pstmt.executeUpdate() > 0;

            if (success) {
                System.out.println("✅ Xóa hóa đơn #" + id + " thành công");
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi xóa hóa đơn: " + e.getMessage());
        }

        return false;
    }

    // ==================== RETRIEVE (SELECT) ====================

    /**
     * Lấy tất cả hóa đơn (đã thanh toán)
     */
    public ObservableList<HoaDon> getAllHoaDon() {
        ObservableList<HoaDon> list = FXCollections.observableArrayList();

        String sql = """
            SELECT h.*, t.ho_ten 
            FROM hoadon h
            LEFT JOIN taikhoan t ON h.id_nhanvien = t.id_nhanvien
            WHERE h.trang_thai = 'PAID'
            ORDER BY h.ngay_thanh_toan DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                HoaDon hd = extractHoaDonFromResultSet(rs);
                hd.setTenNhanVien(rs.getString("ho_ten"));
                list.add(hd);
            }
            System.out.println("✅ Đã load " + list.size() + " hóa đơn đã thanh toán");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy TẤT CẢ hóa đơn (mọi trạng thái) cho Admin
     * (Hàm này JOIN với taikhoan để lấy tên NV)
     */
    public ObservableList<HoaDon> getAllHoaDon_Admin() {
        ObservableList<HoaDon> list = FXCollections.observableArrayList();

        String sql = """
            SELECT h.*, t.ho_ten 
            FROM hoadon h
            LEFT JOIN taikhoan t ON h.id_nhanvien = t.id_nhanvien
            ORDER BY h.ngay_tao DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Tái sử dụng hàm extract của bạn
                HoaDon hd = extractHoaDonFromResultSet(rs);
                hd.setTenNhanVien(rs.getString("ho_ten"));
                list.add(hd);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy tất cả hóa đơn (Admin): " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy hóa đơn theo ID
     */
    public HoaDon getById(int id) {
        String sql = """
            SELECT h.*, t.ho_ten 
            FROM hoadon h
            LEFT JOIN taikhoan t ON h.id_nhanvien = t.id_nhanvien
            WHERE h.id_hoadon = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                HoaDon hd = extractHoaDonFromResultSet(rs);
                hd.setTenNhanVien(rs.getString("ho_ten"));
                return hd;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy hóa đơn: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy hóa đơn PENDING của nhân viên
     */
    public ObservableList<HoaDon> getPendingInvoicesByNhanVien(int idNhanVien) {
        ObservableList<HoaDon> list = FXCollections.observableArrayList();

        String sql = """
            SELECT h.*, t.ho_ten 
            FROM hoadon h
            LEFT JOIN taikhoan t ON h.id_nhanvien = t.id_nhanvien
            WHERE h.id_nhanvien = ? AND h.trang_thai = 'PENDING'
            ORDER BY h.ngay_tao DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idNhanVien);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                HoaDon hd = extractHoaDonFromResultSet(rs);
                hd.setTenNhanVien(rs.getString("ho_ten"));
                list.add(hd);
            }
            System.out.println("✅ Load " + list.size() + " hóa đơn chờ của NV #" + idNhanVien);

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy hóa đơn chờ: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy hóa đơn đã thanh toán theo ngày
     */
    public ObservableList<HoaDon> getHoaDonByDate(LocalDate date) {
        ObservableList<HoaDon> list = FXCollections.observableArrayList();

        String sql = """
            SELECT h.*, t.ho_ten 
            FROM hoadon h
            LEFT JOIN taikhoan t ON h.id_nhanvien = t.id_nhanvien
            WHERE DATE(h.ngay_thanh_toan) = ?
              AND h.trang_thai = 'PAID'
            ORDER BY h.ngay_thanh_toan DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                HoaDon hd = extractHoaDonFromResultSet(rs);
                hd.setTenNhanVien(rs.getString("ho_ten"));
                list.add(hd);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy hóa đơn: " + e.getMessage());
        }

        return list;
    }

    /**
     * Lấy hóa đơn bởi nhân viên
     */
    public ObservableList<HoaDon> getHoaDonByNhanVien(int idNhanVien) {
        ObservableList<HoaDon> list = FXCollections.observableArrayList();

        String sql = """
            SELECT h.*, t.ho_ten 
            FROM hoadon h
            LEFT JOIN taikhoan t ON h.id_nhanvien = t.id_nhanvien
            WHERE h.id_nhanvien = ?
            ORDER BY h.ngay_tao DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idNhanVien);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                HoaDon hd = extractHoaDonFromResultSet(rs);
                hd.setTenNhanVien(rs.getString("ho_ten"));
                list.add(hd);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy tất cả hóa đơn của NV: " + e.getMessage());
        }
        return list;
    }

    // ==================== PAYMENT ACTIONS ====================

    /**
     * Hoàn thành thanh toán (PENDING → PAID)
     * Trigger sẽ tự động set 'ngay_thanh_toan'
     */
    public boolean completePayment(int idHoaDon) {
        String sql = """
            UPDATE hoadon 
            SET trang_thai = 'PAID'
            WHERE id_hoadon = ? AND trang_thai = 'PENDING'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idHoaDon);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Thanh toán hóa đơn #" + idHoaDon + " thành công");
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thanh toán: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hủy hóa đơn (PENDING → CANCELLED)
     * Trigger xóa chitiethoadon sẽ tự động hoàn kho
     */
    public boolean cancelInvoice(int idHoaDon) {
        String sql = """
            UPDATE hoadon 
            SET trang_thai = 'CANCELLED'
            WHERE id_hoadon = ? AND trang_thai = 'PENDING'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idHoaDon);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Hủy hóa đơn #" + idHoaDon);
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi hủy hóa đơn: " + e.getMessage());
        }
        return false;
    }

    // ==================== STATISTICS ====================

    /**
     * Tính tổng doanh thu theo ngày (chỉ đã thanh toán)
     */
    public BigDecimal getTongDoanhThuByDate(LocalDate date) {
        String sql = """
            SELECT COALESCE(SUM(tong_tien), 0) 
            FROM hoadon 
            WHERE DATE(ngay_thanh_toan) = ? AND trang_thai = 'PAID'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tính doanh thu: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Tính tổng doanh thu nhân viên theo ngày
     */
    public BigDecimal getTongDoanhThuNhanVien(int idNhanVien, LocalDate date) {
        String sql = """
            SELECT COALESCE(SUM(tong_tien), 0) 
            FROM hoadon 
            WHERE id_nhanvien = ? 
              AND DATE(ngay_thanh_toan) = ? 
              AND trang_thai = 'PAID'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idNhanVien);
            pstmt.setDate(2, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tính doanh thu: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Đếm số hóa đơn đã thanh toán theo ngày
     */
    public int countHoaDonByDate(LocalDate date) {
        String sql = """
            SELECT COUNT(*) 
            FROM hoadon 
            WHERE DATE(ngay_thanh_toan) = ? AND trang_thai = 'PAID'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi đếm hóa đơn: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Đếm số hóa đơn nhân viên theo ngày
     */
    public int countHoaDonNhanVien(int idNhanVien, LocalDate date) {
        String sql = """
            SELECT COUNT(*) 
            FROM hoadon 
            WHERE id_nhanvien = ? 
              AND DATE(ngay_thanh_toan) = ? 
              AND trang_thai = 'PAID'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idNhanVien);
            pstmt.setDate(2, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi đếm hóa đơn: " + e.getMessage());
        }
        return 0;
    }

    // ==================== HELPER METHOD ====================

    /**
     * Trích xuất HoaDon từ ResultSet
     */
    private HoaDon extractHoaDonFromResultSet(ResultSet rs) throws SQLException {
        // Parse trạng thái
        TrangThai trangThai = TrangThai.PENDING;
        try {
            String ttStr = rs.getString("trang_thai");
            if (ttStr != null) {
                trangThai = TrangThai.valueOf(ttStr);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Trạng thái không hợp lệ: " + rs.getString("trang_thai"));
        }

        // Parse ngày thanh toán
        Timestamp tsThanhToan = rs.getTimestamp("ngay_thanh_toan");
        LocalDateTime ngayThanhToan = tsThanhToan != null ? tsThanhToan.toLocalDateTime() : null;

        // Parse ngày cập nhật
        Timestamp tsCapNhat = rs.getTimestamp("ngay_cap_nhat");
        LocalDateTime ngayCapNhat = tsCapNhat != null ? tsCapNhat.toLocalDateTime() : null;

        return new HoaDon(
                rs.getInt("id_hoadon"),
                rs.getInt("id_nhanvien"),
                rs.getTimestamp("ngay_tao").toLocalDateTime(),
                rs.getBigDecimal("tong_tien"),
                trangThai,
                rs.getString("ghi_chu"),
                ngayThanhToan,
                ngayCapNhat
        );
    }
}