package com.example.foodhelp.backend.controladores;


import com.example.foodhelp.backend.entidades.Receta;
import com.example.foodhelp.backend.repositorio.RepositorioReceta;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
