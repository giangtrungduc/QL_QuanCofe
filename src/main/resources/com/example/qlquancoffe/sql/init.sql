CREATE DATABASE qlquancoffe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE qlquancoffe;

-- XÓA BẢNG CŨ (theo thứ tự đúng)
DROP TABLE IF EXISTS ChiTietHoaDon;
DROP TABLE IF EXISTS HoaDon;
DROP TABLE IF EXISTS SanPham;
DROP TABLE IF EXISTS TaiKhoan;
DROP TABLE IF EXISTS DanhMuc;

-- TẠO LẠI VỚI TÊN CHỮ THƯỜNG
CREATE TABLE danhmuc (
                         id_danhmuc INT AUTO_INCREMENT,
                         ten_danhmuc VARCHAR(100) NOT NULL UNIQUE,
                         PRIMARY KEY(id_danhmuc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE taikhoan (
                          id_nhanvien INT AUTO_INCREMENT,
                          ho_ten VARCHAR(100) NOT NULL,
                          ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
                          mat_khau VARCHAR(255) NOT NULL,
                          vai_tro ENUM('QuanLy', 'NhanVien') NOT NULL,
                          trang_thai ENUM('DangLamViec', 'DaNghiViec') NOT NULL DEFAULT 'DangLamViec',
                          ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY(id_nhanvien),
                          INDEX idx_ten_dang_nhap (ten_dang_nhap),
                          INDEX idx_trang_thai (trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sanpham (
                         id_sanpham INT AUTO_INCREMENT,
                         ten_sanpham VARCHAR(150) NOT NULL,
                         gia_ban DECIMAL(10, 0) NOT NULL CHECK (gia_ban >= 0),
                         so_luong_ton_kho INT NOT NULL DEFAULT 0 CHECK (so_luong_ton_kho >= 0),
                         anh_san_pham VARCHAR(255),
                         id_danhmuc INT NOT NULL,
                         trang_thai ENUM('ConHang', 'HetHang') NOT NULL DEFAULT 'ConHang',
                         ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         ngay_cap_nhat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY(id_sanpham),
                         INDEX idx_danhmuc (id_danhmuc),
                         INDEX idx_trang_thai (trang_thai),
                         FOREIGN KEY (id_danhmuc)
                             REFERENCES danhmuc(id_danhmuc)
                             ON DELETE RESTRICT
                             ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE hoadon (
                        id_hoadon INT AUTO_INCREMENT,
                        id_nhanvien INT NOT NULL,
                        ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        tong_tien DECIMAL(12, 0) NOT NULL DEFAULT 0 CHECK (tong_tien >= 0),
                        ghi_chu TEXT,
                        PRIMARY KEY(id_hoadon),
                        INDEX idx_nhanvien (id_nhanvien),
                        INDEX idx_ngay_tao (ngay_tao),
                        FOREIGN KEY (id_nhanvien)
                            REFERENCES taikhoan(id_nhanvien)
                            ON DELETE RESTRICT
                            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chitiethoadon (
                               id_hoadon INT NOT NULL,
                               id_sanpham INT NOT NULL,
                               so_luong INT NOT NULL CHECK (so_luong > 0),
                               don_gia DECIMAL(10, 0) NOT NULL CHECK (don_gia >= 0),
                               thanh_tien DECIMAL(12, 0) NOT NULL CHECK (thanh_tien >= 0),
                               PRIMARY KEY (id_hoadon, id_sanpham),
                               INDEX idx_sanpham (id_sanpham),
                               FOREIGN KEY (id_hoadon)
                                   REFERENCES hoadon(id_hoadon)
                                   ON DELETE CASCADE
                                   ON UPDATE CASCADE,
                               FOREIGN KEY (id_sanpham)
                                   REFERENCES sanpham(id_sanpham)
                                   ON DELETE RESTRICT
                                   ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- THÊM DỮ LIỆU MẪU
INSERT INTO danhmuc (ten_danhmuc) VALUES
                                      ('Cà phê'), ('Trà'), ('Sinh tố'), ('Bánh ngọt'), ('Đồ ăn nhẹ');

-- Mật khẩu: admin123
INSERT INTO taikhoan (ho_ten, ten_dang_nhap, mat_khau, vai_tro) VALUES
                                                                    ('Nguyễn Văn A', 'admin', '$2a$10$AFR42wwoT4nWzCkB.d8EBesN1qWpQTaAht9t2S5AYnPnbe/eY41pi', 'QuanLy'),
                                                                    ('Trần Thị B', 'nhanvien', '$2a$10$AFR42wwoT4nWzCkB.d8EBesN1qWpQTaAht9t2S5AYnPnbe/eY41pi', 'NhanVien');

INSERT INTO sanpham (ten_sanpham, gia_ban, so_luong_ton_kho, anh_san_pham, id_danhmuc) VALUES
                                                                                           ('Cà phê đen đá', 25000, 100, '/images/caphe-den.jpg', 1),
                                                                                           ('Cà phê sữa đá', 30000, 100, '/images/caphe-sua.jpg', 1),
                                                                                           ('Bạc xỉu', 30000, 80, '/images/bac-xiu.jpg', 1),
                                                                                           ('Trà đào cam sả', 35000, 50, '/images/tra-dao-cam-sa.jpg', 2),
                                                                                           ('Trà sữa trân châu', 40000, 60, '/images/tra-sua.jpg', 2),
                                                                                           ('Sinh tố bơ', 45000, 30, '/images/sinh-to-bo.jpg', 3),
                                                                                           ('Bánh croissant', 25000, 20, '/images/banh-croissant.jpg', 4);
INSERT INTO hoadon (id_nhanvien, ngay_tao, tong_tien, ghi_chu) VALUES
    (2, '2025-11-4 8:15:00', 190000, 'Khách mang đi');

INSERT INTO chitiethoadon (id_hoadon, id_sanpham, so_luong, don_gia, thanh_tien) VALUES
                                                                                     (2, 5, 3, 40000, 120000),  -- 3x Trà sữa trân châu
                                                                                     (2, 4, 2, 35000, 70000);   -- 2x Trà đào cam sả

UPDATE hoadon SET tong_tien = (SELECT SUM(thanh_tien) FROM chitiethoadon WHERE id_hoadon = 2) WHERE id_hoadon = 2;


-- KIỂM TRA
SHOW TABLES;
SELECT * FROM danhmuc;
