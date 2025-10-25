package com.example.foodhelp.backend.controladores;


import com.example.foodhelp.backend.entidades.Receta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recetas")
public class ControladorReceta {

    private final RepositorioReceta repositorioReceta;


    public ControladorReceta(RepositorioReceta repositorioReceta) {
        this.repositorioReceta = repositorioReceta;
    }

    @GetMapping
    public List<Receta> obtenerTodasLasRecetas() {
        return repositorioReceta.findAll();
    }

    @GetMapping("/buscar")
    public List<Receta> buscarRecetasPorNombre(@RequestParam String nombre) {
        return repositorioReceta.findByNombreContainingIgnoreCase(nombre);
    }

    @GetMapping("/por-categoria")
    public List<Receta> buscarRecetasPorCategoria(@RequestParam String categoria) {
        return repositorioReceta.findByCategoriaNombreContainingIgnoreCase(categoria);
    }

    @GetMapping("/por-ingrediente/or")
    public List<Receta> buscarPorCualquierIngrediente(@RequestParam List<String> ingredientes) {
        if (ingredientes == null || ingredientes.isEmpty()) {
            return Collections.emptyList();
        }
        // se convierte a minuscula para que coinsida con la consulta
        List<String> ingredientesLower = ingredientes.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return repositorioReceta.findByCualquierIngredienteEnLista(ingredientesLower);
    }

    @GetMapping("/por-ingrediente/and")
    public List<Receta> buscarPorTodosLosIngredientes(@RequestParam List<String> ingredientes) {
        if (ingredientes == null || ingredientes.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> ingredientesLowerUnicos = ingredientes.stream()
                .map(String::toLowerCase)
                .distinct()
                .collect(Collectors.toList());

        long cantidad = (long) ingredientesLowerUnicos.size();

        return repositorioReceta.findByTodosLosIngredientesEnLista(ingredientesLowerUnicos, cantidad);
    }


}
