package com.example.foodhelp.backend;
import com.example.foodhelp.backend.controladores.ControladorReceta;
import com.example.foodhelp.backend.dto.RespuestaReceta;
import com.example.foodhelp.backend.entidades.Receta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

@SpringBootTest
class FoodhelpBackendApplicationTests {
	@Autowired
	private DataSource dataSource;
	@Autowired
	private RepositorioReceta repositorioReceta;
	@Autowired
	private ControladorReceta controladorReceta;

	@Test
	void contextLoads() {
	}

	@Test
	void pruebaConexionBaseDatos() throws Exception {
		System.out.println("PRUEBA DE CONEXION BASE DE DATOS");
		try (Connection connection = dataSource.getConnection()) {
			assertThat(connection).isNotNull();
			assertThat(connection.isValid(1)).isTrue();
			System.out.println("CONEXION EXITOSA A LA BASE DE DATOS: " + connection.getMetaData().getDatabaseProductName());
		}
	}

	@Test
	void pruebaObtenerTodasLasRecetas() {
		System.out.println("Prueba Obtener Todas Las Recetas");
		List<Receta> recetas = repositorioReceta.findAll();
		assertThat(recetas).isNotNull();
		if (recetas.isEmpty()) {
			System.out.println("No hay recetas en la Base De Gatos");
		} else {
			System.out.println("Se encontraron " + recetas.size() + " recetas");
			recetas.forEach(receta -> System.out.println
					("  -> " + receta.getNombre()));
		}
	}

	@Test
	void pruebaBuscarRecetaPorIdEncontrado() {
		System.out.println("Prueba busqueda por ID (exito)");
		// IMPORTANTE siempre hay que colocar un " idL" en vez de solo el id
		// para indicar que es un long y no un int IMPORTANTEEEE AAAAAAAAAA
		Long idBusqueda = 1L;
		System.out.println("Ejecutando busqueda por ID: " + idBusqueda);
		ResponseEntity<RespuestaReceta> respuesta = controladorReceta.buscarRecetaPorId(idBusqueda);
		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotNull();
		assertThat(respuesta.getBody().getId()).isEqualTo(idBusqueda);

		System.out.println("Busqueda exitosa. Receta encontrada: " + respuesta.getBody().getNombre());
	}

	@Test
	void pruebaBuscarRecetasPorNombre() {
		System.out.println("Prueba Buscar Recetas Por Nombre");
		String nombreBusqueda = "pollo";
		System.out.println("Ejecutando busqueda por nombre: '" + nombreBusqueda + "'");
		List<RespuestaReceta> recetasEncontradas = controladorReceta.buscarRecetasPorNombre(nombreBusqueda);
		assertThat(recetasEncontradas).isNotNull();
		if (recetasEncontradas.isEmpty()) {
			System.out.println("No se encontraron recetas con ese nombre");
		} else {
			System.out.println("Se encontraron las siguientes recetas : " + recetasEncontradas.size() + " recetas.");
			assertThat(recetasEncontradas.get(0).getNombre()).containsIgnoringCase(nombreBusqueda);
		}
	}

	@Test
	void pruebaBuscarRecetasPorCategoria() {
		System.out.println("Prueba busqueda de categoria");
		String categoriaBusqueda = "FRITURAS";
		System.out.println("Ejecutando busqueda por categoria: '" + categoriaBusqueda + "'");
		List<RespuestaReceta> recetasEncontradas = controladorReceta.buscarRecetasPorCategoria(categoriaBusqueda);
		assertThat(recetasEncontradas).isNotNull();
		if (recetasEncontradas.isEmpty()) {
			System.out.println("No se encontraron recetas en la categoria");
		} else {
			System.out.println("Lista de recetas encontradas:");
			recetasEncontradas.forEach(receta -> {
				System.out.println("  -> ID: " + receta.getId() + ", Nombre: " + receta.getNombre() + ", Descripcion: " + receta.getDescripcion());
			});
		}
	}

	@Test
	void pruebaBuscarPorCualquierIngrediente() {
		System.out.println("Prueba busqueda 'O' por ingredientes");
		List<String> ingredientesBusqueda = List.of("huevo", "choclo");
		System.out.println("Ejecutando busqueda por ingredientes: '" + ingredientesBusqueda + "'");
		List<RespuestaReceta> recetasEncontradas = controladorReceta.buscarPorCualquierIngrediente(ingredientesBusqueda);
		assertThat(recetasEncontradas).isNotNull();
		if (recetasEncontradas.isEmpty()) {
			System.out.println("No se encontraron recetas con esos ingredientes.");
		} else {
			System.out.println("Lista de recetas encontradas:");
			recetasEncontradas.forEach(receta -> {
				System.out.println("  -> ID: " + receta.getId() + ", Nombre: " + receta.getNombre());
			});
		}
	}

	@Test
	void pruebaBuscarPorTodosLosIngredientes() {
		System.out.println("Prueba busqueda 'Y' por ingredientes");
		List<String> ingredientesBusqueda = List.of("Carne Molida", "Cebolla");
		System.out.println("Ejecutando busqueda por ingredientes: '" + ingredientesBusqueda + "'");
		List<RespuestaReceta> recetasEncontradas = controladorReceta.buscarPorTodosLosIngredientes(ingredientesBusqueda);
		assertThat(recetasEncontradas).isNotNull();
		if (recetasEncontradas.isEmpty()) {
			System.out.println("No se encontraron recetas con todos esos ingredientes.");
		} else {
			System.out.println("Lista de recetas encontradas:");
			recetasEncontradas.forEach(receta -> {
				System.out.println("  -> ID: " + receta.getId() + ", Nombre: " + receta.getNombre());
			});
		}
	}

	@Test
	void pruebaBuscarRecetaPorNombreExactoEncontrado() {
		System.out.println("Prueba busqueda exacta por nombre (exito)");
		String nombreExacto = "Pastel de Choclo";
		System.out.println("Ejecutando busqueda exacta por: '" + nombreExacto + "'");
		ResponseEntity<RespuestaReceta> respuesta = controladorReceta.buscarRecetaPorNombreExacto(nombreExacto);
		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotNull();
		RespuestaReceta recetaEncontrada = respuesta.getBody();

		System.out.println("Busqueda exitosa. Detalles de la receta encontrada:");
		System.out.println("   ID: " + recetaEncontrada.getId());
		System.out.println("   Nombre: " + recetaEncontrada.getNombre());
		System.out.println("   Descripcion: " + recetaEncontrada.getDescripcion());
		System.out.println("   Categoria: " + recetaEncontrada.getCategoriaNombre());
		System.out.println("   Tiempo: " + recetaEncontrada.getTiempoPreparacion() + " min");
		System.out.println("   Imagen URL: " + recetaEncontrada.getImagenUrl());
		System.out.println("   Ingredientes: " + recetaEncontrada.getIngredientes());
	}
}
