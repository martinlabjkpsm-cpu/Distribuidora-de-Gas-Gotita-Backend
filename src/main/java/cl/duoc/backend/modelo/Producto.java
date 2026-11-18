package cl.duoc.backend.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

    /** Código del negocio (CL001, RG002...). Único. */
    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    /** 'Unidad' o 'Kit' (CHECK ck_producto_unidad). */
    @Column(nullable = false, length = 20)
    private String unidad = "Unidad";

    /** Precios en pesos chilenos (enteros, > 0). */
    @Column(name = "precio_residencial", nullable = false)
    private int precioResidencial;

    @Column(name = "precio_comercial", nullable = false)
    private int precioComercial;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    /** Aparece en Ofertas. */
    @Column(nullable = false)
    private boolean destacado = false;

    /** Un producto ya vendido no se borra: se desactiva. */
    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    protected Producto() {
    }

    public Producto(String codigo, Categoria categoria, String nombre, String descripcion, String unidad,
                    int precioResidencial, int precioComercial, int stockActual, String imagenUrl, boolean destacado) {
        this.codigo = codigo;
        this.categoria = categoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.unidad = unidad;
        this.precioResidencial = precioResidencial;
        this.precioComercial = precioComercial;
        this.stockActual = stockActual;
        this.imagenUrl = imagenUrl;
        this.destacado = destacado;
    }

    /** El servidor decide el precio según la tarifa; el cliente no lo envía. */
    public int precioSegun(TipoTarifa tarifa) {
        return tarifa == TipoTarifa.COMERCIAL ? precioComercial : precioResidencial;
    }

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public int getPrecioResidencial() { return precioResidencial; }
    public void setPrecioResidencial(int precioResidencial) { this.precioResidencial = precioResidencial; }
    public int getPrecioComercial() { return precioComercial; }
    public void setPrecioComercial(int precioComercial) { this.precioComercial = precioComercial; }
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
