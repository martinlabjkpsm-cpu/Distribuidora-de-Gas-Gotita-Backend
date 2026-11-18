package cl.duoc.backend.config;

import cl.duoc.backend.modelo.Categoria;
import cl.duoc.backend.modelo.Producto;
import cl.duoc.backend.modelo.Rol;
import cl.duoc.backend.modelo.Usuario;
import cl.duoc.backend.repositorio.CategoriaRepository;
import cl.duoc.backend.repositorio.ProductoRepository;
import cl.duoc.backend.repositorio.UsuarioRepository;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pasa los datos simulados del frontend (catalogoGasGotita.js) a la base, solo si las tablas están vacías.
 * Así, al reiniciar el backend no se duplican ni se pierden datos.
 * Usuarios de prueba: solo si existe la variable DEMO_PASSWORD (la contraseña no queda en el repositorio).
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public DatosIniciales(CategoriaRepository categorias, ProductoRepository productos, UsuarioRepository usuarios,
                          PasswordEncoder passwordEncoder, @Value("${gotita.demo.password:}") String demoPassword) {
        this.categorias = categorias;
        this.productos = productos;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categorias.count() == 0) {
            cargarCatalogo();
        }
        if (usuarios.count() == 0 && demoPassword != null && !demoPassword.isBlank()) {
            String hash = passwordEncoder.encode(demoPassword);
            usuarios.save(new Usuario("11111111-1", "Ana María Valenzuela", "admin@gasgotita.test", hash, Rol.ADMINISTRADOR));
            usuarios.save(new Usuario("22222222-2", "Camila Torres", "operadora@gasgotita.test", hash, Rol.OPERADOR));
            usuarios.save(new Usuario("33333333-3", "Juan Pérez", "repartidor@gasgotita.test", hash, Rol.REPARTIDOR));
            usuarios.save(new Usuario("44444444-4", "Carlos Mendoza", "cliente@gasgotita.test", hash, Rol.CLIENTE));
            log.info("Usuarios de prueba creados (uno por rol).");
        }
    }

    private void cargarCatalogo() {
        Map<String, Categoria> cat = new HashMap<>();
        for (String nombre : new String[]{"Cilindros de Gas", "Reguladores", "Mangueras y Conexiones", "Accesorios"}) {
            cat.put(nombre, categorias.save(new Categoria(nombre)));
        }
        Categoria cil = cat.get("Cilindros de Gas");
        Categoria reg = cat.get("Reguladores");
        Categoria man = cat.get("Mangueras y Conexiones");
        Categoria acc = cat.get("Accesorios");

        productos.save(new Producto("CL001", cil, "Cilindro GLP 5 kg", "Cilindro de gas licuado de petróleo 5 kg. Para uso residencial (cocina, calefacción pequeña).", "Unidad", 6500, 6000, 80, "/img/gas5kg.jpeg"));
        productos.save(new Producto("CL002", cil, "Cilindro GLP 11 kg", "Cilindro estándar doméstico. El más utilizado en hogares chilenos. Compatible con reguladores estándar.", "Unidad", 12000, 11000, 200, "/img/gas11kg.jpeg"));
        productos.save(new Producto("CL003", cil, "Cilindro GLP 15 kg", "Cilindro de mayor capacidad para hogares de alto consumo o locales pequeños.", "Unidad", 16000, 14500, 90, "/img/gas15kg.jpeg"));
        productos.save(new Producto("CL004", cil, "Cilindro GLP 45 kg", "Cilindro industrial. Uso comercial: restaurantes, talleres, calefacción de locales.", "Unidad", 45000, 40000, 30, "/img/gasgotita45.jpeg"));
        productos.save(new Producto("RG001", reg, "Regulador doméstico estándar", "Regulador de 1 etapa para cilindros 5, 11 y 15 kg. Presión de salida 28 mbar.", "Unidad", 8990, 8200, 45, "/img/regulador-domestico.webp"));
        productos.save(new Producto("RG002", reg, "Regulador de alta presión", "Regulador para cocinas industriales o equipos de mayor consumo. Presión regulable.", "Unidad", 18990, 17000, 12, "/img/regulador-altapresion.jpeg"));
        productos.save(new Producto("RG003", reg, "Regulador dual (2 salidas)", "Permite conectar dos artefactos simultáneamente al mismo cilindro.", "Unidad", 14990, 13500, 18, "/img/regulador-dual.png"));
        productos.save(new Producto("MG001", man, "Manguera gas 1.5 m", "Manguera flexible homologada. Diámetro interior 9mm. Compatible con reguladores estándar.", "Unidad", 3990, 3500, 80, "/img/manguera-gas.webp"));
        productos.save(new Producto("MG002", man, "Manguera gas 3 m", "Manguera larga para instalaciones donde el artefacto está alejado del cilindro.", "Unidad", 6990, 6200, 50, "/img/manguera-gas.webp"));
        productos.save(new Producto("MG003", man, "Abrazadera metálica", "Abrazadera de acero para asegurar la conexión manguera-regulador y manguera-artefacto.", "Unidad", 990, 800, 200, "/img/abrazadera-metalica.webp"));
        productos.save(new Producto("MG004", man, "Kit conexión completo (regulador + manguera 1.5m + abrazaderas)", "Todo lo necesario para instalar un cilindro nuevo.", "Kit", 12990, 11500, 25, "/img/kit-conexion-completo.webp"));
        productos.save(new Producto("AC001", acc, "Carro porta cilindro 11/15 kg", "Carro metálico con ruedas para transportar cilindros dentro del hogar con seguridad.", "Unidad", 12990, 11000, 20, "/img/carro-gas.png"));
        productos.save(new Producto("AC002", acc, "Tapa protectora para válvula", "Tapa de plástico ABS para proteger la válvula del cilindro durante el transporte.", "Unidad", 1490, 1200, 60, "/img/valvula-gas.jpeg"));
        productos.save(new Producto("AC003", acc, "Detector de gas a batería", "Sensor electroquímico. Alarma sonora y visual ante fuga de gas GLP o metano.", "Unidad", 19990, 17000, 8, "/img/detecr.webp"));
        log.info("Catálogo inicial cargado: 4 categorías y 14 productos.");
    }
}
