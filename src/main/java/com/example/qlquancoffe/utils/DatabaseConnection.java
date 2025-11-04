package com.example.qlquancoffe.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối đến MySQL Database
 * Sử dụng HikariCP Connection Pool để tối ưu hiệu suất
 * Hỗ trợ cả LOCALHOST và CLOUD
 */
public class DatabaseConnection {

    // ===================== CHỌN MÔI TRƯỜNG =====================
    // ⚠️ THAY ĐỔI GIÁ TRỊ NÀY ĐỂ CHUYỂN ĐỔI
    private static final boolean USE_LOCALHOST = true;  // true = localhost, false = cloud

    // ===================== CẤU HÌNH LOCALHOST =====================
    private static final String LOCAL_HOST = "localhost";
    private static final String LOCAL_PORT = "3306";
    private static final String LOCAL_DB_NAME = "qlquancoffe"; // Tên database của bạn
    private static final String LOCAL_USER = "root";
    private static final String LOCAL_PASSWORD = "duc123"; // Mật khẩu MySQL local của bạn

    // ===================== CẤU HÌNH CLOUD (Railway) =====================
    private static final String CLOUD_HOST = "maglev.proxy.rlwy.net";
    private static final String CLOUD_PORT = "25382";
    private static final String CLOUD_DB_NAME = "railway";
    private static final String CLOUD_USER = "root";
    private static final String CLOUD_PASSWORD = "aUKfugtuQBefRjogUvVEyRAARDfbqqts";

    // ===================== URL KẾT NỐI (TỰ ĐỘNG) =====================
    private static final String DB_URL = USE_LOCALHOST
            ? String.format("jdbc:mysql://%s:%s/%s", LOCAL_HOST, LOCAL_PORT, LOCAL_DB_NAME)
            : String.format("jdbc:mysql://%s:%s/%s", CLOUD_HOST, CLOUD_PORT, CLOUD_DB_NAME);

    private static final String DB_USER = USE_LOCALHOST ? LOCAL_USER : CLOUD_USER;
    private static final String DB_PASSWORD = USE_LOCALHOST ? LOCAL_PASSWORD : CLOUD_PASSWORD;

    // ===================== HIKARICP DATASOURCE =====================
    private static HikariDataSource dataSource;

    static {
        try {
            setupDataSource();
            System.out.println("✅ HikariCP Connection Pool đã được khởi tạo");
            System.out.println("🌍 Môi trường: " + (USE_LOCALHOST ? "LOCALHOST" : "CLOUD"));
            System.out.println("📡 Kết nối tới: " + DB_URL);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khởi tạo Connection Pool: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cấu hình HikariCP DataSource
     */
    private static void setupDataSource() {
        HikariConfig config = new HikariConfig();

        // ===== CẤU HÌNH CƠ BẢN =====
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // ===== CẤU HÌNH CONNECTION POOL =====
        if (USE_LOCALHOST) {
            // Localhost: Ít connections, timeout ngắn
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000);      // 10s
            config.setIdleTimeout(300000);           // 5 phút
            config.setMaxLifetime(600000);           // 10 phút
        } else {
            // Cloud: Nhiều connections hơn, timeout dài hơn
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);      // 30s
            config.setIdleTimeout(600000);           // 10 phút
            config.setMaxLifetime(1800000);          // 30 phút
        }

        // ===== CẤU HÌNH MYSQL =====
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // ===== CẤU HÌNH CHARSET & TIMEZONE =====
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("serverTimezone", "Asia/Ho_Chi_Minh");
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("allowPublicKeyRetrieval", "true");

        // ===== TÊN POOL =====
        config.setPoolName("QLQuanCoffee-Pool-" + (USE_LOCALHOST ? "LOCAL" : "CLOUD"));

        // ===== HEALTH CHECK =====
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Lấy connection từ pool
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource chưa được khởi tạo!");
        }

        try {
            Connection conn = dataSource.getConnection();
            System.out.println("✅ Đã lấy connection từ pool (Active: " +
                    dataSource.getHikariPoolMXBean().getActiveConnections() + "/" +
                    dataSource.getHikariPoolMXBean().getTotalConnections() + ")");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy connection từ pool: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Đóng connection pool
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("🔒 Đã đóng HikariCP Connection Pool");
        }
    }

    /**
     * Kiểm tra connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Test connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * In thông tin Pool
     */
    public static void printPoolStats() {
        if (dataSource != null) {
            System.out.println("\n📊 THỐNG KÊ CONNECTION POOL:");
            System.out.println("   Môi trường: " + (USE_LOCALHOST ? "LOCALHOST" : "CLOUD"));
            System.out.println("   Active Connections: " +
                    dataSource.getHikariPoolMXBean().getActiveConnections());
            System.out.println("   Idle Connections: " +
                    dataSource.getHikariPoolMXBean().getIdleConnections());
            System.out.println("   Total Connections: " +
                    dataSource.getHikariPoolMXBean().getTotalConnections());
            System.out.println("   Threads Waiting: " +
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
    }

    /**
     * In thông tin database
     */
    public static void printDatabaseInfo() {
        try (Connection conn = getConnection()) {
            System.out.println("\n📊 THÔNG TIN DATABASE:");
            System.out.println("   Môi trường: " + (USE_LOCALHOST ? "🏠 LOCALHOST" : "☁️ CLOUD"));
            System.out.println("   Database: " + conn.getCatalog());
            System.out.println("   URL: " + conn.getMetaData().getURL());
            System.out.println("   User: " + conn.getMetaData().getUserName());
            System.out.println("   Driver: " + conn.getMetaData().getDriverName());
            System.out.println("   Driver Version: " + conn.getMetaData().getDriverVersion());

            System.out.println("\n📋 DANH SÁCH BẢNG:");
            ResultSet rs = conn.getMetaData().getTables(
                    null, null, "%", new String[]{"TABLE"}
            );
            while (rs.next()) {
                System.out.println("   - " + rs.getString("TABLE_NAME"));
            }
            rs.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy thông tin database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static int getActiveConnections() {
        return dataSource != null ?
                dataSource.getHikariPoolMXBean().getActiveConnections() : 0;
    }

    public static int getIdleConnections() {
        return dataSource != null ?
                dataSource.getHikariPoolMXBean().getIdleConnections() : 0;
    }

    public static boolean isPoolRunning() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Kiểm tra đang dùng môi trường nào
     */
    public static String getCurrentEnvironment() {
        return USE_LOCALHOST ? "LOCALHOST" : "CLOUD";
    }
}