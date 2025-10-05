package com.example.foodhelp.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.example.foodhelp.backend.entidades.Receta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;


@SpringBootApplication
public class FoodhelpBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodhelpBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner pruebaPrimeraConexion() {
		return args -> {
			System.out.println(probarConexionSQL());

			System.out.println(" prueba recetas ");

			try{
				List<Receta> recetas = obtenerTodasLasRecetas();

				if(recetas.isEmpty()){
					System.out.println("No hay recetas");
				}else{
					recetas.forEach(receta -> {
						System.out.println("Recetas encontradas " + receta.toString());
					});

				}
			}catch (Exception e){
				System.out.println("Error al obtener recetas: " + e.getMessage());
			}
		};
	}

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
