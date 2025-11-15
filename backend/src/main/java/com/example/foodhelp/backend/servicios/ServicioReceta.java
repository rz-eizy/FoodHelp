package com.example.foodhelp.backend.servicios;


import com.example.foodhelp.backend.dto.RespuestaReceta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioReceta {

    private final RepositorioReceta repositorioReceta;

    // metodos de lectura de los datos

    public List<RespuestaReceta> obtenerTodasLasRecetas() {
        return repositorioReceta.findAll().stream()
                .map(RespuestaReceta::new)
                .collect(Collectors.toList());
    }

    public List<RespuestaReceta> buscarRecetasPorNombre(String nombre) {
        return repositorioReceta.findByNombreContainingIgnoreCase(nombre).stream()
                .map(RespuestaReceta::new)
                .collect(Collectors.toList());
    }

    public List<RespuestaReceta> buscarRecetasPorCategoria(String categoria) {
        return repositorioReceta.findByCategoriaNombreContainingIgnoreCase(categoria).stream()
                .map(RespuestaReceta::new)
                .collect(Collectors.toList());
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