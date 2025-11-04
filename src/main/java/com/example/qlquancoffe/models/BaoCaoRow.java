package com.example.qlquancoffe.models;

import com.example.qlquancoffe.utils.CurrencyUtil;
import javafx.beans.property.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Model đặc biệt dùng cho Báo cáo (TreeTableView)
 */
public class BaoCaoRow {
    private final StringProperty ten;
    private final IntegerProperty soLuong;
    private final ObjectProperty<BigDecimal> thanhTien;
    private final ObjectProperty<BigDecimal> donGiaTB; // Đơn giá trung bình

    // Thuộc tính để xác định cấp (Danh mục hay Sản phẩm)
    private final boolean isCategory;

    /**
     * Constructor cho SẢN PHẨM (lá)
     */
    public BaoCaoRow(String tenSP, int soLuong, BigDecimal thanhTien) {
        this.ten = new SimpleStringProperty(tenSP);
        this.soLuong = new SimpleIntegerProperty(soLuong);
        this.thanhTien = new SimpleObjectProperty<>(thanhTien);
        this.isCategory = false;

        // Tính đơn giá TB
        if (soLuong > 0) {
            this.donGiaTB = new SimpleObjectProperty<>(
                    thanhTien.divide(BigDecimal.valueOf(soLuong), 0, RoundingMode.HALF_UP)
            );
        } else {
            this.donGiaTB = new SimpleObjectProperty<>(BigDecimal.ZERO);
        }
    }

    /**
     * Constructor cho DANH MỤC (nhánh)
     */
    public BaoCaoRow(String tenDanhMuc) {
        this.ten = new SimpleStringProperty(tenDanhMuc);
        this.soLuong = new SimpleIntegerProperty(0);
        this.thanhTien = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.donGiaTB = new SimpleObjectProperty<>(BigDecimal.ZERO); // Sẽ tính toán lại sau
        this.isCategory = true;
    }

    /**
     * Cập nhật tổng cho danh mục
     */
    public void add(BaoCaoRow productRow) {
        this.soLuong.set(this.soLuong.get() + productRow.getSoLuong());
        this.thanhTien.set(this.thanhTien.get().add(productRow.getThanhTien()));

        // Tính lại đơn giá TB cho cả danh mục
        if (this.soLuong.get() > 0) {
            this.donGiaTB.set(
                    this.thanhTien.get().divide(BigDecimal.valueOf(this.soLuong.get()), 0, RoundingMode.HALF_UP)
            );
        }
    }

    // Getters và Properties
    public String getTen() { return ten.get(); }
    public StringProperty tenProperty() { return ten; }
    public int getSoLuong() { return soLuong.get(); }
    public IntegerProperty soLuongProperty() { return soLuong; }
    public BigDecimal getThanhTien() { return thanhTien.get(); }
    public ObjectProperty<BigDecimal> thanhTienProperty() { return thanhTien; }
    public BigDecimal getDonGiaTB() { return donGiaTB.get(); }
    public ObjectProperty<BigDecimal> donGiaTBProperty() { return donGiaTB; }
    public boolean isCategory() { return isCategory; }
}