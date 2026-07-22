package com.rrhh.saas.nucleo.config;

import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.servicio.ServicioCargaDatosDemo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(PropiedadesDemo.class)
public class InicializadorDatos {

    @Bean
    CommandLineRunner crearSuperAdmin(RepositorioUsuario repositorioUsuario,
                                      PasswordEncoder passwordEncoder,
                                      ServicioCargaDatosDemo servicioCargaDatosDemo,
                                      Environment entorno,
                                      @Value("${SUPERADMIN_EMAIL:admin@rrhh.local}") String emailSuperAdmin,
                                      @Value("${SUPERADMIN_PASSWORD:}") String passwordSuperAdmin) {
        return args -> {
            if (repositorioUsuario.findByEmailAndDeletedFalse(emailSuperAdmin).isEmpty()) {
                String contrasena = resolverContrasenaSuperAdmin(entorno, passwordSuperAdmin);
                if (contrasena != null) {
                    Usuario superAdmin = new Usuario();
                    superAdmin.setEmail(emailSuperAdmin);
                    superAdmin.setPasswordHash(passwordEncoder.encode(contrasena));
                    superAdmin.setEsSuperAdmin(true);
                    superAdmin.setActivo(true);
                    repositorioUsuario.save(superAdmin);
                }
            }
            servicioCargaDatosDemo.cargarSiCorresponde();
            servicioCargaDatosDemo.completarVacacionesLctDemoSiCorresponde();
            servicioCargaDatosDemo.completarFichajeDemoSiCorresponde();
            servicioCargaDatosDemo.completarOrganizacionActivosYChecklistsDemoSiCorresponde();
            servicioCargaDatosDemo.repararReciboDemoSiCorresponde();
        };
    }

    private String resolverContrasenaSuperAdmin(Environment entorno, String passwordConfigurado) {
        if (passwordConfigurado != null && !passwordConfigurado.isBlank()) {
            return passwordConfigurado;
        }
        if (entorno.acceptsProfiles(Profiles.of("prod"))) {
            return null;
        }
        return "Admin123!";
    }
}
