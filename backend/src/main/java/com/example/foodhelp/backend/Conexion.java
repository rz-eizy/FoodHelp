package com.example.foodhelp.backend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@Service
public class Conexion {
    @Autowired
    private DataSource dataSource;

    public String probarConexionSQL(){
        try (Connection connection = dataSource.getConnection()) {
            return "Conexión exitosa a la base de datos" + connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            return "Error al conectar a la base de datos: " + e.getMessage();
        }

    }

}
