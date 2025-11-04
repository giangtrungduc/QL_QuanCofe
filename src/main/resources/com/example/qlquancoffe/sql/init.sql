-- ==================== TẠO DATABASE ====================
DROP DATABASE IF EXISTS qlquancoffe;
CREATE DATABASE qlquancoffe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qlquancoffe;

-- ==================== XÓA BẢNG CŨ ====================
DROP TABLE IF EXISTS chitiethoadon;
DROP TABLE IF EXISTS hoadon;
DROP TABLE IF EXISTS sanpham;
DROP TABLE IF EXISTS taikhoan;
DROP TABLE IF EXISTS danhmuc;

-- ==================== BẢNG DANH MỤC (ĐƠN GIẢN) ====================
CREATE TABLE danhmuc (
                         id_danhmuc INT AUTO_INCREMENT,
                         ten_danhmuc VARCHAR(100) NOT NULL UNIQUE,
                         PRIMARY KEY(id_danhmuc),
                         INDEX idx_ten_danhmuc (ten_danhmuc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng danh mục sản phẩm';

-- ==================== BẢNG TÀI KHOẢN (ĐÃ ĐƠN GIẢN HÓA) ====================
CREATE TABLE taikhoan (
                          id_nhanvien INT AUTO_INCREMENT,
                          ho_ten VARCHAR(100) NOT NULL,
                          ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
                          mat_khau VARCHAR(255) NOT NULL,
                          vai_tro ENUM('QuanLy', 'NhanVien') NOT NULL DEFAULT 'NhanVien',
                          PRIMARY KEY(id_nhanvien),
                          UNIQUE INDEX idx_ten_dang_nhap (ten_dang_nhap),
                          INDEX idx_vai_tro (vai_tro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng tài khoản nhân viên (đơn giản)';

-- ==================== BẢNG SẢN PHẨM (ĐÃ ĐƠN GIẢN HÓA) ====================
CREATE TABLE sanpham (
                         id_sanpham INT AUTO_INCREMENT,
                         ten_sanpham VARCHAR(150) NOT NULL,
                         gia_ban DECIMAL(10, 0) NOT NULL CHECK (gia_ban >= 0),
                         so_luong_ton_kho INT NOT NULL DEFAULT 0 CHECK (so_luong_ton_kho >= 0),
                         id_danhmuc INT NOT NULL,
                         trang_thai ENUM('ConHang', 'HetHang', 'NgungKinhDoanh') NOT NULL DEFAULT 'ConHang',
                         PRIMARY KEY(id_sanpham),
                         INDEX idx_danhmuc (id_danhmuc),
                         INDEX idx_trang_thai (trang_thai),
                         INDEX idx_ten_sanpham (ten_sanpham),
                         FOREIGN KEY (id_danhmuc)
                             REFERENCES danhmuc(id_danhmuc)
                             ON DELETE RESTRICT
                             ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng sản phẩm (đơn giản)';

-- ==================== BẢNG HÓA ĐƠN (ĐÃ ĐIỀU CHỈNH) ====================
CREATE TABLE hoadon (
                        id_hoadon INT AUTO_INCREMENT,
                        id_nhanvien INT NOT NULL,
                        ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        tong_tien DECIMAL(12, 0) NOT NULL DEFAULT 0 CHECK (tong_tien >= 0),
                        trang_thai ENUM('PENDING', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
                        ghi_chu TEXT,
                        ngay_thanh_toan TIMESTAMP NULL,
                        ngay_cap_nhat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY(id_hoadon),
                        INDEX idx_nhanvien (id_nhanvien),
                        INDEX idx_ngay_tao (ngay_tao),
                        INDEX idx_trang_thai (trang_thai),
                        FOREIGN KEY (id_nhanvien)
                            REFERENCES taikhoan(id_nhanvien)
                            ON DELETE RESTRICT
                            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng hóa đơn (đã bỏ tiền khách đưa/thừa)';

-- ==================== BẢNG CHI TIẾT HÓA ĐƠN (ĐẦY ĐỦ) ====================
CREATE TABLE chitiethoadon (
                               id_chitiethoadon INT AUTO_INCREMENT,
                               id_hoadon INT NOT NULL,
                               id_sanpham INT NOT NULL,
                               ten_sanpham VARCHAR(150) NOT NULL,
                               so_luong INT NOT NULL CHECK (so_luong > 0),
                               don_gia DECIMAL(10, 0) NOT NULL CHECK (don_gia >= 0),
                               thanh_tien DECIMAL(12, 0) NOT NULL CHECK (thanh_tien >= 0),
                               ghi_chu VARCHAR(255),
                               PRIMARY KEY (id_chitiethoadon),
                               UNIQUE INDEX idx_unique_hoadon_sanpham (id_hoadon, id_sanpham),
                               INDEX idx_hoadon (id_hoadon),
                               INDEX idx_sanpham (id_sanpham),
                               FOREIGN KEY (id_hoadon)
                                   REFERENCES hoadon(id_hoadon)
                                   ON DELETE CASCADE
                                   ON UPDATE CASCADE,
                               FOREIGN KEY (id_sanpham)
                                   REFERENCES sanpham(id_sanpham)
                                   ON DELETE RESTRICT
                                   ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng chi tiết hóa đơn';

-- ==================== TRIGGER TỰ ĐỘNG (Vẫn cần thiết cho tồn kho) ====================

DELIMITER $$

-- Trigger sau khi thêm chi tiết hóa đơn
CREATE TRIGGER trg_after_insert_chitiethoadon
    AFTER INSERT ON chitiethoadon
    FOR EACH ROW
BEGIN
    UPDATE sanpham
    SET so_luong_ton_kho = so_luong_ton_kho - NEW.so_luong
    WHERE id_sanpham = NEW.id_sanpham;

    UPDATE sanpham
    SET trang_thai = 'HetHang'
    WHERE id_sanpham = NEW.id_sanpham AND so_luong_ton_kho <= 0;
    END$$

    -- Trigger sau khi cập nhật chi tiết hóa đơn
    CREATE TRIGGER trg_after_update_chitiethoadon
        AFTER UPDATE ON chitiethoadon
        FOR EACH ROW
    BEGIN
        UPDATE sanpham
        SET so_luong_ton_kho = so_luong_ton_kho + OLD.so_luong
        WHERE id_sanpham = OLD.id_sanpham;

        UPDATE sanpham
        SET so_luong_ton_kho = so_luong_ton_kho - NEW.so_luong
        WHERE id_sanpham = NEW.id_sanpham;

        UPDATE sanpham
        SET trang_thai = CASE
                             WHEN so_luong_ton_kho <= 0 THEN 'HetHang'
                             ELSE 'ConHang'
            END
        WHERE id_sanpham IN (OLD.id_sanpham, NEW.id_sanpham);
        END$$

        -- Trigger sau khi xóa chi tiết hóa đơn
        CREATE TRIGGER trg_after_delete_chitiethoadon
            AFTER DELETE ON chitiethoadon
            FOR EACH ROW
        BEGIN
            UPDATE sanpham
            SET so_luong_ton_kho = so_luong_ton_kho + OLD.so_luong,
                trang_thai = CASE
                                 WHEN so_luong_ton_kho + OLD.so_luong > 0 THEN 'ConHang'
                                 ELSE trang_thai
                    END
            WHERE id_sanpham = OLD.id_sanpham;
            END$$

            -- Trigger tự động cập nhật tổng tiền hóa đơn
            CREATE TRIGGER trg_after_insert_chitiethoadon_update_total
                AFTER INSERT ON chitiethoadon
                FOR EACH ROW
            BEGIN
                UPDATE hoadon
                SET tong_tien = (
                    SELECT COALESCE(SUM(thanh_tien), 0)
                    FROM chitiethoadon
                    WHERE id_hoadon = NEW.id_hoadon
                )
                WHERE id_hoadon = NEW.id_hoadon;
                END$$

                CREATE TRIGGER trg_after_update_chitiethoadon_update_total
                    AFTER UPDATE ON chitiethoadon
                    FOR EACH ROW
                BEGIN
                    UPDATE hoadon
                    SET tong_tien = (
                        SELECT COALESCE(SUM(thanh_tien), 0)
                        FROM chitiethoadon
                        WHERE id_hoadon = NEW.id_hoadon
                    )
                    WHERE id_hoadon = NEW.id_hoadon;
                    END$$

                    CREATE TRIGGER trg_after_delete_chitiethoadon_update_total
                        AFTER DELETE ON chitiethoadon
                        FOR EACH ROW
                    BEGIN
                        UPDATE hoadon
                        SET tong_tien = (
                            SELECT COALESCE(SUM(thanh_tien), 0)
                            FROM chitiethoadon
                            WHERE id_hoadon = OLD.id_hoadon
                        )
                        WHERE id_hoadon = OLD.id_hoadon;
                        END$$

                        -- Trigger cập nhật ngày thanh toán
                        CREATE TRIGGER trg_before_update_hoadon_set_payment_date
                            BEFORE UPDATE ON hoadon
                            FOR EACH ROW
                        BEGIN
                            IF NEW.trang_thai = 'PAID' AND OLD.trang_thai != 'PAID' THEN
        SET NEW.ngay_thanh_toan = CURRENT_TIMESTAMP;
                        END IF;
                        END$$

                        DELIMITER ;

-- ==================== DỮ LIỆU MẪU (ĐÃ ĐIỀU CHỈNH) ====================

-- Danh mục
                        INSERT INTO danhmuc (ten_danhmuc) VALUES
                                                              ('Cà phê'),
                                                              ('Trà'),
                                                              ('Sinh tố'),
                                                              ('Bánh ngọt'),
                                                              ('Đồ ăn nhẹ'),
                                                              ('Nước ép');

-- Tài khoản (Mật khẩu: admin123)
                        INSERT INTO taikhoan (ho_ten, ten_dang_nhap, mat_khau, vai_tro) VALUES
                                                                                            ('Nguyễn Văn Admin', 'admin', '$2a$10$xtOLbyDmopPQGgoYZCWtVOV9gPo4UkB4VhANd6VxrXkbVobTIQUVa', 'QuanLy'),
                                                                                            ('Trần Thị Bình', 'nhanvien1', '$2a$10$xtOLbyDmopPQGgoYZCWtVOV9gPo4UkB4VhANd6VxrXkbVobTIQUVa', 'NhanVien'),
                                                                                            ('Lê Văn Cường', 'nhanvien2', '$2a$10$xtOLbyDmopPQGgoYZCWtVOV9gPo4UkB4VhANd6VxrXkbVobTIQUVa', 'NhanVien');

-- Sản phẩm
                        INSERT INTO sanpham (ten_sanpham, gia_ban, so_luong_ton_kho, id_danhmuc) VALUES
-- Cà phê (ID: 1-5)
('Cà phê đen đá', 25000, 100, 1),
('Cà phê sữa đá', 30000, 100, 1),
('Bạc xỉu', 30000, 80, 1),
('Cappuccino', 45000, 50, 1),
('Espresso', 35000, 60, 1),

-- Trà (ID: 6-8)
('Trà đào cam sả', 35000, 50, 2),
('Trà sữa trân châu', 40000, 60, 2),
('Trà xanh matcha', 50000, 40, 2),

-- Sinh tố (ID: 9-10)
('Sinh tố bơ', 45000, 30, 3),
('Sinh tố dâu', 42000, 25, 3),

-- Bánh ngọt (ID: 11-12)
('Bánh croissant', 25000, 20, 4),
('Bánh tiramisu', 55000, 15, 4),

-- Đồ ăn nhẹ (ID: 13-14)
('Khoai tây chiên', 30000, 40, 5),
('Gà popcorn', 40000, 35, 5),

-- Nước ép (ID: 15-16)
('Nước ép cam', 35000, 30, 6),
('Nước ép dưa hấu', 30000, 25, 6);

                        -- ==================== DỮ LIỆU MẪU HÓA ĐƠN & CHI TIẾT (MỚI) ====================

-- Hóa đơn 1 (PAID) - Tạo bởi Nhân viên 2 (Trần Thị Bình)
                        INSERT INTO hoadon (id_nhanvien, ngay_tao, trang_thai, ghi_chu, ngay_thanh_toan) VALUES
                            (2, '2025-11-01 10:30:00', 'PAID', 'Khách quen', '2025-11-01 10:31:00');

-- Chi tiết cho Hóa đơn 1 (ID tự tăng = 1)
                        INSERT INTO chitiethoadon (id_hoadon, id_sanpham, ten_sanpham, so_luong, don_gia, thanh_tien) VALUES
                                                                                                                          (1, 1, 'Cà phê đen đá', 2, 25000, 50000),
                                                                                                                          (1, 11, 'Bánh croissant', 1, 25000, 25000);
                        -- Trigger sẽ cập nhật hoadon (ID=1) tong_tien = 75000
-- Trigger sẽ cập nhật sanpham (ID=1) ton_kho = 98, (ID=11) ton_kho = 19

-- Hóa đơn 2 (PAID) - Tạo bởi Nhân viên 3 (Lê Văn Cường)
                        INSERT INTO hoadon (id_nhanvien, ngay_tao, trang_thai, ghi_chu, ngay_thanh_toan) VALUES
                            (3, '2025-11-02 14:00:00', 'PAID', 'Mang đi', '2025-11-02 14:01:30');

-- Chi tiết cho Hóa đơn 2 (ID tự tăng = 2)
                        INSERT INTO chitiethoadon (id_hoadon, id_sanpham, ten_sanpham, so_luong, don_gia, thanh_tien) VALUES
                                                                                                                          (2, 6, 'Trà đào cam sả', 1, 35000, 35000),
                                                                                                                          (2, 9, 'Sinh tố bơ', 1, 45000, 45000);
                        -- Trigger sẽ cập nhật hoadon (ID=2) tong_tien = 80000
-- Trigger sẽ cập nhật sanpham (ID=6) ton_kho = 49, (ID=9) ton_kho = 29

-- Hóa đơn 3 (PENDING) - Tạo bởi Nhân viên 2 (Trần Thị Bình)
                        INSERT INTO hoadon (id_nhanvien, trang_thai, ghi_chu) VALUES
                            (2, 'PENDING', 'Bàn 5'); -- ngay_tao sẽ là CURRENT_TIMESTAMP

-- Chi tiết cho Hóa đơn 3 (ID tự tăng = 3)
                        INSERT INTO chitiethoadon (id_hoadon, id_sanpham, ten_sanpham, so_luong, don_gia, thanh_tien) VALUES
                            (3, 2, 'Cà phê sữa đá', 3, 30000, 90000);
                        -- Trigger sẽ cập nhật hoadon (ID=3) tong_tien = 90000
-- Trigger sẽ cập nhật sanpham (ID=2) ton_kho = 97

-- ==================== KIỂM TRA DỮ LIỆU MỚI ====================
                        SELECT '=== TÀI KHOẢN ===' AS '';
                        SELECT id_nhanvien, ho_ten, ten_dang_nhap, vai_tro FROM taikhoan;

                        SELECT '=== SẢN PHẨM (SAU KHI BÁN HÀNG) ===' AS '';
                        SELECT id_sanpham, ten_sanpham, gia_ban, so_luong_ton_kho, trang_thai
                        FROM sanpham
                        WHERE id_sanpham IN (1, 2, 6, 9, 11); -- Kiểm tra các sản phẩm đã bán

                        SELECT '=== HÓA ĐƠN (MỚI) ===' AS '';
                        SELECT h.id_hoadon, h.ngay_tao, h.tong_tien, h.trang_thai, t.ho_ten AS NhanVienTao
                        FROM hoadon h
                                 JOIN taikhoan t ON h.id_nhanvien = t.id_nhanvien;

                        SELECT '=== CHI TIẾT HÓA ĐƠN (MỚI) ===' AS '';
                        SELECT id_hoadon, ten_sanpham, so_luong, thanh_tien
                        FROM chitiethoadon;