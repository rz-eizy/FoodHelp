package com.example.foodhelp.backend.controladores;
import com.example.foodhelp.backend.dto.RespuestaReceta;
import com.example.foodhelp.backend.servicios.ServicioReceta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/recetas")
public class ControladorReceta {


    private final ServicioReceta servicioReceta;

    @GetMapping
    public List<RespuestaReceta> obtenerTodasLasRecetas() {
        return servicioReceta.obtenerTodasLasRecetas();
    }

    @GetMapping("/buscar")
    public List<RespuestaReceta> buscarRecetasPorNombre(@RequestParam String nombre) {
        return servicioReceta.buscarRecetasPorNombre(nombre);
    }

    @GetMapping("/por-categoria")
    public List<RespuestaReceta> buscarRecetasPorCategoria(@RequestParam String categoria) {
        return servicioReceta.buscarRecetasPorCategoria(categoria);
    }

    @GetMapping("/por-ingrediente/or")
    public List<RespuestaReceta> buscarPorCualquierIngrediente(@RequestParam List<String> ingredientes) {
        return servicioReceta.buscarPorCualquierIngrediente(ingredientes);
    }

    @GetMapping("/por-ingrediente/and")
    public List<RespuestaReceta> buscarPorTodosLosIngredientes(@RequestParam List<String> ingredientes) {
        return servicioReceta.buscarPorTodosLosIngredientes(ingredientes);
    }

    @GetMapping("/buscar-exacto")
    public ResponseEntity<RespuestaReceta> buscarRecetaPorNombreExacto(@RequestParam String nombre) {
        return servicioReceta.buscarRecetaPorNombreExacto(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
