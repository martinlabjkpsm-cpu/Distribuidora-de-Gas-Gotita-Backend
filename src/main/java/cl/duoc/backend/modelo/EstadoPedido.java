package cl.duoc.backend.modelo;

/** Ciclo de vida de un pedido: PENDIENTE -> EN_CAMINO -> ENTREGADO (o CANCELADO mientras esté PENDIENTE). */
public enum EstadoPedido {
    PENDIENTE, EN_CAMINO, ENTREGADO, CANCELADO
}
