package cl.duoc.backend.modelo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Traduce EN_CAMINO (Java) <-> 'EN CAMINO' (columna estado de pedido e historial_estado_pedido). */
@Converter(autoApply = true)
public class EstadoPedidoConverter implements AttributeConverter<EstadoPedido, String> {

    @Override
    public String convertToDatabaseColumn(EstadoPedido estado) {
        return estado == null ? null : estado.getValorBD();
    }

    @Override
    public EstadoPedido convertToEntityAttribute(String valor) {
        return valor == null ? null : EstadoPedido.desdeBD(valor);
    }
}
