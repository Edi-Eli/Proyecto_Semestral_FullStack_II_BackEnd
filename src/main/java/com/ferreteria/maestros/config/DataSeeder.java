package com.ferreteria.maestros.config;

import com.ferreteria.maestros.usuario.entity.Rol;
import com.ferreteria.maestros.usuario.entity.Usuario;
import com.ferreteria.maestros.usuario.repository.RolRepository;
import com.ferreteria.maestros.usuario.repository.UsuarioRepository;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Siembra los datos base de la aplicación al arrancar (idempotente):
 * <ul>
 *   <li>Roles {@code Administrador}, {@code Vendedor} y {@code Cliente}.</li>
 *   <li>Usuarios demo del frontend ({@code src/data/db.js}) con contraseña cifrada con BCrypt.</li>
 * </ul>
 *
 * <p>Se ejecuta en cada arranque, pero solo crea lo que no existe.</p>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** Contraseña de todos los usuarios demo (igual que en el front). */
    private static final String PASSWORD_DEMO = "1234";

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Rol administrador = asegurarRol("Administrador", "Acceso total: usuarios, inventario y reportes.");
        Rol vendedor = asegurarRol("Vendedor", "Gestiona inventario y el estado de los pedidos.");
        Rol cliente = asegurarRol("Cliente", "Cliente registrado; puede tener cuenta corriente.");

        crearUsuarioDemo("195436789", "sebas@duoc.cl", "Sebastian", "Perez Gonzalez",
                LocalDate.of(2000, 5, 15), administrador,
                "Región Metropolitana de Santiago", "Santiago", "Av. Siempre Viva 123");

        crearUsuarioDemo("123456785", "edi@profesor.duoc.cl", "Eduardo", "Lopez Muñoz",
                LocalDate.of(1995, 3, 20), vendedor,
                "Región de Valparaíso", "Viña del Mar", "Calle Los Pinos 45");

        crearUsuarioDemo("204567891", "maria.torres@gmail.com", "Maria", "Torres Silva",
                LocalDate.of(1998, 11, 2), cliente,
                "Región Metropolitana de Santiago", "Providencia", "Pasaje Las Flores 8");

        crearUsuarioDemo("167895433", "carlos.rojas@duoc.cl", "Carlos", "Rojas Diaz",
                LocalDate.of(1990, 7, 12), cliente,
                "Región del Biobío", "Concepción", "Av. Collao 1550");

        crearUsuarioDemo("111111111", "jose.fernandez@gmail.com", "Jose", "Fernandez Soto",
                LocalDate.of(1987, 1, 30), vendedor,
                "Región Metropolitana de Santiago", "Maipú", "Villa Los Volcanes 210");

        log.info("Seed base verificado: roles [{}], [{}], [{}] y usuarios demo.",
                administrador.getNombre(), vendedor.getNombre(), cliente.getNombre());
    }

    private Rol asegurarRol(String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    log.info("Creando rol '{}'", nombre);
                    return rolRepository.save(new Rol(nombre, descripcion));
                });
    }

    private void crearUsuarioDemo(String run, String correo, String nombre, String apellidos,
                                  LocalDate fechaNacimiento, Rol rol,
                                  String region, String comuna, String direccion) {
        if (usuarioRepository.existsByCorreo(correo)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(PASSWORD_DEMO));
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setFechaNacimiento(fechaNacimiento);
        usuario.setRol(rol);
        usuario.setRegion(region);
        usuario.setComuna(comuna);
        usuario.setDireccion(direccion);
        usuario.setActivo(true);
        usuario.setCuentaCorriente(false);
        usuarioRepository.save(usuario);
        log.info("Usuario demo creado: '{}' (rol '{}')", correo, rol.getNombre());
    }
}
