package io.mountblue.blogger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

    http.csrf(customizer->customizer.disable());
    http.authorizeHttpRequests(auth-> auth
            .requestMatchers(HttpMethod.GET,"/posts","/posts/filter","/posts/{id:\\d+}").permitAll()
            .requestMatchers(HttpMethod.POST,"/comments/add/**").permitAll()
            .requestMatchers("/posts/new", "/posts/save", "/posts/update/**", "/posts/delete/**").hasAnyRole("AUTHOR","ADMIN")
            .anyRequest().authenticated());
    http.formLogin(form->
            form
                    .loginPage("/login")
                    .loginProcessingUrl("/authenticateTheUser")
                    .permitAll());
    http.sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

     return  http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
