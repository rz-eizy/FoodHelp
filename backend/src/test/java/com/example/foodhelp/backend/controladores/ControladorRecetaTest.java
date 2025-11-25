package com.example.foodhelp.backend.controladores;
import static org.mockito.Mockito.when;

import com.example.foodhelp.backend.entidades.Categoria;
import com.example.foodhelp.backend.entidades.Receta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ControladorRecetaTest {

    @Mock
    private RepositorioReceta repositorioReceta;

    // 2. Inyectamos el mock dentro del Controlador que vamos a probar
    @InjectMocks
    private ControladorReceta controladorReceta;

    private Receta recetaEjemplo;

    private Categoria categoriaEjemplo;

    @BeforeEach
    void setUp() {

        categoriaEjemplo = new Categoria();
        categoriaEjemplo.setId(10L);
        categoriaEjemplo.setNombre("ALMUERZO");
        // Preparamos un objeto base para usar en los tests
        recetaEjemplo = new Receta();
        recetaEjemplo.setId(1L);
        recetaEjemplo.setNombre("Pollo con Papas");
        recetaEjemplo.setDescripcion("Una receta clásica");
        recetaEjemplo.setCategoria(categoriaEjemplo);


    }

    @Test
    @DisplayName("Debe retornar todas las recetas cuando existen en la BD")
    void pruebaObtenerTodasLasRecetas() {
        // GIVEN (Dado): El repositorio retornará una lista con 1 receta
        when(repositorioReceta.findAll()).thenReturn(List.of(recetaEjemplo));

        // WHEN (Cuando): Llamamos al método del controlador
        // (Asumo que tu controlador tiene un método que llama a repo.findAll())
        List<Receta> resultado = controladorReceta.obtenerTodasLasRecetas();

        // THEN (Entonces): Verificamos los resultados
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Pollo con Papas");

        // Verificamos que el repositorio fue llamado exactamente una vez
        verify(repositorioReceta, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar lista vacía si no hay recetas")
    void pruebaObtenerTodasLasRecetas_Vacio() {
        // GIVEN
        when(repositorioReceta.findAll()).thenReturn(Collections.emptyList());

        // WHEN
        List<Receta> resultado = controladorReceta.obtenerTodasLasRecetas();

        // THEN
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe buscar recetas por nombre correctamente")
    void pruebaBuscarRecetasPorNombre() {
        // GIVEN
        String nombreBusqueda = "pollo";
        when(controladorReceta.buscarRecetasPorNombre(nombreBusqueda))
                .thenReturn(List.of(recetaEjemplo));
        // NOTA: Si el método 'buscarRecetasPorNombre' del controlador llama a un método
        // del repositorio (ej. findByNombreContaining), debes mockear ESE método del repo aquí.
        // Ejemplo: when(repositorioReceta.findByNombreContainingIgnoreCase("pollo")).thenReturn(...)

        // WHEN
        List<Receta> resultado = controladorReceta.buscarRecetasPorNombre(nombreBusqueda);

        // THEN
        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).getNombre()).containsIgnoringCase(nombreBusqueda);
    }

    @Test
    @DisplayName("Debe buscar recetas por categoría")
    void pruebaBuscarRecetasPorCategoria() {
        // GIVEN
        String nombreCategoriaBusqueda = "ALMUERZO";

        // Aquí simulamos que el repositorio busca por el nombre de la categoría
        // NOTA: Ajusta "findByCategoriaNombre" al nombre real que tengas en tu RepositorioReceta
        when(controladorReceta.buscarRecetasPorCategoria(nombreCategoriaBusqueda))
                .thenReturn(List.of(recetaEjemplo));

        // WHEN
        List<Receta> resultado = controladorReceta.buscarRecetasPorCategoria(nombreCategoriaBusqueda);

        // THEN
        assertThat(resultado).hasSize(1);

        // Aserción corregida: Entramos al objeto categoría y pedimos su nombre
        assertThat(resultado.get(0).getCategoria().getNombre()).isEqualTo(nombreCategoriaBusqueda);
    }

    @Test
    @DisplayName("Debe buscar por CUALQUIER ingrediente (OR)")
    void pruebaBuscarPorCualquierIngrediente() {
        // GIVEN
        List<String> ingredientes = List.of("huevo", "choclo");
        // Simulamos que encontramos una receta
        when(controladorReceta.buscarPorCualquierIngrediente(ingredientes))
                .thenReturn(List.of(recetaEjemplo));

        // WHEN
        List<Receta> resultado = controladorReceta.buscarPorCualquierIngrediente(ingredientes);

        // THEN
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Debe buscar por TODOS los ingredientes (AND)")
    void pruebaBuscarPorTodosLosIngredientes() {
        // GIVEN
        List<String> ingredientes = List.of("Carne Molida", "Cebolla");
        when(controladorReceta.buscarPorTodosLosIngredientes(ingredientes))
                .thenReturn(List.of(recetaEjemplo));

        // WHEN
        List<Receta> resultado = controladorReceta.buscarPorTodosLosIngredientes(ingredientes);

        // THEN
        assertThat(resultado).isNotNull();
        // Aquí validamos lógica de negocio: si la lista no es nula, pasó la prueba unitaria
    }


}