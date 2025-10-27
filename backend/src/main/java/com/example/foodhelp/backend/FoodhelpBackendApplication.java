package com.example.foodhelp.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.example.foodhelp.backend.controladores.ControladorReceta;
import com.example.foodhelp.backend.entidades.Receta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

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

			pruebaBusquedaPorCategoria();

		};
	}

	private void pruebaBusquedaPorNombre() {

		System.out.println("Prueba de busqueda por nombre :");


		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("ingresa el nombre de la receta a buscar ");
			String nombrebusqueda = scanner.nextLine();

			System.out.println(" Ejecutando busqueda por termino: '" + nombrebusqueda );

			List<Receta> recetasEncontradas = controladorReceta.buscarRecetasPorNombre(nombrebusqueda);

			if (recetasEncontradas.isEmpty()) {
				System.out.println("No se encontraron recetas.");
			} else {
				System.out.println("Busqueda exitosa: Se encontraron " + recetasEncontradas.size() + " recetas.");
				recetasEncontradas.forEach(receta -> {
					System.out.println("  Resultado :  ID: " + receta.getId() + ", Nombre: " + receta.getNombre() + ", Descripcion" + receta.getDescripcion() );
				});
			}
		} catch (Exception e) {
			System.err.println("Error al realizar la busqueda: " + e.getMessage());
		}
	}

	private void pruebaBusquedaPorCategoria() {

		System.out.println("Prueba de busqueda por categoria :");


		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("ingresa el nombre de la categoria a buscar ");
			String categoriabusqueda = scanner.nextLine();

			System.out.println(" Ejecutando busqueda por termino: '" + categoriabusqueda );

			List<Receta> recetasEncontradas = controladorReceta.buscarRecetasPorCategoria(categoriabusqueda);

			if (recetasEncontradas.isEmpty()) {
				System.out.println("No se encontraron recetas.");
			} else {
				System.out.println("Busqueda exitosa: Se encontraron " + recetasEncontradas.size() + " recetas.");
				recetasEncontradas.forEach(receta -> {
					System.out.println("  Resultado :  ID: " + receta.getId() + ", Nombre: " + receta.getNombre() + ", Descripcion" + receta.getDescripcion() );
				});
			}
		} catch (Exception e) {
			System.err.println("Error al realizar la busqueda: " + e.getMessage());
		}
	}


	@Autowired
	private DataSource dataSource;

	@Autowired
	private RepositorioReceta repositorioReceta;

	@Autowired
	private ControladorReceta controladorReceta;



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
