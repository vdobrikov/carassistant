package com.carassistant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@Configuration
@EnableWebSecurity
public class AppSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth,
                                @Value("${carassistant.user.admin.password}") String adminPassword) throws Exception {
        auth
            .inMemoryAuthentication()
            .withUser("admin").password(adminPassword).roles("ADMIN", "ACTUATOR");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/").permitAll()
            .antMatchers("/api/**").hasRole("ADMIN")
            .and().httpBasic()
            .and().csrf().disable();
    }
}
