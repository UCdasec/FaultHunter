module com.afivd.afivd {
    requires javafx.controls;
    requires javafx.fxml;
    requires antlr;
    requires java.desktop;


    opens com.afivd.afivd to javafx.fxml;
    exports com.afivd.afivd;
}