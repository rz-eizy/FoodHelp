package com.example.foodhelp.backend.servicios;


import com.example.foodhelp.backend.dto.RespuestaReceta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioReceta {

    private final RepositorioReceta repositorioReceta;

    // metodos de lectura de los datos

    public List<RespuestaReceta> obtenerTodasLasRecetas() {
        return repositorioReceta.findAll().stream()
                .map(RespuestaReceta::new)
                .collect(Collectors.toList());
    }

    public Optional<RespuestaReceta> buscarPorId(Long id) {
        return repositorioReceta.findById(id)
                .map(RespuestaReceta::new);
    }

    public List<RespuestaReceta> buscarRecetasPorNombre(String nombre) {
        log.info("ENTRADA: Busqueda de receta solicitada por NOMBRE: '{}'", nombre);
        List<RespuestaReceta> listaResultado = repositorioReceta.findByNombreContainingIgnoreCase(nombre).stream()
                .map(RespuestaReceta::new)
                .collect(Collectors.toList());
        log.info("SALIDA: Busqueda finalizada. Se encontraron {} recetas. RECETAS: {}", listaResultado.size(), listaResultado);
        return listaResultado;

    }

    public List<RespuestaReceta> buscarRecetasPorCategoria(String categoria) {
        log.info("ENTRADA: Busqueda de receta solicitada por CATEGORIA: '{}'", categoria);
        List<RespuestaReceta> listaResultado = repositorioReceta.findByCategoriaNombreContainingIgnoreCase(categoria).stream()
                .map(RespuestaReceta::new)
                .collect(Collectors.toList());
        log.info("SALIDA: Busqueda finalizada. Se encontraron {} recetas. RECETAS: {}", listaResultado.size(), listaResultado);
        return listaResultado;
    }

    public List<RespuestaReceta> buscarPorCualquierIngrediente(List<String> ingredientes) {
        if (ingredientes == null || ingredientes.isEmpty()) return Collections.emptyList();

        List<String> ingredientesLower = ingredientes.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return repositorioReceta.findByCualquierIngredienteEnLista(ingredientesLower).stream()
                .map(RespuestaReceta::new)
                .collect(Collectors.toList());
    }

    public List<RespuestaReceta> buscarPorTodosLosIngredientes(List<String> ingredientes) {
        if (ingredientes == null || ingredientes.isEmpty()) return Collections.emptyList();

        List<String> ingredientesLowerUnicos = ingredientes.stream()
                .map(String::toLowerCase)
                .distinct()
                .collect(Collectors.toList());

        long cantidad = ingredientesLowerUnicos.size();

        return repositorioReceta.findByTodosLosIngredientesEnLista(ingredientesLowerUnicos, cantidad).stream()
                .map(RespuestaReceta::new)
                .collect(Collectors.toList());
    }

    public Optional<RespuestaReceta> buscarRecetaPorNombreExacto(String nombre) {
        return repositorioReceta.findByNombreIgnoreCase(nombre)
                .map(RespuestaReceta::new);
    }
}