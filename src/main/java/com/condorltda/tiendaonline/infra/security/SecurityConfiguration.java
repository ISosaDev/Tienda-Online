package com.condorltda.tiendaonline.infra.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Indica a Spring que esta clase contiene configuraciones
@EnableWebSecurity // Habilita la configuración de seguridad web de Spring Security
public class SecurityConfiguration {

    // Define un Bean que configura la cadena de filtros de seguridad HTTP
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configura las reglas de autorización para las solicitudes HTTP
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso específico a los endpoints de la API que usamos en el demo
                        .requestMatchers("/api/inventario/entrada").permitAll() // Permitir entrada de inventario
                        .requestMatchers("/api/pedidos").permitAll() // Permitir realización de pedidos
                        .requestMatchers("/api/productos").permitAll() // Permitir listado de productos

                        // Permitir acceso a la raíz, index.html y todos los recursos dentro de /static/
                        .requestMatchers("/", "/index.html", "/static/**").permitAll()

                        // **Negar explícitamente cualquier otra solicitud que no coincida con las anteriores**
                        // Esto es más estricto que .authenticated() y ayuda a depurar el 403
                        .anyRequest().permitAll()
                )
                // Deshabilitar la protección CSRF para simplificar el demo
                .csrf(csrf -> csrf.disable());

        return http.build();
    }


    // Nota: Si necesitas configurar autenticación en el futuro (ej. con usuarios de BD, JWT),
    // tendrías que añadir más Beans aquí, como un AuthenticationManager, PasswordEncoder,
    // y quizás configurar el tipo de autenticación (formLogin, httpBasic, jwt).
}
