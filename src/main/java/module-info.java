/**
 * Provides GoannaWatch management functionality, including GoannaWatch creation,
 * login, and session tracking, along with the JavaFX UI controllers
 * that drive the application's screens.
 */
module GoannaWatch {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.apache.commons.validator;
    requires atlantafx.base;
    requires password4j;
    requires java.desktop;
    requires javafx.web;
    requires java.net.http;
    requires org.json;
    requires jdk.jsobject;
    requires org.apache.commons.csv;


    opens GoannaWatch to javafx.fxml;
    exports GoannaWatch;

    opens GoannaWatch.database to org.junit.platform.commons;

    exports GoannaWatch.account.controller;
    opens GoannaWatch.account.controller to javafx.fxml, org.junit.platform.commons;

    exports GoannaWatch.account.model;
    opens GoannaWatch.account.model to javafx.fxml, org.junit.platform.commons;

    exports GoannaWatch.observations.model;
    opens GoannaWatch.observations.model to javafx.fxml, org.junit.platform.commons;

    exports GoannaWatch.observations.controller;
    opens GoannaWatch.observations.controller to javafx.fxml, org.junit.platform.commons;
}