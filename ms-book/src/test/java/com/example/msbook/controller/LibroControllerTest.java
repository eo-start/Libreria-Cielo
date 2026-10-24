/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.controller;

import com.example.msbook.entity.Libro;
import com.example.msbook.service.LibroService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroControllerTest {

    @Mock
    private LibroService libroService;

    @InjectMocks
    private LibroController libroController;

    private Libro libro;
    private MockMultipartFile imagen;

    private final Path uploadsPath      = Path.of("uploads").toAbsolutePath().normalize();
    private final Path imagenPruebaPath = uploadsPath.resolve("imagen-test.jpg").normalize();

    @BeforeEach
    void setUp() {
        libro = new Libro();
        libro.setId(1);
        libro.setTitulo("Cien años de soledad");
        libro.setAutor("Gabriel García Márquez");
        libro.setStock(10);
        libro.setPrecio(60.0);
        libro.setImagenUrl("imagen-test.jpg");
        libro.setAnio(LocalDate.of(1967, 1, 1));

        imagen = new MockMultipartFile(
                "imagen",
                "portada.jpg",
                "image/jpeg",
                "contenido de prueba".getBytes()
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(imagenPruebaPath);
    }

    @Test
    void listarLibrosDebeRetornarLista() {
        when(libroService.listar()).thenReturn(List.of(libro));

        List<Libro> response = libroController.listarLibros();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Cien años de soledad", response.get(0).getTitulo());

        verify(libroService).listar();
    }

    @Test
    void crearLibroDebeRetornarLibroCreado() {
        // Firma actual: guardar(titulo, autor, stock, stockMinimo, precio, categoriaId, provedoresId, file, anio)
        when(libroService.guardar(
                "Cien años de soledad",
                "Gabriel García Márquez",
                10,
                10,
                60.0,
                1,
                1,
                imagen,
                LocalDate.of(1967, 1, 1)
        )).thenReturn(libro);

        ResponseEntity<Libro> response = libroController.crearLibro(
                "Cien años de soledad",
                "Gabriel García Márquez",
                10,
                10,
                60.0,
                1,
                1,
                1967,
                imagen
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Cien años de soledad", response.getBody().getTitulo());

        verify(libroService).guardar(
                "Cien años de soledad",
                "Gabriel García Márquez",
                10,
                10,
                60.0,
                1,
                1,
                imagen,
                LocalDate.of(1967, 1, 1)
        );
    }

    @Test
    void editarLibroDebeRetornarLibroActualizado() {
        // Firma actual: editar(id, titulo, autor, stock, stockMinimo, precio, categoriaId, provedoresId, file, anio, estado)
        when(libroService.editar(
                1,
                "Cien años de soledad",
                "Gabriel García Márquez",
                20,
                10,
                70.0,
                1,
                1,
                imagen,
                LocalDate.of(1967, 1, 1),
                null
        )).thenReturn(libro);

        ResponseEntity<Libro> response = libroController.editarLibro(
                1,
                "Cien años de soledad",
                "Gabriel García Márquez",
                20,
                10,
                70.0,
                1,
                1,
                1967,
                imagen,
                null
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(libroService).editar(
                1,
                "Cien años de soledad",
                "Gabriel García Márquez",
                20,
                10,
                70.0,
                1,
                1,
                imagen,
                LocalDate.of(1967, 1, 1),
                null
        );
    }

    @Test
    void eliminarLibroDebeRetornarNoContent() {
        doNothing().when(libroService).eliminar(1);

        ResponseEntity<Void> response = libroController.eliminarLibro(1);

        assertEquals(204, response.getStatusCode().value());

        verify(libroService).eliminar(1);
    }

    @Test
    void listarLibroPorIdDebeRetornarOkCuandoExiste() {
        when(libroService.listarPorId(1)).thenReturn(Optional.of(libro));

        ResponseEntity<Libro> response = libroController.listarlibroId(1);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Cien años de soledad", response.getBody().getTitulo());

        verify(libroService).listarPorId(1);
    }

    @Test
    void listarLibroPorIdDebeRetornarNotFoundCuandoNoExiste() {
        when(libroService.listarPorId(99)).thenReturn(Optional.empty());

        ResponseEntity<Libro> response = libroController.listarlibroId(99);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(libroService).listarPorId(99);
    }

    @Test
    void actualizarStockDebeRetornarLibroActualizado() {
        libro.setStock(25);

        when(libroService.actualizarStock(1, 25)).thenReturn(libro);

        ResponseEntity<Libro> response = libroController.actualizarStock(1, 25);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(25, response.getBody().getStock());

        verify(libroService).actualizarStock(1, 25);
    }

    @Test
    void listarLibrosPorCategoriaDebeRetornarLista() {
        when(libroService.listarPorCategoria(1)).thenReturn(List.of(libro));

        List<Libro> response = libroController.listarLibrosPorCategoria(1);

        assertEquals(1, response.size());

        verify(libroService).listarPorCategoria(1);
    }

    @Test
    void listarLibrosPorAnioDebeRetornarLista() {
        when(libroService.listarPorAnio(LocalDate.of(2024, 1, 1))).thenReturn(List.of(libro));

        List<Libro> response = libroController.listarLibrosPorAnio(2024);

        assertEquals(1, response.size());

        verify(libroService).listarPorAnio(LocalDate.of(2024, 1, 1));
    }

    @Test
    void listarLibrosDesdeAnioDebeRetornarLista() {
        when(libroService.listarDesdeAnio(LocalDate.of(2020, 1, 1))).thenReturn(List.of(libro));

        List<Libro> response = libroController.listarLibrosDesdeAnio(2020);

        assertEquals(1, response.size());

        verify(libroService).listarDesdeAnio(LocalDate.of(2020, 1, 1));
    }

    @Test
    void serveFileDebeRetornarResourceCuandoArchivoExiste() throws IOException {
        Files.createDirectories(uploadsPath);
        Files.writeString(imagenPruebaPath, "contenido de imagen");

        ResponseEntity<Resource> response = libroController.serveFile("imagen-test.jpg");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().exists());
    }

    @Test
    void serveFileDebeLanzarNotFoundCuandoArchivoNoExiste() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> libroController.serveFile("archivo-no-existe.jpg")
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void serveFileDebeLanzarBadRequestCuandoNombreEsInvalido() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> libroController.serveFile("../archivo-malicioso.jpg")
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void serveFileDebeLanzarBadRequestCuandoNombreEsNull() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> libroController.serveFile(null)
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void serveFileDebeLanzarBadRequestCuandoNombreEstaVacio() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> libroController.serveFile("   ")
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void serveFileDebeLanzarBadRequestCuandoNombreTieneCaracteresNoPermitidos() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> libroController.serveFile("imagen|maliciosa.jpg")
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}
