package com.example.foodhelp.backend.controladores;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.example.foodhelp.backend.dto.RespuestaReceta;
import com.example.foodhelp.backend.servicios.ServicioReceta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ControladorRecetaTest {

    @Mock
    private ServicioReceta servicioReceta;

    @InjectMocks
    private ControladorReceta controladorReceta;

    private RespuestaReceta dtoEjemplo;

    @BeforeEach
    void setUp() {
        dtoEjemplo = new RespuestaReceta();
        dtoEjemplo.setId(1L);
        dtoEjemplo.setNombre("Pollo con Papas");
        dtoEjemplo.setDescripcion("Una receta clásica");
        dtoEjemplo.setTiempoPreparacion(45);
        dtoEjemplo.setImagenUrl("http://ejemplo.com/img.jpg");
        dtoEjemplo.setInstrucciones("Paso 1: Cocinar...");
    }

    @Test
    @DisplayName("Debe retornar lista de DTOs al obtener todas las recetas")
    void pruebaObtenerTodasLasRecetas() {
        when(servicioReceta.obtenerTodasLasRecetas()).thenReturn(List.of(dtoEjemplo));
        List<RespuestaReceta> resultado = controladorReceta.obtenerTodasLasRecetas();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Pollo con Papas");
        verify(servicioReceta, times(1)).obtenerTodasLasRecetas();
    }

    @Test
    @DisplayName("Debe retornar 200 OK y el DTO si encuentra la receta por ID")
    void pruebaBuscarRecetaPorId_Encontrado() {
        Long idBusqueda = 1L;
        when(servicioReceta.buscarPorId(idBusqueda)).thenReturn(Optional.of(dtoEjemplo));
        ResponseEntity<RespuestaReceta> respuesta = controladorReceta.buscarRecetaPorId(idBusqueda);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Debe retornar 404 Not Found si NO encuentra la receta por ID")
    void pruebaBuscarRecetaPorId_NoEncontrado() {
        Long idBusqueda = 99L;
        when(servicioReceta.buscarPorId(idBusqueda)).thenReturn(Optional.empty());
        ResponseEntity<RespuestaReceta> respuesta = controladorReceta.buscarRecetaPorId(idBusqueda);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNull();
    }

    @Test
    @DisplayName("Debe buscar DTOs por nombre")
    void pruebaBuscarRecetasPorNombre() {
        String nombre = "pollo";
        when(servicioReceta.buscarRecetasPorNombre(nombre)).thenReturn(List.of(dtoEjemplo));
        List<RespuestaReceta> resultado = controladorReceta.buscarRecetasPorNombre(nombre);
        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).getNombre()).isEqualTo("Pollo con Papas");
    }

    @Test
    @DisplayName("Debe buscar DTOs por categoría")
    void pruebaBuscarRecetasPorCategoria() {
        String categoriaBusqueda = "ALMUERZO";

        when(servicioReceta.buscarRecetasPorCategoria(categoriaBusqueda)).thenReturn(List.of(dtoEjemplo));

        List<RespuestaReceta> resultado = controladorReceta.buscarRecetasPorCategoria(categoriaBusqueda);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Debe buscar por cualquier ingrediente (OR)")
    void pruebaBuscarPorCualquierIngrediente() {
        List<String> ingredientesBusqueda = List.of("Papa", "Pollo");

        when(servicioReceta.buscarPorCualquierIngrediente(ingredientesBusqueda)).thenReturn(List.of(dtoEjemplo));

        List<RespuestaReceta> resultado = controladorReceta.buscarPorCualquierIngrediente(ingredientesBusqueda);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Debe buscar por todos los ingredientes (AND)")
    void pruebaBuscarPorTodosLosIngredientes() {
        List<String> ingredientesBusqueda = List.of("Papa", "Pollo", "Sal");

        when(servicioReceta.buscarPorTodosLosIngredientes(ingredientesBusqueda)).thenReturn(List.of(dtoEjemplo));

        List<RespuestaReceta> resultado = controladorReceta.buscarPorTodosLosIngredientes(ingredientesBusqueda);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Debe retornar 200 OK si encuentra nombre exacto")
    void pruebaBuscarRecetaPorNombreExacto_Encontrado() {
        String nombreExacto = "Pollo con Papas";
        when(servicioReceta.buscarRecetaPorNombreExacto(nombreExacto)).thenReturn(Optional.of(dtoEjemplo));

        ResponseEntity<RespuestaReceta> respuesta = controladorReceta.buscarRecetaPorNombreExacto(nombreExacto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getNombre()).isEqualTo(nombreExacto);
    }

    @Test
    @DisplayName("Debe retornar 404 Not Found si NO encuentra nombre exacto")
    void pruebaBuscarRecetaPorNombreExacto_NoEncontrado() {
        String nombreFalso = "Comida Inexistente";
        when(servicioReceta.buscarRecetaPorNombreExacto(nombreFalso)).thenReturn(Optional.empty());

        ResponseEntity<RespuestaReceta> respuesta = controladorReceta.buscarRecetaPorNombreExacto(nombreFalso);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}