module com.mycompany.torneodeportivo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.mariadb.jdbc;

    opens com.mycompany.torneodeportivo to javafx.fxml;
    opens com.mycompany.torneodeportivo.controlador to javafx.fxml;
    opens com.mycompany.torneodeportivo.modelo to javafx.base;
    opens com.mycompany.torneodeportivo.mysql.controladores to javafx.fxml;
    opens com.mycompany.torneodeportivo.mysql.modelos to javafx.base;

    exports com.mycompany.torneodeportivo;
}
