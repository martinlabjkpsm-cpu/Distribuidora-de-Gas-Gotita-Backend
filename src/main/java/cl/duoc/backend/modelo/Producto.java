package cl.duoc.backend.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "producto")
public class Producto {

    /** Código del negocio (CL001, RG002...). Es la clave primaria y no se repite. */
    @Id
    @Column(length = 10)
    private String codigo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String unidad = "Unidad";

    /** Precios en pesos chilenos (enteros, sin decimales). */
    @Column(name = "precio_residencial", nullable = false)
    private int precioResidencial;

    @Column(name = "precio_comercial", nullable = false)
    private int precioComercial;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual;

    @Column(name = "imagen_src", length = 200)
    private String imagenSrc;

    protected Producto() {
    }

    public Producto(String codigo, Categoria categoria, String nombre, String descripcion, String unidad,
                    int precioResidencial, int precioComercial, int stockActual, String imagenSrc) {
        this.codigo = codigo;
        this.categoria = categoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.unidad = unidad;
        this.precioResidencial = precioResidencial;
        this.precioComercial = precioComercial;
        this.stockActual = stockActual;
        this.imagenSrc = imagenSrc;
    }

    /** El servidor decide el precio según la tarifa; el cliente no lo envía. */
    public int precioSegun(TipoTarifa tarifa) {
        return tarifa == TipoTarifa.COMERCIAL ? precioComercial : precioResidencial;
    }

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
    public String getImagenSrc() { return imagenSrc; }
    public void setImagenSrc(String imagenSrc) { this.imagenSrc = imagenSrc; }
}
