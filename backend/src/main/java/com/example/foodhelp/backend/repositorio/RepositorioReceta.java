package com.example.foodhelp.backend.repositorio;

import com.example.foodhelp.backend.entidades.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioReceta extends JpaRepository<Receta, String> {
}
