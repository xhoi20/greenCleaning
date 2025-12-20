

package com.laundry.tokenlogin;

import com.laundry.service.CustomEmployeeDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomEmployeeDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //
//@Bean
//public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//    http
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests(authorize -> authorize
//
//                    .requestMatchers("/login", "/api/auth/**").permitAll()
//                 .requestMatchers("/images/**") .permitAll()//"/css/**", "/js/**", "/**/*.jpg", "/**/*.png", "/**/*.ico", "/**/*.svg").permitAll()
//                    .requestMatchers("/payments", "/payments/view/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_CASHIER", "ROLE_DELIVERY")
//                    .requestMatchers("/payments/add")
//                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_CASHIER")
//                    .requestMatchers("/payments/add").hasAnyAuthority("ROLE_CASHIER")
//                    .requestMatchers("/customers/customer-list", "/employees/employee-list", "/orders", "/order-items","/customers/detail/**",
//                            "/services","/customers/edit/**","/customers/customer-form", "/supplies", "/payments", "/api/payments/status/**").authenticated()
//
//                    .requestMatchers( "/customers/delete/**",
//                            "/employees/edit/**", "/employees/delete/**",
//                            "/orders/edit/**", "/orders/delete/**",
//                            "/order-items/edit/**", "/order-items/delete/**",
//                            "/services/edit/**", "/services/delete/**", "/services/restock/**", "/payments/by-cashier", "/payments/metrics",
//                            "/payments/delete/**")
//                    .hasAnyAuthority("ROLE_ADMIN")
//
//                    .requestMatchers("/customers/customer-form", "/employees/employee-form", "/orders/add",
//                            "/order-items/add", "/services/add", "/supplies/add")
//                    .hasAnyAuthority("ROLE_ADMIN")
//
//                    .anyRequest().authenticated()
//            )
//
//            .sessionManagement(session -> session
//                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);
//
//    return http.build();
//}
//}
//
//
//
//
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
////import org.springframework.security.config.http.SessionCreationPolicy;
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
////// Importo JwtAuthenticationFilter nëse e ke si @Component ose @Bean
////// import com.laundry.tokenlogin.JwtAuthenticationFilter;
////
////@Configuration
////@EnableWebSecurity
////public class SecurityConfig {
////
////    // @Autowired private JwtAuthenticationFilter jwtAuthFilter;  // Nëse e ke, uncomment-oje
////
////    @Bean
////    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
////        http
////                .csrf(csrf -> csrf.disable())  // Disabilizo CSRF për API REST (POST JSON)
////                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Pa sesione për JWT
////                .authorizeHttpRequests(auth -> auth
////                        .requestMatchers("/api/employees/**").permitAll()  // LEJO TË GJITHË PËR /api/employees (GET, POST, etj.)
////                        .requestMatchers("/api/auth/**").permitAll()  // Nëse ke login, lejoje
////                        .anyRequest().authenticated()  // Të tjerat kërkojnë autentikim (p.sh., /api/orders)
////                )
////        // .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);  // Shto filter-in JWT vetëm për paths e mbrojtura
////        ;  // Komentoje përkohësisht nëse bën probleme
////
////        return http.build();
////    }
////}
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize

                        // Public
                        .requestMatchers("/login", "/api/auth/**","/orders/add").permitAll()
                        .requestMatchers("/images/**", "/css/**", "/js/**").permitAll()

                        // Payments (si i kishe, por pa duplikim)
                        .requestMatchers("/payments", "/payments/view/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_CLEANER", "ROLE_DELIVERY")
                        .requestMatchers("/payments/add", "/orders/add")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_ CLEANER")

                        // Këto faqe thjesht duhet të jenë të autentikuara
                        .requestMatchers(
                                "/customers/customer-list",
                                "/customers/customer-form",
                                "/employees/employee-list",
                                "/orders",
                                "/order-items",
                                "/customers/detail/**",
                                "/services",
                                "/customers/edit/**",
                                "/customers/customer-form",
                                "/supplies/**",
                                "/transactions/**",
                                "/inventory-transactions/**",
                                "/payments",
                                "/api/payments/status/**"
                        ).authenticated()

                        // Admin-only
                        .requestMatchers(
                                "/customers/delete/**",
                                "/employees/edit/**", "/employees/delete/**",
                                "/orders/edit/**", "/orders/delete/**",
                                "/order-items/edit/**", "/order-items/delete/**",
                                "/services/edit/**", "/services/delete/**", "/services/restock/**",
                                "/payments/by-cashier", "/payments/metrics",
                                "/payments/delete/**"
                        ).hasAnyAuthority("ROLE_ADMIN")

                        // Admin-only forma për krijime
                        .requestMatchers(
//                                "/customers/customer-form",
                                "/employees/employee-form",

                                "/order-items/add",
                                "/services/add",
                                "/supplies/add"
                        ).hasAnyAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}