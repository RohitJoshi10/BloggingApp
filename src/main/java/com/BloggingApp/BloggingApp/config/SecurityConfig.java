package com.BloggingApp.BloggingApp.config;
import com.BloggingApp.BloggingApp.infrastructure.redis.RedisService;
import com.BloggingApp.BloggingApp.security.JwtAuthenticationEntryPoint;
import com.BloggingApp.BloggingApp.security.JwtAuthenticationFilter;
import com.BloggingApp.BloggingApp.security.JwtTokenHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Date;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RedisService redisService;
    private final JwtTokenHelper jwtTokenHelper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF ko pura disable kar do, isse login aur webhook dono chalne lagenge
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Webhook ko sabse upar rakho (Public Access)
                        .requestMatchers("/api/v1/payments/webhook").permitAll()
                        .requestMatchers("/api/v1/payments/success").permitAll()
                        .requestMatchers("/api/v1/payments/success").permitAll()

                        // 2. Auth endpoints (Login/Register)
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 3. Sabhi GET calls (Posts, etc.)
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                        // 4. Baaki payments APIs (Create Intent, etc.) lock rahengi
                        .requestMatchers("/api/v1/payments/**").authenticated()

                        // 5. Baaki sab kuch lock
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/api/v1/auth/google/success", true)
                )
                // 🔥 LOGOUT HANDLER START
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            String authHeader = request.getHeader("Authorization");
                            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                String token = authHeader.substring(7);

                                try {
                                    // 1. Token ki expiration date nikalo
                                    Date expirationDate = jwtTokenHelper.getExpirationDateFromToken(token);
                                    long now = System.currentTimeMillis();

                                    // 2. Diff nikaalo (milliseconds mein)
                                    long diffInMs = expirationDate.getTime() - now;

                                    if (diffInMs > 0) {
                                        // 3. Jitna time bacha hai, utni hi der ke liye Redis mein rakho
                                        // Seconds mein convert karke (diffInMs / 1000)
                                        redisService.saveTokenToBlacklist(token, diffInMs / 1000);
                                        System.out.println("Token blacklisted for remaining " + (diffInMs / 1000) + " seconds");
                                    }
                                } catch (Exception e) {
                                    System.out.println("Could not calculate token expiration: " + e.getMessage());
                                }
                            }
                        })
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Logged out successfully\"}");
                            response.getWriter().flush();
                        })
                )
                // 🔥 LOGOUT HANDLER END
                .exceptionHandling(ex -> ex.authenticationEntryPoint(this.jwtAuthenticationEntryPoint))
                // OAuth2 aur Session ke liye IF_REQUIRED best rehta hai
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
                //.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // JWT Filter ko UsernamePasswordAuthenticationFilter se pehle add karna
        http.addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/v1/auth/**").permitAll() // Login aur Register ko allow karein
//                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll() // Sabhi GET methods ko public kar rahe hain (Posts, Categories, Images)
//                        .requestMatchers("/api/v1/payments/**").authenticated()
//                        .requestMatchers("/api/v1/payments/webhook").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/payments/webhook"))
//                .oauth2Login(oauth2 -> oauth2
//                        .defaultSuccessUrl("/api/v1/auth/google/success", true)
//                )
//                .exceptionHandling(ex -> ex.authenticationEntryPoint(this.jwtAuthenticationEntryPoint))
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
//
//        // Sabse important line: Filter register karna
//        http.addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}



/*


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig{

    private final CustomUserDetailService customUserDetailService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF disable karna (kiya tha aapne)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated() // Saare requests authenticated hone chahiye
                )
                .httpBasic(withDefaults()); // Basic Authentication enable karna

        return http.build();
    }



    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
//        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//
//        // Yahan dhyan dena: setUserDetailsService hota hai
//        daoAuthenticationProvider.setUserDetailsService(this.customUserDetailService);
//
//        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
//        return new ProviderManager(daoAuthenticationProvider);
//    }

}

Asal mein DaoAuthenticationProvider ye method apni parent class (DaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider) se inherit karta hai. Agar IntelliJ use red show kar raha hai, toh iska matlab hai ki background mein libraries properly load nahi hui hain ya index corruption hai.

Lekin good news ye hai ki aapne jo abhi code likha hai (jisme AuthenticationManager comment out hai), wo Spring Boot 3 ka ekdum standard aur sahi tarika hai!

Ab ye code kaise kaam karega?
Spring Boot itna smart hai ki jab wo dekhta hai aapke paas:

Ek UserDetailsService ka bean hai (aapki @Service wali class).

Ek PasswordEncoder ka bean hai.

...toh wo background mein apne aap ek DaoAuthenticationProvider banata hai aur usme ye dono cheezein set kar deta hai. Aapko manually ye sab likhne ki zaroorat hi nahi hai.
 */