package com.sap.config;

import com.sap.service.CustomUserDetailsService;
import com.sap.service.GitHubOAuth2UserService;
import com.sap.service.GoogleOAuth2UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(customUserDetailsService);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            GitHubOAuth2UserService gitHubOAuth2UserService,
            GoogleOAuth2UserService googleOAuth2UserService)
            throws Exception {

        http
                .authenticationProvider(authenticationProvider)

                .cors(cors -> {
                })

                .csrf(csrf -> csrf.disable())

                .oauth2Login(oauth -> oauth

                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(gitHubOAuth2UserService)
                                .oidcUserService(googleOAuth2UserService)
                        )

                        .defaultSuccessUrl(
                                "http://localhost:5173",
                                true
                        )

                        .failureHandler((request, response, exception) -> {

                            String mensagem =
                                    "Não foi possível realizar o login. "
                                    + "Este e-mail já está vinculado a outra forma de login.";

                            String detalhe = exception.getMessage();

                            if (detalhe != null) {

                                if (detalhe.contains(
                                        "Não foi possível obter um e-mail válido"
                                )) {

                                    mensagem =
                                            "Não foi possível realizar o login. "
                                            + "Não foi possível obter um e-mail válido da sua conta.";
                                }
                            }

                            String mensagemCodificada =
                                    URLEncoder.encode(
                                            mensagem,
                                            StandardCharsets.UTF_8
                                    );

                            response.sendRedirect(
                                    "http://localhost:5173/?oauthError="
                                            + mensagemCodificada
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/cadastro",
                                "/api/auth/login",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/api/auth/me",
                                "/api/auth/logout"
                        )
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/disciplinas/**",
                                "/api/assuntos/**",
                                "/api/avaliacoes",
                                "/api/livros/**"
                        )
                        .hasAnyRole(
                                "ESTUDANTE",
                                "PROFESSOR"
                        )

                        .requestMatchers(
                                "/api/questoes/**",
                                "/api/avaliacoes/finalizar",
                                "/api/analises/**"
                        )
                        .hasRole("ESTUDANTE")

                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration
                .getAuthenticationManager();
    }
}