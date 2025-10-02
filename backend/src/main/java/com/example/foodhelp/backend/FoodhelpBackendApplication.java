package com.example.foodhelp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class FoodhelpBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodhelpBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner pruebaPrimeraConexion(Conexion conexion) {
		return args -> {
			System.out.println(conexion.probarConexionSQL());
		};
	}

}
