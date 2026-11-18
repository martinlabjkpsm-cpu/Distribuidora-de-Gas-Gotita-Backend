package cl.duoc.backend.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Clave primaria compuesta de detalle_pedido: (id_pedido, id_producto). Un producto una vez por pedido. */
@Embeddable
public class DetallePedidoId implements Serializable {

    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "id_producto")
    private Integer idProducto;

    protected DetallePedidoId() {
    }

    public DetallePedidoId(Integer idPedido, Integer idProducto) {
        this.idPedido = idPedido;
        this.idProducto = idProducto;
    }

    public Integer getIdPedido() { return idPedido; }
    public Integer getIdProducto() { return idProducto; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetallePedidoId otro)) return false;
        return Objects.equals(idPedido, otro.idPedido) && Objects.equals(idProducto, otro.idProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPedido, idProducto);
    }
}
