/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.service.impl;

import com.example.msbook.entity.Categoria;
import com.example.msbook.entity.Libro;
import com.example.msbook.entity.Provedores;
import com.example.msbook.repository.CategoriaRepository;
import com.example.msbook.repository.LibroRepository;
import com.example.msbook.repository.ProvedoresRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServiceImplTest {

    private static final LocalDate ANIO_1967 = LocalDate.of(1967, 1, 1);
    private static final LocalDate ANIO_2020 = LocalDate.of(2020, 1, 1);
    private static final LocalDate ANIO_2024 = LocalDate.of(2024, 1, 1);

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProvedoresRepository provedoresRepository;

    @InjectMocks
    private LibroServiceImpl libroService;

    private Categoria categoria;
    private Provedores proveedor;
    private Libro libro;
    private MockMultipartFile imagenValida;
    private Path uploadsPath;
    private Set<Path> archivosExistentesAntes;

    @BeforeEach
    void setUp() throws IOException {
        uploadsPath = Path.of("uploads").toAbsolutePath().normalize();
        Files.createDirectories(uploadsPath);

        archivosExistentesAntes = new HashSet<>();
        try (var paths = Files.list(uploadsPath)) {
            paths.forEach(archivosExistentesAntes::add);
        }

        categoria = new Categoria();
        categoria.setId(1);
        categoria.setNombre("Literatura");

        proveedor = new Provedores();
        proveedor.setId(1);
        proveedor.setNombre("Proveedor Principal");

        libro = new Libro();
        libro.setId(1);
        libro.setTitulo("Cien años de soledad");
        libro.setAutor("Gabriel García Márquez");
        libro.setStock(10);
        libro.setPrecio(60.0);
        libro.setCategoria(categoria);
        libro.setProvedores(proveedor);
        libro.setImagenUrl("imagen-antigua.jpg");
        libro.setAnio(ANIO_1967);

        imagenValida = new MockMultipartFile(
                "imagen",
                "portada.jpg",
                "image/jpeg",
                "contenido de imagen".getBytes()
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(uploadsPath)) {
            try (var paths = Files.list(uploadsPath)) {
                paths.filter(path -> !archivosExistentesAntes.contains(path))
                        .forEach(this::eliminarArchivoTemporal);
            }
        }
    }

    // ── listar ───────────────────────────────────────────────────────────────

    @Test
    void listarDebeRetornarTodosLosLibros() {
        when(libroRepository.findAll()).thenReturn(List.of(libro));

        List<Libro> resultado = libroService.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Cien años de soledad", resultado.get(0).getTitulo());

        verify(libroRepository).findAll();
    }

    // ── guardar ──────────────────────────────────────────────────────────────

    @Test
    void guardarDebeCrearLibroConImagenValida() {
        when(libroRepository.count()).thenReturn(0L);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(provedoresRepository.findById(1)).thenReturn(Optional.of(proveedor));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> {
            Libro libroGuardado = invocation.getArgument(0);
            libroGuardado.setId(1);
            return libroGuardado;
        });

        Libro resultado = guardarLibro(1, 1, imagenValida);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Cien años de soledad", resultado.getTitulo());
        assertEquals("Gabriel García Márquez", resultado.getAutor());
        assertEquals(10, resultado.getStock());
        assertEquals(60.0, resultado.getPrecio());
        assertEquals(categoria, resultado.getCategoria());
        assertEquals(proveedor, resultado.getProvedores());
        assertNotNull(resultado.getImagenUrl());
        assertTrue(resultado.getImagenUrl().endsWith(".jpg"));
        assertEquals(ANIO_1967, resultado.getAnio());

        verify(libroRepository).count();
        verify(categoriaRepository).findById(1);
        verify(provedoresRepository).findById(1);
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    void guardarDebePermitirImagenPng() {
        MockMultipartFile imagenPng = new MockMultipartFile(
                "imagen", "portada.png", "image/png", "contenido png".getBytes()
        );

        when(libroRepository.count()).thenReturn(1L);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(provedoresRepository.findById(1)).thenReturn(Optional.of(proveedor));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = guardarLibro(1, 1, imagenPng);

        assertNotNull(resultado);
        assertNotNull(resultado.getImagenUrl());
        assertTrue(resultado.getImagenUrl().endsWith(".png"));

        verify(libroRepository).count();
        verify(categoriaRepository).findById(1);
        verify(provedoresRepository).findById(1);
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    void guardarDebePermitirImagenWebp() {
        MockMultipartFile imagenWebp = new MockMultipartFile(
                "imagen", "portada.webp", "image/webp", "contenido webp".getBytes()
        );

        when(libroRepository.count()).thenReturn(2L);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(provedoresRepository.findById(1)).thenReturn(Optional.of(proveedor));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = guardarLibro(1, 1, imagenWebp);

        assertNotNull(resultado);
        assertNotNull(resultado.getImagenUrl());
        assertTrue(resultado.getImagenUrl().endsWith(".webp"));

        verify(libroRepository).count();
        verify(categoriaRepository).findById(1);
        verify(provedoresRepository).findById(1);
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    void guardarDebePermitirImagenNula() {
        // La imagen es opcional — null produce imagenUrl = null
        when(libroRepository.count()).thenReturn(0L);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(provedoresRepository.findById(1)).thenReturn(Optional.of(proveedor));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = guardarLibro(1, 1, null);

        assertNotNull(resultado);
        assertNull(resultado.getImagenUrl());

        verify(libroRepository).count();
        verify(categoriaRepository).findById(1);
        verify(provedoresRepository).findById(1);
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    void guardarDebePermitirImagenVacia() {
        // La imagen vacía se trata igual que null — imagenUrl = null
        MockMultipartFile archivoVacio = new MockMultipartFile(
                "imagen", "vacio.jpg", "image/jpeg", new byte[0]
        );

        when(libroRepository.count()).thenReturn(0L);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(provedoresRepository.findById(1)).thenReturn(Optional.of(proveedor));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = guardarLibro(1, 1, archivoVacio);

        assertNotNull(resultado);
        assertNull(resultado.getImagenUrl());

        verify(libroRepository).count();
        verify(categoriaRepository).findById(1);
        verify(provedoresRepository).findById(1);
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    void guardarDebeLanzarExcepcionCuandoCategoriaNoExiste() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> guardarLibro(99, 1, imagenValida)
        );

        assertEquals("Categoría no encontrada", exception.getMessage());

        verify(categoriaRepository).findById(99);
        verify(provedoresRepository, never()).findById(anyInt());
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    void guardarDebeLanzarExcepcionCuandoProveedorNoExiste() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(provedoresRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> guardarLibro(1, 99, imagenValida)
        );

        assertEquals("Proveedor no encontrado", exception.getMessage());

        verify(categoriaRepository).findById(1);
        verify(provedoresRepository).findById(99);
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    void guardarDebeLanzarExcepcionCuandoNombreArchivoEsNull() {
        MockMultipartFile archivoSinNombre = new MockMultipartFile(
                "imagen", null, "image/jpeg", "contenido".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> guardarLibro(1, 1, archivoSinNombre)
        );

        assertEquals("El archivo no tiene nombre válido.", exception.getMessage());

        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(provedoresRepository);
        verifyNoInteractions(libroRepository);
    }

    @Test
    void guardarDebeLanzarExcepcionCuandoNombreArchivoEstaEnBlanco() {
        MockMultipartFile archivoSinNombreValido = new MockMultipartFile(
                "imagen", "   ", "image/jpeg", "contenido".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> guardarLibro(1, 1, archivoSinNombreValido)
        );

        assertEquals("El archivo no tiene nombre válido.", exception.getMessage());

        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(provedoresRepository);
        verifyNoInteractions(libroRepository);
    }

    @Test
    void guardarDebeLanzarExcepcionCuandoArchivoNoTieneExtension() {
        MockMultipartFile archivoSinExtension = new MockMultipartFile(
                "imagen", "archivo", "image/jpeg", "contenido".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> guardarLibro(1, 1, archivoSinExtension)
        );

        assertEquals("El archivo no tiene extensión válida.", exception.getMessage());

        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(provedoresRepository);
        verifyNoInteractions(libroRepository);
    }

    @Test
    void guardarDebeLanzarExcepcionCuandoExtensionEstaVacia() {
        MockMultipartFile archivoConPuntoFinal = new MockMultipartFile(
                "imagen", "archivo.", "image/jpeg", "contenido".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> guardarLibro(1, 1, archivoConPuntoFinal)
        );

        assertEquals("El archivo no tiene extensión válida.", exception.getMessage());

        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(provedoresRepository);
        verifyNoInteractions(libroRepository);
    }

    @Test
    void guardarDebeLanzarExcepcionCuandoExtensionNoEsPermitida() {
        MockMultipartFile archivoNoPermitido = new MockMultipartFile(
                "imagen", "archivo.pdf", "application/pdf", "contenido".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> guardarLibro(1, 1, archivoNoPermitido)
        );

        assertEquals(
                "Tipo de archivo inválido. Solo se permiten imágenes JPG, JPEG, PNG o WEBP.",
                exception.getMessage()
        );

        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(provedoresRepository);
        verifyNoInteractions(libroRepository);
    }

    @Test
    void guardarDebeLanzarExcepcionCuandoFallaLecturaDelArchivo() throws IOException {
        MultipartFile archivoConError = mock(MultipartFile.class);
        when(archivoConError.isEmpty()).thenReturn(false);
        when(archivoConError.getOriginalFilename()).thenReturn("imagen.jpg");
        when(archivoConError.getInputStream()).thenThrow(new IOException("Error de lectura"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> guardarLibro(1, 1, archivoConError)
        );

        assertEquals("No se pudo guardar la imagen del libro.", exception.getMessage());

        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(provedoresRepository);
        verifyNoInteractions(libroRepository);
    }

    // ── editar ───────────────────────────────────────────────────────────────

    @Test
    void editarDebeActualizarLibroSinCambiarImagen() {
        when(libroRepository.findForUpdate(1)).thenReturn(Optional.of(libro));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(provedoresRepository.findById(1)).thenReturn(Optional.of(proveedor));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = editarLibro(1, 1, 1, null);

        assertNotNull(resultado);
        assertEquals("Libro actualizado", resultado.getTitulo());
        assertEquals("Autor actualizado", resultado.getAutor());
        assertEquals(20, resultado.getStock());
        assertEquals(80.0, resultado.getPrecio());
        assertEquals("imagen-antigua.jpg", resultado.getImagenUrl());
        assertEquals(ANIO_2020, resultado.getAnio());

        verify(libroRepository).findForUpdate(1);
        verify(categoriaRepository).findById(1);
        verify(provedoresRepository).findById(1);
        verify(libroRepository).save(libro);
    }

    @Test
    void editarDebeActualizarLibroCambiandoImagen() throws IOException {
        Path imagenAntigua = uploadsPath.resolve("imagen-antigua.jpg").normalize();
        Files.writeString(imagenAntigua, "imagen antigua");

        when(libroRepository.findForUpdate(1)).thenReturn(Optional.of(libro));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(provedoresRepository.findById(1)).thenReturn(Optional.of(proveedor));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = editarLibro(1, 1, 1, imagenValida);

        assertNotNull(resultado);
        assertEquals("Libro actualizado", resultado.getTitulo());
        assertNotNull(resultado.getImagenUrl());
        assertNotEquals("imagen-antigua.jpg", resultado.getImagenUrl());
        assertFalse(Files.exists(imagenAntigua));

        verify(libroRepository).findForUpdate(1);
        verify(categoriaRepository).findById(1);
        verify(provedoresRepository).findById(1);
        verify(libroRepository).save(libro);
    }

    @Test
    void editarDebeLanzarExcepcionCuandoLibroNoExiste() {
        when(libroRepository.findForUpdate(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> editarLibro(99, 1, 1, null)
        );

        assertEquals("Libro no encontrado", exception.getMessage());

        verify(libroRepository).findForUpdate(99);
        verifyNoInteractions(categoriaRepository);
        verifyNoInteractions(provedoresRepository);
    }

    @Test
    void editarDebeLanzarExcepcionCuandoCategoriaNoExiste() {
        when(libroRepository.findForUpdate(1)).thenReturn(Optional.of(libro));
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> editarLibro(1, 99, 1, null)
        );

        assertEquals("Categoría no encontrada", exception.getMessage());

        verify(libroRepository).findForUpdate(1);
        verify(categoriaRepository).findById(99);
        verifyNoInteractions(provedoresRepository);
    }

    @Test
    void editarDebeLanzarExcepcionCuandoProveedorNoExiste() {
        when(libroRepository.findForUpdate(1)).thenReturn(Optional.of(libro));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(provedoresRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> editarLibro(1, 1, 99, null)
        );

        assertEquals("Proveedor no encontrado", exception.getMessage());

        verify(libroRepository).findForUpdate(1);
        verify(categoriaRepository).findById(1);
        verify(provedoresRepository).findById(99);
    }

    // ── eliminar ─────────────────────────────────────────────────────────────

    @Test
    void eliminarDebeEliminarLibroCuandoExiste() throws IOException {
        Path imagenExistente = uploadsPath.resolve("imagen-antigua.jpg").normalize();
        Files.writeString(imagenExistente, "imagen de prueba");

        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));
        doNothing().when(libroRepository).deleteById(1);

        libroService.eliminar(1);

        assertFalse(Files.exists(imagenExistente));

        verify(libroRepository).findById(1);
        verify(libroRepository).deleteById(1);
    }

    @Test
    void eliminarNoDebeFallarCuandoLibroNoExiste() {
        when(libroRepository.findById(99)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> libroService.eliminar(99));

        verify(libroRepository).findById(99);
        verify(libroRepository, never()).deleteById(anyInt());
    }

    @Test
    void eliminarDebePermitirImagenNull() {
        libro.setImagenUrl(null);
        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));
        doNothing().when(libroRepository).deleteById(1);

        assertDoesNotThrow(() -> libroService.eliminar(1));

        verify(libroRepository).findById(1);
        verify(libroRepository).deleteById(1);
    }

    @Test
    void eliminarDebePermitirImagenEnBlanco() {
        libro.setImagenUrl("   ");
        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));
        doNothing().when(libroRepository).deleteById(1);

        assertDoesNotThrow(() -> libroService.eliminar(1));

        verify(libroRepository).findById(1);
        verify(libroRepository).deleteById(1);
    }

    @Test
    void eliminarDebeLanzarExcepcionCuandoNombreArchivoNoEsPermitido() {
        libro.setImagenUrl("imagen|maliciosa.jpg");
        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> libroService.eliminar(1)
        );

        assertEquals("Nombre de archivo no permitido.", exception.getMessage());

        verify(libroRepository).findById(1);
        verify(libroRepository, never()).deleteById(anyInt());
    }

    // ── listarPorId / actualizarStock ─────────────────────────────────────────

    @Test
    void listarPorIdDebeRetornarLibroCuandoExiste() {
        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));

        Optional<Libro> resultado = libroService.listarPorId(1);

        assertTrue(resultado.isPresent());
        assertEquals("Cien años de soledad", resultado.get().getTitulo());

        verify(libroRepository).findById(1);
    }

    @Test
    void actualizarStockDebeModificarCantidad() {
        when(libroRepository.findForUpdate(1)).thenReturn(Optional.of(libro));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = libroService.actualizarStock(1, 25);

        assertNotNull(resultado);
        assertEquals(25, resultado.getStock());

        verify(libroRepository).findForUpdate(1);
        verify(libroRepository).save(libro);
    }

    @Test
    void actualizarStockDebeLanzarExcepcionCuandoLibroNoExiste() {
        when(libroRepository.findForUpdate(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> libroService.actualizarStock(99, 25)
        );

        assertEquals("Libro no encontrado", exception.getMessage());

        verify(libroRepository).findForUpdate(99);
        verify(libroRepository, never()).save(any(Libro.class));
    }

    // ── listarPorCategoria / listarPorAnio ────────────────────────────────────

    @Test
    void listarPorCategoriaDebeRetornarLibros() {
        when(libroRepository.findByCategoriaId(1)).thenReturn(List.of(libro));

        List<Libro> resultado = libroService.listarPorCategoria(1);

        assertEquals(1, resultado.size());

        verify(libroRepository).findByCategoriaId(1);
    }

    @Test
    void listarPorAnioDebeRetornarLibros() {
        when(libroRepository.findByAnio(ANIO_2024)).thenReturn(List.of(libro));

        List<Libro> resultado = libroService.listarPorAnio(ANIO_2024);

        assertEquals(1, resultado.size());

        verify(libroRepository).findByAnio(ANIO_2024);
    }

    @Test
    void listarDesdeAnioDebeRetornarLibrosPosteriores() {
        when(libroRepository.findByAnioAfter(ANIO_2020)).thenReturn(List.of(libro));

        List<Libro> resultado = libroService.listarDesdeAnio(ANIO_2020);

        assertEquals(1, resultado.size());

        verify(libroRepository).findByAnioAfter(ANIO_2020);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    // Firma actual: guardar(titulo, autor, stock, stockMinimo, precio, categoriaId, provedoresId, file, anio)
    private Libro guardarLibro(Integer categoriaId, Integer proveedorId, MultipartFile archivo) {
        return libroService.guardar(
                "Cien años de soledad",
                "Gabriel García Márquez",
                10,
                10,
                60.0,
                categoriaId,
                proveedorId,
                archivo,
                ANIO_1967
        );
    }

    // Firma actual: editar(id, titulo, autor, stock, stockMinimo, precio, categoriaId, provedoresId, file, anio, estado)
    private Libro editarLibro(Integer libroId, Integer categoriaId, Integer proveedorId, MultipartFile archivo) {
        return libroService.editar(
                libroId,
                "Libro actualizado",
                "Autor actualizado",
                20,
                10,
                80.0,
                categoriaId,
                proveedorId,
                archivo,
                ANIO_2020,
                "ACTIVO"
        );
    }

    private void eliminarArchivoTemporal(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Limpieza de archivos temporales generados por las pruebas.
        }
    }
}
