-- =====================================================================
--  Distribuidora de Gas Gotita · Script de creación de tablas
--  Basado en: 4.0 DER_Distribuidora_Gas_Gotita (10 entidades)
--  Motor: PostgreSQL 18 (Amazon RDS · base gotita_db)
--
--  Ejecutar:  psql -h <DB_HOST> -U <DB_USER> -d gotita_db -f 01_crear_tablas.sql
--
--  Equivalencias MySQL → PostgreSQL usadas:
--    INT AUTO_INCREMENT   → INT GENERATED ALWAYS AS IDENTITY
--    DEFAULT ahora        → DEFAULT CURRENT_TIMESTAMP
--  Los nombres van en minúscula (PostgreSQL los guarda así si no llevan comillas).
--  No toca la tabla "pedidos" que usa la Lambda (esta se llama "pedido").
-- =====================================================================

BEGIN;  -- si algo falla, no se crea nada a medias

-- ---------------------------------------------------------------------
-- (Opcional) Borrar todo y volver a crear. Descomentar SOLO si quieres
-- reiniciar desde cero: elimina también los datos.
-- ---------------------------------------------------------------------
-- DROP TABLE IF EXISTS historial_estado_pedido, pago, ubicacion_repartidor,
--   detalle_pedido, pedido, producto, categoria, zona_despacho, usuario, rol CASCADE;

-- =========================== 1. ROL ==================================
CREATE TABLE rol (
    id_rol       INT GENERATED ALWAYS AS IDENTITY,
    nombre       VARCHAR(20)  NOT NULL,
    descripcion  VARCHAR(150) NULL,
    CONSTRAINT pk_rol         PRIMARY KEY (id_rol),
    CONSTRAINT uq_rol_nombre  UNIQUE (nombre),
    CONSTRAINT ck_rol_nombre  CHECK (nombre IN ('ADMINISTRADOR', 'OPERADOR', 'REPARTIDOR', 'CLIENTE'))
);

-- ========================= 2. USUARIO ================================
CREATE TABLE usuario (
    id_usuario      INT GENERATED ALWAYS AS IDENTITY,
    rut             VARCHAR(12)  NOT NULL,          -- ej. 12345678-9
    nombre          VARCHAR(100) NOT NULL,
    email           VARCHAR(120) NOT NULL,
    telefono        VARCHAR(15)  NULL,              -- ej. +56912345678
    password_hash   VARCHAR(255) NOT NULL,          -- solo el hash (BCrypt), nunca texto plano
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_rol          INT          NOT NULL,          -- rol inicial CLIENTE (lo asigna el servidor)
    CONSTRAINT pk_usuario        PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_rut    UNIQUE (rut),
    CONSTRAINT uq_usuario_email  UNIQUE (email),
    CONSTRAINT fk_usuario_rol    FOREIGN KEY (id_rol) REFERENCES rol (id_rol) ON DELETE RESTRICT
);

-- ====================== 3. ZONA_DESPACHO =============================
CREATE TABLE zona_despacho (
    id_zona          INT GENERATED ALWAYS AS IDENTITY,
    nombre           VARCHAR(40)  NOT NULL,         -- ej. Zona Centro
    cobertura        VARCHAR(200) NOT NULL,         -- ej. Chillán sector oriente, Chillán Viejo
    dias_despacho    VARCHAR(60)  NOT NULL,         -- ej. Lunes a Sábado
    hora_inicio      TIME         NOT NULL,
    hora_fin         TIME         NOT NULL,
    tiempo_estimado  VARCHAR(30)  NULL,             -- ej. 1 – 3 horas
    CONSTRAINT pk_zona_despacho  PRIMARY KEY (id_zona),
    CONSTRAINT uq_zona_nombre    UNIQUE (nombre),
    CONSTRAINT ck_zona_horario   CHECK (hora_fin > hora_inicio)
);

-- ======================== 4. CATEGORIA ===============================
CREATE TABLE categoria (
    id_categoria  INT GENERATED ALWAYS AS IDENTITY,
    nombre        VARCHAR(60) NOT NULL,             -- ej. Reguladores
    CONSTRAINT pk_categoria         PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nombre  UNIQUE (nombre)
);

-- ========================= 5. PRODUCTO ===============================
CREATE TABLE producto (
    id_producto         INT GENERATED ALWAYS AS IDENTITY,
    codigo              VARCHAR(10)  NOT NULL,      -- ej. CL002
    nombre              VARCHAR(120) NOT NULL,
    descripcion         VARCHAR(500) NULL,
    unidad              VARCHAR(20)  NOT NULL,
    precio_residencial  INT          NOT NULL,      -- CLP
    precio_comercial    INT          NOT NULL,      -- CLP
    stock_actual        INT          NOT NULL,
    imagen_url          VARCHAR(255) NULL,
    destacado           BOOLEAN      NOT NULL DEFAULT FALSE,  -- aparece en Ofertas
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    id_categoria        INT          NOT NULL,
    CONSTRAINT pk_producto              PRIMARY KEY (id_producto),
    CONSTRAINT uq_producto_codigo       UNIQUE (codigo),
    CONSTRAINT ck_producto_unidad       CHECK (unidad IN ('Unidad', 'Kit')),
    CONSTRAINT ck_producto_precio_res   CHECK (precio_residencial > 0),
    CONSTRAINT ck_producto_precio_com   CHECK (precio_comercial > 0),
    CONSTRAINT ck_producto_stock        CHECK (stock_actual >= 0),
    CONSTRAINT fk_producto_categoria    FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria) ON DELETE RESTRICT
);

-- ========================== 6. PEDIDO ================================
CREATE TABLE pedido (
    id_pedido         INT GENERATED ALWAYS AS IDENTITY,
    codigo            VARCHAR(12)   NOT NULL,       -- ej. PED-1001
    fecha             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    direccion         VARCHAR(200)  NOT NULL,
    latitud_entrega   DECIMAL(9,6)  NULL,           -- punto de entrega en el mapa
    longitud_entrega  DECIMAL(9,6)  NULL,
    indicaciones      VARCHAR(300)  NULL,
    tipo_tarifa       VARCHAR(12)   NOT NULL,
    metodo_pago       VARCHAR(15)   NOT NULL,
    estado            VARCHAR(12)   NOT NULL DEFAULT 'PENDIENTE',
    total             INT           NOT NULL,       -- lo calcula el servidor
    id_repartidor     INT           NULL,           -- NULL = sin asignar (rol REPARTIDOR)
    id_cliente        INT           NOT NULL,       -- rol CLIENTE
    id_zona           INT           NOT NULL,
    CONSTRAINT pk_pedido              PRIMARY KEY (id_pedido),
    CONSTRAINT uq_pedido_codigo       UNIQUE (codigo),
    CONSTRAINT ck_pedido_tarifa       CHECK (tipo_tarifa IN ('RESIDENCIAL', 'COMERCIAL')),
    CONSTRAINT ck_pedido_metodo_pago  CHECK (metodo_pago IN ('EFECTIVO', 'TRANSFERENCIA', 'DEBITO')),
    CONSTRAINT ck_pedido_estado       CHECK (estado IN ('PENDIENTE', 'EN CAMINO', 'ENTREGADO', 'CANCELADO')),
    CONSTRAINT ck_pedido_total        CHECK (total >= 0),
    CONSTRAINT ck_pedido_latitud      CHECK (latitud_entrega  BETWEEN -90  AND 90),
    CONSTRAINT ck_pedido_longitud     CHECK (longitud_entrega BETWEEN -180 AND 180),
    CONSTRAINT fk_pedido_repartidor   FOREIGN KEY (id_repartidor) REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_pedido_cliente      FOREIGN KEY (id_cliente)    REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_pedido_zona         FOREIGN KEY (id_zona)       REFERENCES zona_despacho (id_zona) ON DELETE RESTRICT
);

-- ====================== 7. DETALLE_PEDIDO ============================
CREATE TABLE detalle_pedido (
    id_pedido        INT NOT NULL,
    id_producto      INT NOT NULL,
    cantidad         INT NOT NULL,
    precio_unitario  INT NOT NULL,                  -- precio al momento de comprar
    CONSTRAINT pk_detalle_pedido   PRIMARY KEY (id_pedido, id_producto),  -- un producto 1 vez por pedido
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_precio   CHECK (precio_unitario > 0),
    CONSTRAINT fk_detalle_pedido   FOREIGN KEY (id_pedido)   REFERENCES pedido (id_pedido)     ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE RESTRICT
);

-- =================== 8. UBICACION_REPARTIDOR =========================
CREATE TABLE ubicacion_repartidor (
    id_ubicacion   INT GENERATED ALWAYS AS IDENTITY,
    latitud        DECIMAL(9,6) NOT NULL,
    longitud       DECIMAL(9,6) NOT NULL,
    fecha_hora     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- la más reciente es la actual
    id_repartidor  INT          NOT NULL,           -- rol REPARTIDOR
    CONSTRAINT pk_ubicacion_repartidor  PRIMARY KEY (id_ubicacion),
    CONSTRAINT ck_ubicacion_latitud     CHECK (latitud  BETWEEN -90  AND 90),
    CONSTRAINT ck_ubicacion_longitud    CHECK (longitud BETWEEN -180 AND 180),
    CONSTRAINT fk_ubicacion_repartidor  FOREIGN KEY (id_repartidor)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT
);

-- ============================ 9. PAGO ================================
CREATE TABLE pago (
    id_pago      INT GENERATED ALWAYS AS IDENTITY,
    monto        INT         NOT NULL,              -- CLP
    fecha_pago   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comprobante  VARCHAR(50) NULL,                  -- n° de transferencia o voucher
    id_pedido    INT         NOT NULL,              -- se crea al pasar a ENTREGADO
    CONSTRAINT pk_pago         PRIMARY KEY (id_pago),
    CONSTRAINT uq_pago_pedido  UNIQUE (id_pedido),  -- máximo 1 pago por pedido
    CONSTRAINT ck_pago_monto   CHECK (monto > 0),
    CONSTRAINT fk_pago_pedido  FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido) ON DELETE RESTRICT
);

-- ================= 10. HISTORIAL_ESTADO_PEDIDO =======================
CREATE TABLE historial_estado_pedido (
    id_historial  INT GENERATED ALWAYS AS IDENTITY,
    estado        VARCHAR(12) NOT NULL,
    fecha_hora    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_pedido     INT         NOT NULL,
    CONSTRAINT pk_historial_estado_pedido PRIMARY KEY (id_historial),
    CONSTRAINT ck_historial_estado        CHECK (estado IN ('PENDIENTE', 'EN CAMINO', 'ENTREGADO', 'CANCELADO')),
    CONSTRAINT fk_historial_pedido        FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido) ON DELETE CASCADE
);

-- ===================== Índices para las FK ===========================
-- PostgreSQL no crea índices automáticamente en las FK (MySQL sí).
CREATE INDEX ix_usuario_rol            ON usuario (id_rol);
CREATE INDEX ix_producto_categoria     ON producto (id_categoria);
CREATE INDEX ix_pedido_cliente         ON pedido (id_cliente);
CREATE INDEX ix_pedido_repartidor      ON pedido (id_repartidor);
CREATE INDEX ix_pedido_zona            ON pedido (id_zona);
CREATE INDEX ix_detalle_producto       ON detalle_pedido (id_producto);
CREATE INDEX ix_ubicacion_rep_fecha    ON ubicacion_repartidor (id_repartidor, fecha_hora DESC);
CREATE INDEX ix_historial_pedido_fecha ON historial_estado_pedido (id_pedido, fecha_hora);

COMMIT;
