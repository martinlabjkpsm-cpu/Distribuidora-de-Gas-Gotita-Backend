package cl.duoc.backend.controlador;

import cl.duoc.backend.dto.PedidoDtos.AsignarRepartidorRequest;
import cl.duoc.backend.dto.PedidoDtos.CambiarEstadoRequest;
import cl.duoc.backend.dto.PedidoDtos.CrearPedidoRequest;
import cl.duoc.backend.dto.PedidoDtos.PedidoResponse;
import cl.duoc.backend.dto.PedidoDtos.RendicionResponse;
import cl.duoc.backend.modelo.EstadoPedido;
import cl.duoc.backend.servicio.PedidoService;
import cl.duoc.backend.servicio.UsuarioActualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pedidos", description = "Operación principal: pedido de gas, asignación y entrega")
public class PedidoController {

    private final PedidoService pedidos;
    private final UsuarioActualService usuarioActual;

    public PedidoController(PedidoService pedidos, UsuarioActualService usuarioActual) {
        this.pedidos = pedidos;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
    @Operation(summary = "Crear pedido", description = "CLIENTE o ADMINISTRADOR. El servidor toma el cliente del token, "
            + "calcula precios y total, y descuenta stock. 409 si no hay stock suficiente.")
    public PedidoResponse crear(@Valid @RequestBody CrearPedidoRequest req, Authentication auth) {
        return pedidos.crear(req, usuarioActual.obtener(auth));
    }

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "CLIENTE ve los suyos, REPARTIDOR los asignados, OPERADOR y ADMINISTRADOR todos. Filtro opcional ?estado=PENDIENTE")
    public List<PedidoResponse> listar(@RequestParam(required = false) EstadoPedido estado, Authentication auth) {
        return pedidos.listar(usuarioActual.obtener(auth), estado);
    }

    @GetMapping("/rendicion")
    @PreAuthorize("hasRole('REPARTIDOR')")
    @Operation(summary = "Dinero a rendir", description = "REPARTIDOR. Suma de sus pedidos ENTREGADO.")
    public RendicionResponse rendicion(Authentication auth) {
        return pedidos.rendicion(usuarioActual.obtener(auth));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un pedido", description = "403 si el pedido no es del cliente o no está asignado al repartidor. 404 si no existe.")
    public PedidoResponse obtener(@PathVariable Long id, Authentication auth) {
        return pedidos.obtener(id, usuarioActual.obtener(auth));
    }

    @PatchMapping("/{id}/repartidor")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMINISTRADOR')")
    @Operation(summary = "Asignar repartidor", description = "OPERADOR o ADMINISTRADOR. Solo pedidos PENDIENTE (409 si no).")
    public PedidoResponse asignar(@PathVariable Long id, @Valid @RequestBody AsignarRepartidorRequest req) {
        return pedidos.asignarRepartidor(id, req.rutRepartidor());
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('REPARTIDOR','OPERADOR','ADMINISTRADOR')")
    @Operation(summary = "Cambiar estado", description = "PENDIENTE->EN_CAMINO->ENTREGADO; CANCELADO solo desde PENDIENTE. "
            + "El REPARTIDOR solo sobre sus pedidos y no puede cancelar. 409 si la transición no es válida.")
    public PedidoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest req, Authentication auth) {
        return pedidos.cambiarEstado(id, req.estado(), usuarioActual.obtener(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar pedido", description = "ADMINISTRADOR. Si no estaba entregado, devuelve el stock.")
    public void eliminar(@PathVariable Long id) {
        pedidos.eliminar(id);
    }
}
