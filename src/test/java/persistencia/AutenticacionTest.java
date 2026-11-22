package persistencia;

import org.junit.jupiter.api.Test;
import persistencia.dao.ConexionBD;
import persistencia.dao.UsuarioDAO;
import persistencia.dao.UsuarioDAOJdbc;
import persistencia.dto.UsuarioDTO;
import servicio.AutenticacionException;
import servicio.AutenticacionService;
import servicio.CredencialesInvalidasException;
import servicio.UsuarioDuplicadoException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Escenario V1: Registro y login con persistencia DAO/DTO + JDBC (H2).
 */
public class AutenticacionTest {

    private AutenticacionService crearServicioEnMemoria() {
        String dbName = "tpg_test_" + UUID.randomUUID().toString().replace("-", "");
        ConexionBD conexion = new ConexionBD("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1", "sa", "");
        UsuarioDAO dao = new UsuarioDAOJdbc(conexion);
        return new AutenticacionService(dao);
    }

    @Test
    public void testRegistroYLoginCorrectoEnMemoria() {
        AutenticacionService service = crearServicioEnMemoria();

        UsuarioDTO registrado = service.registrar("capitan_kirk", "enterprise1701");
        assertNotNull(registrado.getId(), "Debe asignar un ID generado por H2");
        assertEquals("capitan_kirk", registrado.getNombreUsuario());
        assertNotEquals("enterprise1701", registrado.getPasswordHash(),
                "La contraseña nunca debe guardarse en texto plano");
        assertTrue(registrado.getPasswordHash().contains(":"), "El hash debe incluir sal y digest");

        UsuarioDTO logueado = service.iniciarSesion("capitan_kirk", "enterprise1701");
        assertEquals(registrado.getId(), logueado.getId());
        assertEquals("capitan_kirk", logueado.getNombreUsuario());
    }

    @Test
    public void testRegistroDuplicadoRechazado() {
        AutenticacionService service = crearServicioEnMemoria();
        service.registrar("spock", "vulcano123");

        assertThrows(UsuarioDuplicadoException.class, () -> {
            service.registrar("spock", "otraClave999");
        }, "Debe rechazar nombres de usuario duplicados");
    }

    @Test
    public void testLoginIncorrectoRechazado() {
        AutenticacionService service = crearServicioEnMemoria();
        service.registrar("uhura", "claveCorrecta");

        assertThrows(CredencialesInvalidasException.class, () -> {
            service.iniciarSesion("uhura", "claveIncorrecta");
        }, "Debe rechazar contraseña incorrecta");

        assertThrows(CredencialesInvalidasException.class, () -> {
            service.iniciarSesion("usuarioInexistente", "claveCorrecta");
        }, "Debe rechazar usuario inexistente");
    }

    @Test
    public void testValidacionesDeFormatoEnServicio() {
        AutenticacionService service = crearServicioEnMemoria();

        assertThrows(AutenticacionException.class, () -> service.registrar("", "1234"));
        assertThrows(AutenticacionException.class, () -> service.registrar("ab", "1234"));
        assertThrows(AutenticacionException.class, () -> service.registrar("valido", "123"));
    }

    @Test
    public void testPersistenciaEntreEjecucionesEnDisco() throws IOException {
        Path tempDir = Files.createDirectories(
                Path.of("target", "test-h2-" + UUID.randomUUID().toString().replace("-", "")));
        String dbFilePath = tempDir.resolve("usuarios_test").toAbsolutePath().toString();
        String url = "jdbc:h2:" + dbFilePath;

        try {
            // Primera "ejecucion": registra usuario
            ConexionBD conexion1 = new ConexionBD(url, "sa", "");
            AutenticacionService service1 = new AutenticacionService(new UsuarioDAOJdbc(conexion1));
            UsuarioDTO creado = service1.registrar("nicolas", "claveSegura2026");
            assertNotNull(creado.getId());

            // Segunda "ejecucion": instancias nuevas apuntando al mismo archivo H2
            ConexionBD conexion2 = new ConexionBD(url, "sa", "");
            AutenticacionService service2 = new AutenticacionService(new UsuarioDAOJdbc(conexion2));
            UsuarioDTO recuperado = service2.iniciarSesion("nicolas", "claveSegura2026");

            assertEquals(creado.getId(), recuperado.getId());
            assertEquals("nicolas", recuperado.getNombreUsuario());
        } finally {
            // Limpiar archivos temporales de prueba
            try (var stream = Files.list(tempDir)) {
                stream.forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
            }
            Files.deleteIfExists(tempDir);
        }
    }
}
