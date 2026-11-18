package cl.duoc.backend.servicio;

import cl.duoc.backend.dto.CatalogoDtos.CategoriaRequest;
import cl.duoc.backend.dto.CatalogoDtos.CategoriaResponse;
import cl.duoc.backend.dto.CatalogoDtos.ProductoRequest;
import cl.duoc.backend.dto.CatalogoDtos.ProductoResponse;
import cl.duoc.backend.dto.CatalogoDtos.ZonaResponse;
import cl.duoc.backend.excepcion.ConflictoException;
import cl.duoc.backend.excepcion.NoEncontradoException;
import cl.duoc.backend.excepcion.SolicitudInvalidaException;
import cl.duoc.backend.modelo.Categoria;
import cl.duoc.backend.modelo.Producto;
import cl.duoc.backend.repositorio.CategoriaRepository;
import cl.duoc.backend.repositorio.DetallePedidoRepository;
import cl.duoc.backend.repositorio.ProductoRepository;
import cl.duoc.backend.repositorio.ZonaDespachoRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas del catálogo: códigos y nombres únicos, precios > 0, stock >= 0, no borrar lo que está en uso. */
@Service
public class CatalogoService {

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final DetallePedidoRepository detalles;
    private final ZonaDespachoRepository zonas;

    public CatalogoService(CategoriaRepository categorias, ProductoRepository productos,
                           DetallePedidoRepository detalles, ZonaDespachoRepository zonas) {
        this.categorias = categorias;
        this.productos = productos;
        this.detalles = detalles;
        this.zonas = zonas;
    }

    // ---------- Zonas de despacho ----------

    @Transactional(readOnly = true)
    public List<ZonaResponse> listarZonas() {
        return zonas.findAll(Sort.by("id")).stream().map(ZonaResponse::de).toList();
    }

    // ---------- Categorías ----------

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categorias.findAll(Sort.by("id")).stream().map(CategoriaResponse::de).toList();
    }

    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest req) {
        String nombre = req.nombre().trim();
        if (categorias.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe la categoría " + nombre);
        }
        return CategoriaResponse.de(categorias.save(new Categoria(nombre)));
    }

    /** Renombrar la categoría basta: los productos apuntan a ella por FK, no por nombre. */
    @Transactional
    public CategoriaResponse renombrarCategoria(Integer id, CategoriaRequest req) {
        Categoria c = categorias.findById(id).orElseThrow(() -> new NoEncontradoException("No existe la categoría " + id));
        String nombre = req.nombre().trim();
        categorias.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> { throw new ConflictoException("Ya existe la categoría " + nombre); });
        c.setNombre(nombre);
        return CategoriaResponse.de(c);
    }

    @Transactional
    public void eliminarCategoria(Integer id) {
        Categoria c = categorias.findById(id).orElseThrow(() -> new NoEncontradoException("No existe la categoría " + id));
        if (productos.countByCategoriaId(id) > 0) {
            throw new ConflictoException("No se puede eliminar: la categoría tiene productos");
        }
        categorias.delete(c);
    }

    // ---------- Productos ----------

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductos(String categoria) {
        List<Producto> lista = (categoria == null || categoria.isBlank())
                ? productos.listarActivos()
                : productos.listarActivosPorCategoria(categoria.trim());
        return lista.stream().map(ProductoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProducto(String codigo) {
        return ProductoResponse.de(buscarProducto(codigo));
    }

    @Transactional
    public ProductoResponse crearProducto(ProductoRequest req) {
        String codigo = req.codigo().trim().toUpperCase();
        if (productos.existsByCodigo(codigo)) {
            throw new ConflictoException("Ya existe un producto con el código " + codigo);
        }
        Producto p = new Producto(codigo, categoriaPorNombre(req.categoria()), req.nombre().trim(), req.descripcion(),
                unidadODefecto(req.unidad()), req.precioResidencial(), req.precioComercial(), req.stockActual(),
                req.imagenSrc(), Boolean.TRUE.equals(req.destacado()));
        return ProductoResponse.de(productos.save(p));
    }

    /** El código identifica al producto: si el del cuerpo no coincide con el de la URL, es un 400. */
    @Transactional
    public ProductoResponse actualizarProducto(String codigo, ProductoRequest req) {
        Producto p = buscarProducto(codigo);
        if (!p.getCodigo().equalsIgnoreCase(req.codigo().trim())) {
            throw new SolicitudInvalidaException("El código del producto no se puede cambiar");
        }
        p.setCategoria(categoriaPorNombre(req.categoria()));
        p.setNombre(req.nombre().trim());
        p.setDescripcion(req.descripcion());
        p.setUnidad(unidadODefecto(req.unidad()));
        p.setPrecioResidencial(req.precioResidencial());
        p.setPrecioComercial(req.precioComercial());
        p.setStockActual(req.stockActual());
        p.setImagenUrl(req.imagenSrc());
        p.setDestacado(Boolean.TRUE.equals(req.destacado()));
        p.setActivo(true);
        return ProductoResponse.de(p);
    }

    /**
     * Si el producto nunca se vendió, se borra. Si aparece en pedidos, se DESACTIVA (activo = false):
     * deja de mostrarse en el catálogo pero el historial de pedidos se conserva.
     */
    @Transactional
    public void eliminarProducto(String codigo) {
        Producto p = buscarProducto(codigo);
        if (detalles.existsByProductoId(p.getId())) {
            p.setActivo(false);
        } else {
            productos.delete(p);
        }
    }

    private Producto buscarProducto(String codigo) {
        return productos.buscarPorCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new NoEncontradoException("No existe el producto " + codigo));
    }

    private Categoria categoriaPorNombre(String nombre) {
        return categorias.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new SolicitudInvalidaException("La categoría " + nombre + " no existe"));
    }

    private static String unidadODefecto(String unidad) {
        return (unidad == null || unidad.isBlank()) ? "Unidad" : unidad.trim();
    }
}
