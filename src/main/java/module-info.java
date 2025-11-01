module com.example.qlquancoffe {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires jbcrypt;
    requires com.zaxxer.hikari;

    opens com.example.qlquancoffe to javafx.fxml;
    opens com.example.qlquancoffe.controllers to javafx.fxml;
    opens com.example.qlquancoffe.models to javafx.fxml;
    exports com.example.qlquancoffe;
    exports com.example.qlquancoffe.controllers;
    exports com.example.qlquancoffe.models;
}