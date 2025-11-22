package com.ues.sic.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ues.sic.cuentas.CuentaModel;
import com.ues.sic.cuentas.CuentaRepository;
import com.ues.sic.detalle_partida.DetallePartidaModel;
import com.ues.sic.detalle_partida.DetallePartidaRepository;
import com.ues.sic.periodos.PeriodoContableModel;
import com.ues.sic.periodos.PeriodoContableRepository;
import com.ues.sic.tipoDocumentos.TipoDocumentoModel;
import com.ues.sic.tipoDocumentos.TipoDocumentoRepository;
import com.ues.sic.usuarios.UsuariosModel;
import com.ues.sic.usuarios.UsuariosRepository;

import com.ues.sic.partidas.PartidasModel;
import com.ues.sic.partidas.PartidasRepository;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Configuration
public class DataSeeder {

    // Usuarios por defecto para cada rol
    private static final String DEFAULT_ADMIN_USERNAME = "admin1";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@sic.com";

    private static final String DEFAULT_CONTADOR_USERNAME = "contador1";
    private static final String DEFAULT_CONTADOR_PASSWORD = "contador123";
    private static final String DEFAULT_CONTADOR_EMAIL = "contador@sic.com";

    private static final String DEFAULT_AUDITOR_USERNAME = "auditor1";
    private static final String DEFAULT_AUDITOR_PASSWORD = "auditor123";
    private static final String DEFAULT_AUDITOR_EMAIL = "auditor@sic.com";

    @Bean
    CommandLineRunner seedDefaultUser(UsuariosRepository repo, PasswordEncoder encoder, Environment env) {
        return args -> {
            System.out.println("\n========================================");
            System.out.println("  INICIANDO SEED DE USUARIOS DEFAULT");
            System.out.println("========================================\n");

            createDefaultUsers(repo, encoder);

            System.out.println("\n========================================");
            System.out.println("  SEED DE USUARIOS COMPLETADO");
            System.out.println("========================================\n");
        };
    }

    @Bean
    CommandLineRunner seedCatalogoCuentas(CuentaRepository cuentaRepo) {
        return args -> {
            // Verificar si ya existen cuentas en la base de datos
            if (cuentaRepo.count() > 0) {
                System.out.println("\n El catálogo de cuentas ya existe - omitiendo seed");
                return;
            }

            System.out.println("\n========================================");
            System.out.println("  INICIANDO SEED DE CATÁLOGO DE CUENTAS");
            System.out.println("========================================\n");

            try {
                // Leer el archivo JSON desde resources/data/catalogo.json
                ClassPathResource resource = new ClassPathResource("data/catalogo.json");
                InputStream inputStream = resource.getInputStream();

                // Configurar ObjectMapper con soporte para LocalDate
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                // Parsear el JSON a una lista de CuentaModel
                List<CuentaModel> cuentas = mapper.readValue(
                        inputStream,
                        new TypeReference<List<CuentaModel>>() {
                        });

                // Insertar usando el método insertarCuenta de cada modelo
                int contador = 0;
                for (CuentaModel c : cuentas) {
                    CuentaModel.insertarCuenta(
                            cuentaRepo,
                            c.getCodigo(),
                            c.getNombre(),
                            c.getTipo(),
                            c.getSaldoNormal(),
                            c.getIdPadre());
                    contador++;
                }

                System.out.println(" Total de cuentas insertadas: " + contador);
                System.out.println("\n========================================");
                System.out.println("  SEED DE CATÁLOGO DE CUENTAS COMPLETADO");
                System.out.println("========================================\n");

            } catch (Exception e) {
                System.err.println("ERROR al cargar el catálogo de cuentas: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    @Bean
    CommandLineRunner seedPeriodosContables(PeriodoContableRepository periodoRepo) {
        return args -> {
            // Verificar si ya existen periodos en la base de datos
            if (periodoRepo.count() > 0) {
                System.out.println("\n Los periodos contables ya existen - omitiendo seed");
                return;
            }

            System.out.println("\n========================================");
            System.out.println("  INICIANDO SEED DE PERIODOS CONTABLES");
            System.out.println("========================================\n");

            try {
                // Leer el archivo JSON desde resources/data/periodos.json
                ClassPathResource resource = new ClassPathResource("data/periodos.json");
                InputStream inputStream = resource.getInputStream();

                // Configurar ObjectMapper con soporte para LocalDate
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                // Parsear el JSON a una lista de PeriodoContableModel
                List<PeriodoContableModel> periodos = mapper.readValue(
                        inputStream,
                        new TypeReference<List<PeriodoContableModel>>() {
                        });

                // Insertar usando el método insertarPeriodo de cada modelo
                int contador = 0;
                for (PeriodoContableModel p : periodos) {
                    PeriodoContableModel.insertarPeriodo(
                            periodoRepo,
                            p.getNombre(),
                            p.getFrecuencia(),
                            p.getFechaInicio(),
                            p.getFechaFin(),
                            p.getCerrado());
                    contador++;
                }

                System.out.println(" Total de periodos insertados: " + contador);
                System.out.println("\n========================================");
                System.out.println("  SEED DE PERIODOS CONTABLES COMPLETADO");
                System.out.println("========================================\n");

            } catch (Exception e) {
                System.err.println("ERROR al cargar los periodos contables: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

   @Bean
   @Order(1)
CommandLineRunner seedPartidas(PartidasRepository partidasRepo) {
    return args -> {

        // Si ya hay registros, no hacemos seed
        if (partidasRepo.count() > 0) {
            System.out.println("\n Las partidas ya existen - omitiendo seed");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("      INICIANDO SEED DE PARTIDAS");
        System.out.println("========================================\n");

        try {
            // Leer el archivo JSON desde resources/data/partidas.json
            ClassPathResource resource = new ClassPathResource("data/partidas.json");
            InputStream inputStream = resource.getInputStream();

            ObjectMapper mapper = new ObjectMapper();

            // Leemos el JSON como una lista de Maps (id, descripcion, fecha, idPeriodo, idUsuario)
            List<Map<String, Object>> partidasJson = mapper.readValue(
                    inputStream,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            int contador = 0;

            for (Map<String, Object> pJson : partidasJson) {

                PartidasModel p = new PartidasModel();

                // OJO: no usamos el id del JSON para dejar que JPA lo genere
                p.setDescripcion((String) pJson.get("descripcion"));
                p.setFecha((String) pJson.get("fecha"));

                // idPeriodo e idUsuario vienen como "1" en el JSON, los convertimos a String igual
                p.setIdPeriodo(String.valueOf(pJson.get("idPeriodo")));
                p.setIdUsuario(String.valueOf(pJson.get("idUsuario")));

                partidasRepo.save(p);
                contador++;
            }

            System.out.println(" Total de partidas insertadas: " + contador);
            System.out.println("\n========================================");
            System.out.println("      SEED DE PARTIDAS COMPLETADO");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("ERROR al cargar las partidas: " + e.getMessage());
            e.printStackTrace();
        }
    };
}



@Bean
@Order(2)
CommandLineRunner seedDetallePartidas(
        DetallePartidaRepository detalleRepo,
        PartidasRepository partidasRepo
) {
    return args -> {

        // Si ya existen registros, no sembramos nada
        if (detalleRepo.count() > 0) {
            System.out.println("\n Los detalles de partidas ya existen - omitiendo seed");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("    INICIANDO SEED DE DETALLE PARTIDA");
        System.out.println("========================================\n");

        try {
            // Leer JSON desde resources/data/detallePartidas.json
            ClassPathResource resource = new ClassPathResource("data/detallesPartidas.json");
            InputStream inputStream = resource.getInputStream();

            ObjectMapper mapper = new ObjectMapper();

            List<Map<String, Object>> detallesJson = mapper.readValue(
                    inputStream,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            int contador = 0;

            for (Map<String, Object> dJson : detallesJson) {

                // Buscar Partida real según el "partidaId" del JSON
                Long partidaId = Long.valueOf(dJson.get("partidaId").toString());

                PartidasModel partida = partidasRepo.findById(partidaId)
                        .orElse(null);

                if (partida == null) {
                    System.err.println(" ⚠️ No se encontró la partida con id: " + partidaId);
                    continue;
                }

                // Crear el modelo
                DetallePartidaModel detalle = new DetallePartidaModel();

                detalle.setPartida(partida);
                detalle.setIdCuenta((String) dJson.get("idCuenta"));
                detalle.setDescripcion((String) dJson.get("descripcion"));

                // Convertir Double correctamente
                detalle.setDebito( Double.valueOf(dJson.get("debito").toString()) );
                detalle.setCredito( Double.valueOf(dJson.get("credito").toString()) );

                detalleRepo.save(detalle);
                contador++;
            }

            System.out.println(" Total de detalles insertados: " + contador);
            System.out.println("\n========================================");
            System.out.println(" SEED DETALLE PARTIDA COMPLETADO");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("ERROR al cargar los detalles: " + e.getMessage());
            e.printStackTrace();
        }
    };
}


    @Bean
    CommandLineRunner seedTipoDocumentos(TipoDocumentoRepository tipoRepo) {
        return args -> {
            System.out.println("\n========================================");
            System.out.println("  INICIANDO SEED DE TIPOS DE DOCUMENTO");
            System.out.println("========================================");

            // Si ya hay datos, no volver a insertar
            if (tipoRepo.count() > 0) {
                System.out.println("Tipos de documento ya existen - omitiendo seed");
                return;
            }

            try {
                // Leer el archivo JSON
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = getClass().getResourceAsStream("/data/tipoDocumentos.json");

                List<Map<String, Object>> lista = mapper.readValue(
                        inputStream, new TypeReference<List<Map<String, Object>>>() {
                        });

                for (Map<String, Object> item : lista) {
                    TipoDocumentoModel tipo = new TipoDocumentoModel();
                    tipo.setNombre((String) item.get("nombre"));
                    tipo.setCreadoPor((Integer) item.get("creadoPor"));
                    tipoRepo.save(tipo);

                    System.out.println("Tipo documento '" + tipo.getNombre() + "' creado.");
                }

            } catch (Exception e) {
                System.err.println("Error al ejecutar seed de TipoDocumento: " + e.getMessage());
            }

            System.out.println("========================================");
            System.out.println("  SEED DE TIPOS DE DOCUMENTO COMPLETADO");
            System.out.println("========================================\n");
        };
    }

    /**
     * Crea usuarios por defecto para cada rol si no existen
     */
    private void createDefaultUsers(UsuariosRepository repo, PasswordEncoder encoder) {
        // Crear usuario ADMIN
        createUserIfNotExists(
                repo, encoder,
                DEFAULT_ADMIN_USERNAME,
                DEFAULT_ADMIN_EMAIL,
                "ADMIN",
                DEFAULT_ADMIN_PASSWORD);

        // Crear usuario CONTADOR
        createUserIfNotExists(
                repo, encoder,
                DEFAULT_CONTADOR_USERNAME,
                DEFAULT_CONTADOR_EMAIL,
                "CONTADOR",
                DEFAULT_CONTADOR_PASSWORD);

        // Crear usuario AUDITOR
        createUserIfNotExists(
                repo, encoder,
                DEFAULT_AUDITOR_USERNAME,
                DEFAULT_AUDITOR_EMAIL,
                "AUDITOR",
                DEFAULT_AUDITOR_PASSWORD);
    }

    /**
     * Crea un usuario si no existe
     */
    private void createUserIfNotExists(UsuariosRepository repo, PasswordEncoder encoder,
            String username, String email, String role, String password) {

        // Verificar si el usuario ya existe
        boolean userExists = repo.existsByUsernameIgnoreCase(username)
                || repo.existsByEmailIgnoreCase(email);

        if (userExists) {
            System.out.println(" Usuario '" + username + "' (" + role + ") ya existe - omitiendo creación");
            return;
        }

        // Crear nuevo usuario
        UsuariosModel user = new UsuariosModel();
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(true);
        user.setPassword(encoder.encode(password));

        // Establecer timestamps
        String currentTimestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        user.setCreatedAt(currentTimestamp);
        user.setUpdatedAt(currentTimestamp);

        try {
            repo.save(user);
            System.out.println("Usuario creado exitosamente:");
            System.out.println("   ├─ Usuario: " + username);
            System.out.println("   ├─ Email: " + email);
            System.out.println("   ├─ Rol: " + role);
            System.out.println("   └─ Contraseña: " + password);
            System.out.println();
        } catch (Exception e) {
            System.err.println("ERROR al crear usuario '" + username + "': " + e.getMessage());
            System.err.println();
        }
    }
}
