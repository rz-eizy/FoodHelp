package com.example.foodhelp.backend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.example.foodhelp.backend.entidades.Receta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;


@Service
public class Conexion {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private RepositorioReceta repositorioReceta;



    public String probarConexionSQL(){
        try (Connection connection = dataSource.getConnection()) {
            return "Conexión exitosa a la base de datos" + connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            return "Error al conectar a la base de datos: " + e.getMessage();
        }

    }

    public List<Receta> obtenerTodasLasRecetas() {
        return repositorioReceta.findAll();
    }

}
