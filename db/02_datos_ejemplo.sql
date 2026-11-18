-- =====================================================================
--  Distribuidora de Gas Gotita · Datos de ejemplo
--  Ejecutar DESPUÉS de 01_crear_tablas.sql:
--    psql -h <DB_HOST> -U <DB_USER> -d gotita_db -f 02_datos_ejemplo.sql
--  Catálogo y zonas sacados de "Catalogo Distribuidora Gas El Volcan.xlsx".
--  Las FK se buscan por nombre/código, así no dependen de los id generados.
-- =====================================================================

BEGIN;

-- ----------------------------- Roles ---------------------------------
INSERT INTO rol (nombre, descripcion) VALUES
  ('ADMINISTRADOR', 'Gestiona productos, usuarios y reportes'),
  ('OPERADOR',      'Gestiona pedidos y asigna repartidores'),
  ('REPARTIDOR',    'Entrega pedidos y reporta su ubicación'),
  ('CLIENTE',       'Compra productos y sigue sus pedidos');

-- ---------------------------- Categorías -----------------------------
INSERT INTO categoria (nombre) VALUES
  ('Cilindros de Gas'),
  ('Reguladores'),
  ('Mangueras y Conexiones'),
  ('Accesorios');

-- ------------------------- Zonas de despacho -------------------------
INSERT INTO zona_despacho (nombre, cobertura, dias_despacho, hora_inicio, hora_fin, tiempo_estimado) VALUES
  ('Zona Centro',    'Chillán (sectores centro, norte y sur)',     'Lunes a Sábado',  '08:00', '20:00', '1 – 3 horas'),
  ('Zona Oriente',   'Chillán (sector oriente), Chillán Viejo',    'Lunes a Viernes', '08:00', '18:00', '2 – 4 horas'),
  ('Zona Rural',     'El Carmen, Pinto, San Ignacio',              'Martes y Jueves', '08:00', '16:00', '3 – 6 horas'),
  ('Zona Sur',       'Bulnes, Quillón',                            'Miércoles',       '08:00', '16:00', '4 – 6 horas'),
  ('Zona Comercial', 'Parques industriales y locales comerciales', 'Lunes a Viernes', '07:00', '17:00', 'Según agenda');

-- ---------------------------- Productos ------------------------------
INSERT INTO producto (codigo, nombre, descripcion, unidad, precio_residencial, precio_comercial, stock_actual, destacado, id_categoria)
SELECT v.codigo, v.nombre, v.descripcion, v.unidad, v.p_res, v.p_com, v.stock, v.destacado, c.id_categoria
FROM (VALUES
  ('CL001', 'Cilindro GLP 5 kg',  'Cilindro de gas licuado de petróleo 5 kg. Para uso residencial (cocina, calefacción pequeña).', 'Unidad', 6500, 6000, 80, FALSE, 'Cilindros de Gas'),
  ('CL002', 'Cilindro GLP 11 kg', 'Cilindro estándar doméstico. El más utilizado en hogares chilenos. Compatible con reguladores estándar.', 'Unidad', 12000, 11000, 200, TRUE, 'Cilindros de Gas'),
  ('CL003', 'Cilindro GLP 15 kg', 'Cilindro de mayor capacidad para hogares de alto consumo o locales pequeños.', 'Unidad', 16000, 14500, 90, FALSE, 'Cilindros de Gas'),
  ('CL004', 'Cilindro GLP 45 kg', 'Cilindro industrial. Uso comercial: restaurantes, talleres, calefacción de locales.', 'Unidad', 45000, 40000, 30, FALSE, 'Cilindros de Gas'),
  ('RG001', 'Regulador doméstico estándar', 'Regulador de 1 etapa para cilindros 5, 11 y 15 kg. Presión de salida 28 mbar.', 'Unidad', 8990, 8200, 45, FALSE, 'Reguladores'),
  ('RG002', 'Regulador de alta presión', 'Regulador para cocinas industriales o equipos de mayor consumo. Presión regulable.', 'Unidad', 18990, 17000, 12, FALSE, 'Reguladores'),
  ('RG003', 'Regulador dual (2 salidas)', 'Permite conectar dos artefactos simultáneamente al mismo cilindro.', 'Unidad', 14990, 13500, 18, FALSE, 'Reguladores'),
  ('MG001', 'Manguera gas 1.5 m', 'Manguera flexible homologada. Diámetro interior 9mm. Compatible con reguladores estándar.', 'Unidad', 3990, 3500, 80, FALSE, 'Mangueras y Conexiones'),
  ('MG002', 'Manguera gas 3 m', 'Manguera larga para instalaciones donde el artefacto está alejado del cilindro.', 'Unidad', 6990, 6200, 50, FALSE, 'Mangueras y Conexiones'),
  ('MG003', 'Abrazadera metálica', 'Abrazadera de acero para asegurar la conexión manguera-regulador y manguera-artefacto.', 'Unidad', 990, 800, 200, FALSE, 'Mangueras y Conexiones'),
  ('MG004', 'Kit conexión completo (regulador + manguera 1.5m + abrazaderas)', 'Todo lo necesario para instalar un cilindro nuevo.', 'Kit', 12990, 11500, 25, TRUE, 'Mangueras y Conexiones'),
  ('AC001', 'Carro porta cilindro 11/15 kg', 'Carro metálico con ruedas para transportar cilindros dentro del hogar con seguridad.', 'Unidad', 12990, 11000, 20, FALSE, 'Accesorios'),
  ('AC002', 'Tapa protectora para válvula', 'Tapa de plástico ABS para proteger la válvula del cilindro durante el transporte.', 'Unidad', 1490, 1200, 60, FALSE, 'Accesorios'),
  ('AC003', 'Detector de gas a batería', 'Sensor electroquímico. Alarma sonora y visual ante fuga de gas GLP o metano.', 'Unidad', 19990, 17000, 8, TRUE, 'Accesorios')
) AS v(codigo, nombre, descripcion, unidad, p_res, p_com, stock, destacado, categoria)
JOIN categoria c ON c.nombre = v.categoria;

-- ----------------------------- Usuarios ------------------------------
-- password_hash es un TEXTO DE RELLENO, no un hash válido: con estos usuarios
-- no se puede iniciar sesión. Para probar login, registra usuarios desde la
-- API (que genera el hash BCrypt) o reemplaza este valor por un hash real.
INSERT INTO usuario (rut, nombre, email, telefono, password_hash, id_rol)
SELECT v.rut, v.nombre, v.email, v.telefono, 'HASH_DE_EJEMPLO_REEMPLAZAR', r.id_rol
FROM (VALUES
  ('11111111-1', 'Admin Gotita',      'admin@gotita.cl',      '+56911111111', 'ADMINISTRADOR'),
  ('22222222-2', 'Operador Gotita',   'operador@gotita.cl',   '+56922222222', 'OPERADOR'),
  ('33333333-3', 'Repartidor Gotita', 'repartidor@gotita.cl', '+56933333333', 'REPARTIDOR'),
  ('44444444-4', 'Cliente Ejemplo',   'cliente@gotita.cl',    '+56944444444', 'CLIENTE')
) AS v(rut, nombre, email, telefono, rol)
JOIN rol r ON r.nombre = v.rol;

-- ------------------------------ Pedidos ------------------------------
-- PED-1001: entregado (con repartidor, historial completo y pago)
-- PED-1002: pendiente (sin repartidor asignado)
INSERT INTO pedido (codigo, direccion, latitud_entrega, longitud_entrega, indicaciones,
                    tipo_tarifa, metodo_pago, estado, total, id_repartidor, id_cliente, id_zona)
VALUES
  ('PED-1001', 'Av. Libertad 550, Chillán', -36.606700, -72.103400, 'Casa con reja verde',
   'RESIDENCIAL', 'EFECTIVO', 'ENTREGADO', 32990,
   (SELECT id_usuario FROM usuario WHERE email = 'repartidor@gotita.cl'),
   (SELECT id_usuario FROM usuario WHERE email = 'cliente@gotita.cl'),
   (SELECT id_zona FROM zona_despacho WHERE nombre = 'Zona Centro')),
  ('PED-1002', 'Calle Arauco 120, Chillán Viejo', -36.623000, -72.131500, NULL,
   'RESIDENCIAL', 'TRANSFERENCIA', 'PENDIENTE', 16000,
   NULL,
   (SELECT id_usuario FROM usuario WHERE email = 'cliente@gotita.cl'),
   (SELECT id_zona FROM zona_despacho WHERE nombre = 'Zona Oriente'));

-- total = suma de cantidad × precio_unitario
--   PED-1001: 2 × 12000 (CL002) + 1 × 8990 (RG001) = 32990
--   PED-1002: 1 × 16000 (CL003)                    = 16000
INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario)
SELECT p.id_pedido, pr.id_producto, v.cantidad, v.precio
FROM (VALUES
  ('PED-1001', 'CL002', 2, 12000),
  ('PED-1001', 'RG001', 1,  8990),
  ('PED-1002', 'CL003', 1, 16000)
) AS v(pedido, producto, cantidad, precio)
JOIN pedido   p  ON p.codigo  = v.pedido
JOIN producto pr ON pr.codigo = v.producto;

-- Stock descontado por los pedidos de ejemplo (como lo haría el servidor)
UPDATE producto SET stock_actual = stock_actual - 2 WHERE codigo = 'CL002';
UPDATE producto SET stock_actual = stock_actual - 1 WHERE codigo = 'RG001';
UPDATE producto SET stock_actual = stock_actual - 1 WHERE codigo = 'CL003';

INSERT INTO historial_estado_pedido (estado, fecha_hora, id_pedido)
SELECT v.estado, CURRENT_TIMESTAMP + v.desfase, p.id_pedido
FROM (VALUES
  ('PED-1001', 'PENDIENTE', INTERVAL '0 minutes'),
  ('PED-1001', 'EN CAMINO', INTERVAL '20 minutes'),
  ('PED-1001', 'ENTREGADO', INTERVAL '65 minutes'),
  ('PED-1002', 'PENDIENTE', INTERVAL '0 minutes')
) AS v(pedido, estado, desfase)
JOIN pedido p ON p.codigo = v.pedido;

INSERT INTO pago (monto, comprobante, id_pedido)
SELECT total, NULL, id_pedido FROM pedido WHERE codigo = 'PED-1001';

INSERT INTO ubicacion_repartidor (latitud, longitud, id_repartidor)
SELECT -36.606700, -72.103400, id_usuario FROM usuario WHERE email = 'repartidor@gotita.cl';

COMMIT;
