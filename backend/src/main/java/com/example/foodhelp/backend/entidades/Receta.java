package com.example.foodhelp.backend.entidades;
import jakarta.persistence.*;
import java.util.Set;


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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public void setTiempoPreparacion(Integer tiempoPreparacion) {
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(String instrucciones) {
        this.instrucciones = instrucciones;
    }

    public Set<Ingrediente> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(Set<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes;
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
