/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.controller;

import com.example.msbook.entity.Libro;
import com.example.msbook.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@Tag(
        name = "Libros",
        description = "Operaciones para registrar, listar, editar, eliminar, filtrar libros y actualizar stock."
)
@RestController
@RequestMapping("/libros")
public class LibroController {

    private static final Pattern SAFE_FILE_NAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final Path rootLocation = Path.of(System.getenv().getOrDefault("UPLOADS_DIR", "uploads")).toAbsolutePath().normalize();
    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @Operation(
            summary = "Listar libros",
            description = "Obtiene la lista completa de libros registrados en el catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libros obtenidos correctamente."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping
    public List<Libro> listarLibros() {
        return libroService.listar();
    }

    @Operation(
            summary = "Crear libro",
            description = """
                    Registra un nuevo libro en el catálogo. Este endpoint recibe datos mediante
                    formulario multipart/form-data porque permite cargar una imagen asociada al libro.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro creado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para crear el libro."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Libro> crearLibro(
            @Parameter(description = "Título del libro", required = true)
            @RequestParam("titulo") String titulo,

            @Parameter(description = "Autor del libro", required = true)
            @RequestParam("autor") String autor,

            @Parameter(description = "Cantidad disponible en stock", required = true)
            @RequestParam("stock") Integer stock,

            @Parameter(description = "Stock mínimo para alertas", required = false)
            @RequestParam(value = "stockMinimo", required = false, defaultValue = "10") Integer stockMinimo,

            @Parameter(description = "Precio unitario del libro", required = true)
            @RequestParam("precio") Double precio,

            @Parameter(description = "ID de la categoría asociada al libro", required = true)
            @RequestParam("categoriaId") Integer categoriaId,

            @Parameter(description = "ID del proveedor asociado al libro", required = true)
            @RequestParam("provedoresId") Integer provedoresId,

            @Parameter(description = "Año de publicación o registro del libro", required = true)
            @RequestParam("anio") int anio,

            @Parameter(description = "Imagen asociada al libro. Campo opcional.", required = false)
            @RequestParam(value = "imagen", required = false) MultipartFile imagen
    ) {
        LocalDate fechaAnio = LocalDate.of(anio, 1, 1);

        Libro savedLibro = libroService.guardar(
                titulo,
                autor,
                stock,
                stockMinimo,
                precio,
                categoriaId,
                provedoresId,
                imagen,
                fechaAnio
        );

        return ResponseEntity.ok(savedLibro);
    }

    @Operation(
            summary = "Editar libro",
            description = """
                    Actualiza los datos de un libro existente. Puede recibir una nueva imagen,
                    pero el parámetro imagen no es obligatorio.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para actualizar el libro."),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Libro> editarLibro(
            @Parameter(description = "ID del libro que será actualizado", required = true)
            @PathVariable Integer id,

            @Parameter(description = "Título actualizado del libro", required = true)
            @RequestParam("titulo") String titulo,

            @Parameter(description = "Autor actualizado del libro", required = true)
            @RequestParam("autor") String autor,

            @Parameter(description = "Stock actualizado del libro", required = true)
            @RequestParam("stock") Integer stock,

            @Parameter(description = "Stock mínimo para alertas", required = false)
            @RequestParam(value = "stockMinimo", required = false, defaultValue = "10") Integer stockMinimo,

            @Parameter(description = "Precio actualizado del libro", required = true)
            @RequestParam("precio") Double precio,

            @Parameter(description = "ID de la categoría actualizada", required = false)
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,

            @Parameter(description = "ID del proveedor actualizado", required = false)
            @RequestParam(value = "provedoresId", required = false) Integer provedoresId,

            @Parameter(description = "Año actualizado del libro", required = true)
            @RequestParam("anio") int anio,

            @Parameter(description = "Nueva imagen del libro. Campo opcional.", required = false)
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,

            @Parameter(description = "Estado del producto: ACTIVO o INACTIVO", required = false)
            @RequestParam(value = "estado", required = false) String estado
    ) {
        LocalDate fechaAnio = LocalDate.of(anio, 1, 1);

        Libro updatedLibro = libroService.editar(
                id,
                titulo,
                autor,
                stock,
                stockMinimo,
                precio,
                categoriaId,
                provedoresId,
                imagen,
                fechaAnio,
                estado
        );

        return ResponseEntity.ok(updatedLibro);
    }

    @Operation(
            summary = "Eliminar libro",
            description = "Elimina un libro del catálogo según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Libro eliminado correctamente."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLibro(
            @Parameter(description = "ID del libro que será eliminado", required = true)
            @PathVariable Integer id
    ) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Buscar libro por ID",
            description = "Obtiene la información de un libro específico según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro encontrado correctamente."),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Libro> listarlibroId(
            @Parameter(description = "ID del libro consultado", required = true)
            @PathVariable Integer id
    ) {
        return libroService.listarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Actualizar stock de libro",
            description = "Actualiza la cantidad disponible en stock de un libro específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado correctamente."),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PutMapping("/{id}/stock")
    public ResponseEntity<Libro> actualizarStock(
            @Parameter(description = "ID del libro cuyo stock será actualizado", required = true)
            @PathVariable Integer id,

            @Parameter(description = "Cantidad que será aplicada al stock", required = true)
            @RequestParam Integer cantidad
    ) {
        Libro libro = libroService.actualizarStock(id, cantidad);
        return ResponseEntity.ok(libro);
    }

    @Operation(
            summary = "Movimiento de inventario (entrada/salida)",
            description = "Suma o resta al stock actual según el tipo de movimiento. SALIDA valida que el stock no quede negativo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente para la salida."),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado.")
    })
    @PutMapping("/{id}/inventario")
    public ResponseEntity<Libro> registrarMovimiento(
            @Parameter(description = "ID del libro", required = true)
            @PathVariable Integer id,

            @Parameter(description = "Cantidad a sumar o restar", required = true)
            @RequestParam Integer cantidad,

            @Parameter(description = "Tipo de movimiento: ENTRADA o SALIDA", required = true)
            @RequestParam String tipo
    ) {
        Libro libro = libroService.actualizarStockMovimiento(id, cantidad, tipo);
        return ResponseEntity.ok(libro);
    }

    @Operation(
            summary = "Listar libros por categoría",
            description = "Obtiene los libros asociados a una categoría específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libros por categoría obtenidos correctamente."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/categoria/{categoriaId}")
    public List<Libro> listarLibrosPorCategoria(
            @Parameter(description = "ID de la categoría consultada", required = true)
            @PathVariable Integer categoriaId
    ) {
        return libroService.listarPorCategoria(categoriaId);
    }

    @Operation(
            summary = "Listar libros por año",
            description = "Obtiene los libros registrados o asociados a un año específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libros por año obtenidos correctamente."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/anio/{anio}")
    public List<Libro> listarLibrosPorAnio(
            @Parameter(description = "Año consultado", required = true, example = "2024")
            @PathVariable int anio
    ) {
        return libroService.listarPorAnio(LocalDate.of(anio, 1, 1));
    }

    @Operation(
            summary = "Listar libros desde un año",
            description = "Obtiene los libros registrados desde el año indicado en adelante."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libros obtenidos correctamente desde el año indicado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/anio/desde/{anio}")
    public List<Libro> listarLibrosDesdeAnio(
            @Parameter(description = "Año inicial de búsqueda", required = true, example = "2020")
            @PathVariable int anio
    ) {
        return libroService.listarDesdeAnio(LocalDate.of(anio, 1, 1));
    }

    @Operation(
            summary = "Obtener imagen de libro",
            description = "Permite acceder a una imagen almacenada en la carpeta uploads según el nombre del archivo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo encontrado correctamente."),
            @ApiResponse(responseCode = "400", description = "Nombre de archivo inválido."),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado o no legible."),
            @ApiResponse(responseCode = "500", description = "Error interno al leer el archivo.")
    })
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "Nombre del archivo de imagen solicitado", required = true)
            @PathVariable String filename
    ) {
        String safeFileName = validarNombreArchivo(filename);

        try {
            Path file = rootLocation.resolve(safeFileName).normalize();

            if (!file.startsWith(rootLocation)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ruta de archivo no permitida."
                );
            }

            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se pudo encontrar o leer el archivo solicitado."
                );
            }

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\""
                    )
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "La ruta del archivo no es válida.",
                    e
            );
        }
    }

    private String validarNombreArchivo(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de archivo inválido."
            );
        }

        if (!SAFE_FILE_NAME_PATTERN.matcher(filename).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de archivo no permitido."
            );
        }

        return filename;
    }
}