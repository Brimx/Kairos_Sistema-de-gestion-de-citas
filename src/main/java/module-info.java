module co.edu.upc.citasmedicas {
    // Requerimos los módulos del sistema de JavaFX
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires java.naming;
    requires com.calendarfx.view;
    requires org.xerial.sqlitejdbc;

    // ABRIR los paquetes de vistas y controladores a JavaFX para que FXML pueda inyectar componentes
    opens co.edu.upc.citasmedicas to javafx.fxml;
    opens co.edu.upc.citasmedicas.controller to javafx.fxml;
    opens co.edu.upc.citasmedicas.view to javafx.fxml;
    
    // EXPORTAR los paquetes para que el motor de JavaFX pueda ejecutarlos
    exports co.edu.upc.citasmedicas;
    exports co.edu.upc.citasmedicas.controller;
    exports co.edu.upc.citasmedicas.model;
    exports co.edu.upc.citasmedicas.enums;
    exports co.edu.upc.citasmedicas.service;
    exports co.edu.upc.citasmedicas.dao;
    exports co.edu.upc.citasmedicas.view;
}
