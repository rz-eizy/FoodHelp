package com.example.foodhelp.backend.repositorio;

import com.example.foodhelp.backend.entidades.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioReceta extends JpaRepository<Receta, Long> {


    List<Receta> findByNombreContainingIgnoreCase(String nombre);
    // linea para conseguir recetas por nombre

    List<Receta> findByCategoriaNombreContainingIgnoreCase(String nombreCategoria);
    // linea para conseguir recetas por categoria




}
