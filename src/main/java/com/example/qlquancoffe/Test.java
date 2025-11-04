package com.example.qlquancoffe;

import com.example.qlquancoffe.dao.*;
import com.example.qlquancoffe.models.ChiTietHoaDon;
import com.example.qlquancoffe.models.DanhMuc;
import com.example.qlquancoffe.models.SanPham;
import com.example.qlquancoffe.models.TaiKhoan;
import com.example.qlquancoffe.utils.PasswordUtil;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Test {
    public static void main(String[] args) {
        System.out.println(PasswordUtil.hashPassword("admin123"));
    }
}
