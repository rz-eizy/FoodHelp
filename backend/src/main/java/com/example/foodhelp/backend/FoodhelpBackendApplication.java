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
}
