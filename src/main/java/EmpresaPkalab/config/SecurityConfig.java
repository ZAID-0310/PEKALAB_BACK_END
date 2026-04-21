package EmpresaPkalab.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    var opt = new CorsConfiguration();
                    opt.setAllowedOrigins(List.of("*"));
                    opt.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
                    opt.setAllowedHeaders(List.of("*"));
                    return opt;
                }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // 1. ENDPOINTS DE REPORTE (Solo ADMINISTRADOR)
                        // Agregamos esto arriba para que tenga prioridad sobre el asterisco general
                        .requestMatchers("/api/asistencia/reporte").hasAuthority("ADMINISTRADOR")
                        .requestMatchers("/api/asistencia/alertas").hasAuthority("ADMINISTRADOR")

                        // 2. Rutas para MOTORIZADO y ADMIN (Uso diario)
                        .requestMatchers("/api/asistencia/marcar-entrada").hasAnyAuthority("MOTORIZADO", "ADMINISTRADOR")
                        .requestMatchers("/api/asistencia/marcar-salida/**").hasAnyAuthority("MOTORIZADO", "ADMINISTRADOR")
                        .requestMatchers("/api/asistencia/estado-hoy/**").hasAnyAuthority("MOTORIZADO", "ADMINISTRADOR")

                        .requestMatchers("/api/requerimientos/mi-horario/**").hasAnyAuthority("MOTORIZADO", "ADMINISTRADOR")
                        .requestMatchers("/api/horarios/mi-agenda/**").hasAnyAuthority("MOTORIZADO", "ADMINISTRADOR")

                        // 3. Rutas exclusivas para ADMINISTRADOR
                        .requestMatchers("/api/requerimientos/**").hasAuthority("ADMINISTRADOR")
                        .requestMatchers("/api/usuarios/**").hasAuthority("ADMINISTRADOR")
                        .requestMatchers("/api/tiendas/**").hasAuthority("ADMINISTRADOR")
                        .requestMatchers("/api/horarios/**").hasAuthority("ADMINISTRADOR")

                        .anyRequest().authenticated()
                )
                // Agregamos nuestro filtro de JWT antes del filtro de usuario/password de Spring
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}