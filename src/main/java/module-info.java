module com.example.ICETERIOD {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;


    opens com.example.iceteriod to javafx.fxml;
    exports com.example.iceteriod;
    exports com.example.Exception;
    exports com.example.iceteriod.model;
    opens com.example.iceteriod.model to javafx.fxml;
    opens com.example.iceteriod.view to javafx.fxml;
    exports com.example.iceteriod.view;
    exports com.example.iceteriod.controller;
    opens com.example.iceteriod.controller to javafx.fxml;

}