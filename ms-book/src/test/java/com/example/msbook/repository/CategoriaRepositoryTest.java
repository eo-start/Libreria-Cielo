/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.repository;

import com.example.msbook.entity.Categoria;
import com.example.msbook.models.Formato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Prueba de integración - CategoriaRepository con H2")
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    @DisplayName("Debe guardar y buscar una categoría en una base H2 temporal")
    void debeGuardarYBuscarCategoria() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Útiles escolares");
        categoria.setClasificacion("Material educativo");
        categoria.setFormato(Formato.IMPRESO);
        categoria.setIdioma("Español");

        Categoria categoriaGuardada = categoriaRepository.save(categoria);

        Optional<Categoria> resultado = categoriaRepository.findById(categoriaGuardada.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Útiles escolares");
        assertThat(resultado.get().getClasificacion()).isEqualTo("Material educativo");
        assertThat(resultado.get().getFormato()).isEqualTo(Formato.IMPRESO);
        assertThat(resultado.get().getIdioma()).isEqualTo("Español");
    }
}