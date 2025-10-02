package com.example.foodhelp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.example.foodhelp.backend.entidades.Receta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;

import java.util.List;


@SpringBootApplication
public class FoodhelpBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodhelpBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner pruebaPrimeraConexion(Conexion conexion) {
		return args -> {
			System.out.println(conexion.probarConexionSQL());

			System.out.println(" prueba recetas ");

			try{
				List<Receta> recetas = conexion.obtenerTodasLasRecetas();

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




}
