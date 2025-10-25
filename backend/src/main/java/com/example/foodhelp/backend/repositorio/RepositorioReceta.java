package com.example.foodhelp.backend.repositorio;

import com.example.foodhelp.backend.entidades.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioReceta extends JpaRepository<Receta, Long> {


    List<Receta> findByNombreContainingIgnoreCase(String nombre);
    // linea para conseguir recetas por nombre

    List<Receta> findByCategoriaNombreContainingIgnoreCase(String nombreCategoria);
    // linea para conseguir recetas por categoria

    @Query("SELECT DISTINCT r FROM Receta r" +
            " JOIN r.ingredientes i " +
            "WHERE LOWER(i.nombre) IN :nombres")
    List<Receta> findByCualquierIngredienteEnLista(@Param("nombres") List<String> nombres);

    @Query("SELECT r FROM Receta r JOIN r.ingredientes i WHERE LOWER(i.nombre) IN :nombres GROUP BY r HAVING COUNT(DISTINCT LOWER(i.nombre)) = :cantidad")
    List<Receta> findByTodosLosIngredientesEnLista(
            @Param("nombres") List<String> nombres,
            @Param("cantidad") Long cantidad
    );




}
