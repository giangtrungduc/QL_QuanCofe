package com.example.qlquancoffe;

import com.example.qlquancoffe.dao.DanhMucDAO;
import com.example.qlquancoffe.models.DanhMuc;
import com.example.qlquancoffe.utils.DatabaseConnection;
import com.example.qlquancoffe.utils.SceneSwitcher;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            System.out.println("Khởi động ứng dụng Quản lý Quán Cà Phê");

            System.out.println("Kiểm tra kết nối database...");
            if(!DatabaseConnection.testConnection()) {
                System.err.println("❌ KHÔNG THỂ KẾT NỐI DATABASE!");

                SceneSwitcher.showErrorAlert(
                        "Lỗi kết nối Database",
                        "Không thể kết nối đến database.\n" +
                                "Vui lòng kiểm tra MySQL và thử lại."
                );

                System.exit(1);
                return;
            }
            System.out.println("✅ Kết nối database thành công!\n");

            // Thiết lập Stage chính
            SceneSwitcher.setPrimaryStage(primaryStage);

            // Cấu hình Stage
            primaryStage.setTitle("Quản lý Quán Cà Phê");
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(650);

            // Chuyển sang màn hình đăng nhập
            System.out.println("🔐 Chuyển sang màn hình đăng nhập...");
            SceneSwitcher.switchToLogin();

            System.out.println("✅ Ứng dụng đã khởi động thành công!\n");
        } catch (Exception e) {
            System.err.println("❌ LỖI KHỞI ĐỘNG ỨNG DỤNG:");
            e.printStackTrace();

            SceneSwitcher.showErrorAlert(
                    "Lỗi khởi động",
                    "Đã xảy ra lỗi khi khởi động ứng dụng:\n" + e.getMessage()
            );

            System.exit(1);
        }
    }

    @Override
    public void stop() {
        System.out.println("Đóng ứng dụng...");

        // Đóng kết nối database
        DatabaseConnection.closeDataSource();

        System.out.println("Ứng dụng đã đóng");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
