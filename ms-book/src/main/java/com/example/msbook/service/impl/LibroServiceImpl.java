/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.service.impl;

import com.example.msbook.entity.Categoria;
import com.example.msbook.entity.Libro;
import com.example.msbook.entity.Provedores;
import com.example.msbook.repository.CategoriaRepository;
import com.example.msbook.repository.LibroRepository;
import com.example.msbook.repository.ProvedoresRepository;
import com.example.msbook.service.LibroService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class LibroServiceImpl implements LibroService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "webp"
    );

    private static final Pattern SAFE_FILE_NAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProvedoresRepository provedoresRepository;

    private final Path rootLocation = Path.of(System.getenv().getOrDefault("UPLOADS_DIR", "uploads")).toAbsolutePath().normalize();

    public LibroServiceImpl(
            LibroRepository libroRepository,
            CategoriaRepository categoriaRepository,
            ProvedoresRepository provedoresRepository
    ) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
        this.provedoresRepository = provedoresRepository;

        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar la carpeta de almacenamiento.", e);
        }
    }

    @Override
    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    @Override
    public Libro guardar(String titulo, String autor, Integer stock, Integer stockMinimo, Double precio,
                         Integer categoriaId, Integer provedoresId,
                         MultipartFile file, LocalDate anio) {
        String imagenUrl = (file != null && !file.isEmpty()) ? storeFile(file) : null;

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        Provedores provedores = provedoresRepository.findById(provedoresId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        long count = libroRepository.count();
        String codigo = "PRD-" + String.format("%03d", count + 1);

        Libro libro = new Libro();
        libro.setCodigo(codigo);
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setStock(stock);
        libro.setStockMinimo(stockMinimo != null ? stockMinimo : 10);
        libro.setPrecio(precio);
        libro.setCategoria(categoria);
        libro.setProvedores(provedores);
        libro.setImagenUrl(imagenUrl);
        libro.setAnio(anio);

        return libroRepository.save(libro);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Libro editar(Integer id, String titulo, String autor, Integer stock, Integer stockMinimo, Double precio,
                        Integer categoriaId, Integer provedoresId,
                        MultipartFile file, LocalDate anio, String estado) {
        Libro libroExistente = libroRepository.findForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));

        if (file != null && !file.isEmpty()) {
            deleteFile(libroExistente.getImagenUrl());
            String imagenUrl = storeFile(file);
            libroExistente.setImagenUrl(imagenUrl);
        }

        if (categoriaId != null && categoriaId > 0) {
            Categoria categoria = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
            libroExistente.setCategoria(categoria);
        }

        if (provedoresId != null && provedoresId > 0) {
            Provedores provedores = provedoresRepository.findById(provedoresId)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
            libroExistente.setProvedores(provedores);
        }

        libroExistente.setTitulo(titulo);
        libroExistente.setAutor(autor);
        libroExistente.setStock(stock);
        libroExistente.setStockMinimo(stockMinimo != null ? stockMinimo : libroExistente.getStockMinimo());
        libroExistente.setPrecio(precio);
        libroExistente.setAnio(anio);
        if (estado != null && !estado.isBlank()) libroExistente.setEstado(estado);

        return libroRepository.save(libroExistente);
    }

    @Override
    public void eliminar(Integer id) {
        Optional<Libro> optionalLibro = libroRepository.findById(id);

        if (optionalLibro.isPresent()) {
            Libro libroExistente = optionalLibro.get();
            deleteFile(libroExistente.getImagenUrl());
            libroRepository.deleteById(id);
        }
    }

    @Override
    public Optional<Libro> listarPorId(Integer id) {
        return libroRepository.findById(id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Libro actualizarStock(Integer id, Integer cantidad) {
        Libro libro = libroRepository.findForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));

        libro.setStock(cantidad);
        return libroRepository.save(libro);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Libro actualizarStockMovimiento(Integer id, Integer cantidad, String tipo) {
        Libro libro = libroRepository.findForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));
        int nuevoStock = "ENTRADA".equalsIgnoreCase(tipo)
                ? libro.getStock() + cantidad
                : libro.getStock() - cantidad;
        if (nuevoStock < 0) {
            throw new IllegalStateException("Stock insuficiente. Stock actual: " + libro.getStock());
        }
        libro.setStock(nuevoStock);
        return libroRepository.save(libro);
    }

    @Override
    public List<Libro> listarPorCategoria(Integer categoriaId) {
        return libroRepository.findByCategoriaId(categoriaId);
    }

    @Override
    public List<Libro> listarPorAnio(LocalDate anio) {
        return libroRepository.findByAnio(anio);
    }

    @Override
    public List<Libro> listarDesdeAnio(LocalDate anio) {
        return libroRepository.findByAnioAfter(anio);
    }

    private String storeFile(MultipartFile file) {
        try {
            validarArchivo(file);

            String extension = obtenerExtensionPermitida(file);
            String fileName = generarNombreSeguro(extension);

            Files.createDirectories(rootLocation);

            Path destinationFile = rootLocation.resolve(fileName).normalize();

            validarRutaSegura(destinationFile);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen del libro.", e);
        }
    }

    private void deleteFile(String fileName) {
        try {
            if (fileName == null || fileName.isBlank()) {
                return;
            }

            String safeFileName = obtenerNombreSeguroGuardado(fileName);
            Path filePath = rootLocation.resolve(safeFileName).normalize();

            validarRutaSegura(filePath);

            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo eliminar la imagen del libro.", e);
        }
    }

    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No se puede guardar un archivo vacío.");
        }
    }

    private String obtenerExtensionPermitida(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("El archivo no tiene nombre válido.");
        }

        String nombreBase = extraerNombreBase(originalFilename);
        int dotIndex = nombreBase.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == nombreBase.length() - 1) {
            throw new IllegalArgumentException("El archivo no tiene extensión válida.");
        }

        String extension = nombreBase.substring(dotIndex + 1).toLowerCase(Locale.ROOT);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo inválido. Solo se permiten imágenes JPG, JPEG, PNG o WEBP."
            );
        }

        return extension;
    }

    private String generarNombreSeguro(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private String obtenerNombreSeguroGuardado(String fileName) {
        String nombreBase = extraerNombreBase(fileName);

        if (!SAFE_FILE_NAME_PATTERN.matcher(nombreBase).matches()) {
            throw new SecurityException("Nombre de archivo no permitido.");
        }

        return nombreBase;
    }

    private String extraerNombreBase(String fileName) {
        String normalizado = fileName.replace('\\', '/');
        int slashIndex = normalizado.lastIndexOf('/');

        if (slashIndex >= 0) {
            return normalizado.substring(slashIndex + 1);
        }

        return normalizado;
    }

    private void validarRutaSegura(Path filePath) {
        if (!filePath.startsWith(rootLocation)) {
            throw new SecurityException("Ruta de archivo no permitida.");
        }
    }
}