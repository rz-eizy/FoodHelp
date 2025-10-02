package com.example.foodhelp.backend.entidades;

import jakarta.persistence.*;
@Entity
@Table(name = "ingrediente")
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    private Double cantidad;
    private String unidad;

    @ManyToOne
    @JoinColumn(name = "receta_nombre")
    private Receta receta;

    public Ingrediente(){

    }

    public Ingrediente(Long id, String nombre, String descripcion, Double cantidad, String unidad, Receta receta) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.receta = receta;
    }

    @Override
    public String toString() {
        return "Ingrediente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", cantidad=" + cantidad +
                ", unidad='" + unidad + '\'' +
                '}';
    }
}
