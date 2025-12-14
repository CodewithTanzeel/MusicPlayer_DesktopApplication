module com.vibe {
    requires javafx.controls;
    requires javafx.media;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.vibe to javafx.fxml;
    opens com.vibe.model to javafx.base; // For TableView PropertyValueFactory
    
    exports com.vibe;
    exports com.vibe.ui;
}
