/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.controller;

import com.example.msbook.entity.Categoria;
import com.example.msbook.models.Formato;
import com.example.msbook.repository.CategoriaRepository;
import com.example.msbook.repository.LibroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Prueba de integración - CategoriaController con MockMvc y H2")
@org.springframework.security.test.context.support.WithMockUser(roles="ADMIN")
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LibroRepository libroRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        libroRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe crear y listar categorías usando endpoints simulados")
    void debeCrearYListarCategorias() throws Exception {
        Categoria categoria = crearCategoriaRequest(
                "Útiles escolares",
                "Material educativo",
                Formato.IMPRESO,
                "Español"
        );

        mockMvc.perform(post("/categoria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Útiles escolares"))
                .andExpect(jsonPath("$.clasificacion").value("Material educativo"))
                .andExpect(jsonPath("$.formato").value("IMPRESO"))
                .andExpect(jsonPath("$.idioma").value("Español"));

        mockMvc.perform(get("/categoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Útiles escolares"))
                .andExpect(jsonPath("$[0].clasificacion").value("Material educativo"))
                .andExpect(jsonPath("$[0].formato").value("IMPRESO"))
                .andExpect(jsonPath("$[0].idioma").value("Español"));
    }

    @Test
    @DisplayName("Debe buscar categoría por ID cuando existe")
    void debeBuscarCategoriaPorIdCuandoExiste() throws Exception {
        Categoria categoriaGuardada = categoriaRepository.save(
                crearCategoriaRequest(
                        "Libros digitales",
                        "Educación virtual",
                        Formato.ELECTRONICO,
                        "Inglés"
                )
        );

        mockMvc.perform(get("/categoria/{id}", categoriaGuardada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoriaGuardada.getId()))
                .andExpect(jsonPath("$.nombre").value("Libros digitales"))
                .andExpect(jsonPath("$.clasificacion").value("Educación virtual"))
                .andExpect(jsonPath("$.formato").value("ELECTRONICO"))
                .andExpect(jsonPath("$.idioma").value("Inglés"));
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la categoría no existe")
    void debeRetornarNotFoundCuandoCategoriaNoExiste() throws Exception {
        mockMvc.perform(get("/categoria/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe editar categoría cuando existe")
    void debeEditarCategoriaCuandoExiste() throws Exception {
        Categoria categoriaGuardada = categoriaRepository.save(
                crearCategoriaRequest(
                        "Cuadernos",
                        "Material escolar",
                        Formato.IMPRESO,
                        "Español"
                )
        );

        Categoria categoriaActualizada = crearCategoriaRequest(
                "Audiolibros educativos",
                "Material auditivo",
                Formato.AUDIOLIBRO,
                "Español"
        );

        mockMvc.perform(put("/categoria/{id}", categoriaGuardada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaActualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoriaGuardada.getId()))
                .andExpect(jsonPath("$.nombre").value("Audiolibros educativos"))
                .andExpect(jsonPath("$.clasificacion").value("Material auditivo"))
                .andExpect(jsonPath("$.formato").value("AUDIOLIBRO"))
                .andExpect(jsonPath("$.idioma").value("Español"));
    }

    @Test
    @DisplayName("Debe retornar 404 al editar una categoría inexistente")
    void debeRetornarNotFoundAlEditarCategoriaInexistente() throws Exception {
        Categoria categoriaActualizada = crearCategoriaRequest(
                "Categoría inexistente",
                "Sin clasificación",
                Formato.ELECTRONICO,
                "Francés"
        );

        mockMvc.perform(put("/categoria/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaActualizada)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe eliminar categoría correctamente")
    void debeEliminarCategoriaCorrectamente() throws Exception {
        Categoria categoriaGuardada = categoriaRepository.save(
                crearCategoriaRequest(
                        "Material de oficina",
                        "Oficina",
                        Formato.IMPRESO,
                        "Español"
                )
        );

        mockMvc.perform(delete("/categoria/{id}", categoriaGuardada.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/categoria/{id}", categoriaGuardada.getId()))
                .andExpect(status().isNotFound());
    }

    private Categoria crearCategoriaRequest(
            String nombre,
            String clasificacion,
            Formato formato,
            String idioma
    ) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setClasificacion(clasificacion);
        categoria.setFormato(formato);
        categoria.setIdioma(idioma);
        return categoria;
    }
}