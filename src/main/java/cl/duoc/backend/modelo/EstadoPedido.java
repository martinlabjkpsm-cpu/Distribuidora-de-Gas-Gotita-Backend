package cl.duoc.backend.modelo;

/**
 * Ciclo de vida de un pedido: PENDIENTE -> EN_CAMINO -> ENTREGADO (o CANCELADO mientras esté PENDIENTE).
 * En la base se guarda con el texto del CHECK ('EN CAMINO' lleva espacio): ver EstadoPedidoConverter.
 */
public enum EstadoPedido {
    PENDIENTE("PENDIENTE"),
    EN_CAMINO("EN CAMINO"),
    ENTREGADO("ENTREGADO"),
    CANCELADO("CANCELADO");

    private final String valorBD;

    EstadoPedido(String valorBD) {
        this.valorBD = valorBD;
    }

    public String getValorBD() {
        return valorBD;
    }

    public static EstadoPedido desdeBD(String valor) {
        for (EstadoPedido e : values()) {
            if (e.valorBD.equals(valor)) return e;
        }
        throw new IllegalArgumentException("Estado desconocido en la base: " + valor);
    }
}
