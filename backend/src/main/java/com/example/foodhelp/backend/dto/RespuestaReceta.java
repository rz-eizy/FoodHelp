package com.example.foodhelp.backend.dto;
import com.example.foodhelp.backend.entidades.Categoria;
import com.example.foodhelp.backend.entidades.Receta;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.foodhelp.backend.entidades.Ingrediente;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RespuestaReceta {

    private Long id;
    private String nombre;
    private String descripcion;
    private Integer tiempoPreparacion;
    private String imagenUrl;
    private String instrucciones;
    private Categoria categoria;
    private Set<Ingrediente> ingredientes;

    public RespuestaReceta(Receta receta) {
        this.id = receta.getId();
        this.nombre = receta.getNombre();
        this.descripcion = receta.getDescripcion();
        this.tiempoPreparacion = receta.getTiempoPreparacion();
        this.imagenUrl = receta.getImagenUrl();
        this.instrucciones = receta.getInstrucciones();
        this.categoria = receta.getCategoria();
        this.ingredientes = receta.getIngredientes();
    }
}
