package com.example.foodhelp.backend.entidades;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "receta")
public class Receta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;

    @Column(name = "tiempo_preparacion")
    private Integer tiempoPreparacion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    private String instrucciones;

    @OneToMany(mappedBy = "receta",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Ingrediente> ingredientes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;


    public Receta(){

    }

    public Receta(Set<Ingrediente> ingredientes, String instrucciones, String imagenUrl, Integer tiempoPreparacion, String descripcion, String nombre) {
        this.ingredientes = ingredientes;
        this.instrucciones = instrucciones;
        this.imagenUrl = imagenUrl;
        this.tiempoPreparacion = tiempoPreparacion;
        this.descripcion = descripcion;
        this.nombre = nombre;
    }


    @Override
    public String toString() {
        return "Receta{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tiempoPreparacion=" + tiempoPreparacion +
                ", imagenUrl='" + imagenUrl + '\'' +
                ", instrucciones='" + instrucciones + '\'' +
                ", ingredientes=" + ingredientes +
                ", categoria=" + categoria +
                '}';
    }
}
