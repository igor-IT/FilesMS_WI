package com.microservice.upload.securingweb;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig   {




    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{


        http.authorizeHttpRequests(autorizeRequests -> autorizeRequests
               // .dispatcherTypeMatchers(FORWARD, ERROR).permitAll()

              .requestMatchers(HttpMethod.POST, "/upload").permitAll()
                .requestMatchers(HttpMethod.GET,
                             "/file/**",
                              "/file").permitAll()
                       .requestMatchers(HttpMethod.DELETE,
                               "/file/**").hasAnyRole("ADMIN")
                .anyRequest().authenticated()
        ).httpBasic();
        http.csrf().disable();
        return (SecurityFilterChain) http.build();

    }



    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user =
                User.withDefaultPasswordEncoder()
                        .username("admin")
                        .password("123456")
                        .roles("ADMIN")
                        .build();

        return new InMemoryUserDetailsManager(user);
    }
}
