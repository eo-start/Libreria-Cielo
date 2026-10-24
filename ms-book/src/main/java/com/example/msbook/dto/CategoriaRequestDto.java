/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.dto;

import com.example.msbook.models.Formato;

public class CategoriaRequestDto {

    private String nombre;
    private String clasificacion;
    private Formato formato;
    private String idioma;

    public CategoriaRequestDto() {
    }

    public CategoriaRequestDto(String nombre, String clasificacion, Formato formato, String idioma) {
        this.nombre = nombre;
        this.clasificacion = clasificacion;
        this.formato = formato;
        this.idioma = idioma;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public Formato getFormato() {
        return formato;
    }

    public void setFormato(Formato formato) {
        this.formato = formato;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }
}