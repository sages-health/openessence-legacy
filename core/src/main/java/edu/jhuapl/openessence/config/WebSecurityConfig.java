/*
 * Copyright (c) 2013 The Johns Hopkins University/Applied Physics Laboratory
 *                             All rights reserved.
 *
 * This material may be used, modified, or reproduced by or for the U.S.
 * Government pursuant to the rights granted under the clauses at
 * DFARS 252.227-7013/7014 or FAR 52.227-14.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * NO WARRANTY.   THIS MATERIAL IS PROVIDED "AS IS."  JHU/APL DISCLAIMS ALL
 * WARRANTIES IN THE MATERIAL, WHETHER EXPRESS OR IMPLIED, INCLUDING (BUT NOT
 * LIMITED TO) ANY AND ALL IMPLIED WARRANTIES OF PERFORMANCE,
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF
 * INTELLECTUAL PROPERTY RIGHTS. ANY USER OF THE MATERIAL ASSUMES THE ENTIRE
 * RISK AND LIABILITY FOR USING THE MATERIAL.  IN NO EVENT SHALL JHU/APL BE
 * LIABLE TO ANY USER OF THE MATERIAL FOR ANY ACTUAL, INDIRECT,
 * CONSEQUENTIAL, SPECIAL OR OTHER DAMAGES ARISING FROM THE USE OF, OR
 * INABILITY TO USE, THE MATERIAL, INCLUDING, BUT NOT LIMITED TO, ANY DAMAGES
 * FOR LOST PROFITS.
 */

package edu.jhuapl.openessence.config;

import edu.jhuapl.openessence.controller.LoginController;
import edu.jhuapl.openessence.security.Http401AuthenticationFailureHandler;
import edu.jhuapl.openessence.security.Http403RequestMatcher;
import edu.jhuapl.openessence.security.OEPasswordEncoder;
import edu.jhuapl.openessence.security.OEUserDetailsService;
import edu.jhuapl.openessence.security.OeAuthenticationSuccessHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.AnyRequestMatcher;

import javax.inject.Inject;

@Configuration
@EnableWebSecurity
@Profile(WebSecurityConfig.SECURITY_PROFILE)
public class WebSecurityConfig {

    public static final String SECURITY_PROFILE = "security";

    /**
     * Configure the shared {@link AuthenticationManager}.
     *
     * @param auth this is injected by Spring
     * @throws Exception propagated from Spring
     * @see {@link org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration}
     */
    @Inject
    public void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        ReflectionSaltSource saltSource = new ReflectionSaltSource();
        saltSource.setUserPropertyToUse("salt");
        authProvider.setSaltSource(saltSource);

        // can't use any of the fluid methods for configuring since we use a custom AuthenticationProvider
        auth.authenticationProvider(authProvider);
    }

    @Bean
    public OEUserDetailsService userDetailsService() {
        return new OEUserDetailsService();
    }

    @Bean
    public OEPasswordEncoder passwordEncoder() {
        return new OEPasswordEncoder();
    }

    /**
     * Configures Basic authentication.
     */
    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/api/**")
                    .authorizeRequests()
                    .anyRequest()
                    .authenticated()
                    .and()

                    .httpBasic()
                    .realmName("OpenEssence")
                    .and()

                    // CSRF tokens are too stateful for REST clients
                    .csrf().disable()
            ;
        }
    }

    /**
     * Configures form-based authentication.
     */
    @Configuration
    public static class FormLoginWebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(WebSecurity web) {
            web.ignoring()
                    .antMatchers("/oe/messages/**", "/oe/locale", "/js/**", "/favicon.ico", "/css/**", "/images/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            String loginPage = "/oe" + LoginController.LOGIN_PAGE_PATH;

            http
                    .authorizeRequests()
                    .antMatchers(loginPage, "/login").permitAll()
                    .anyRequest().authenticated()
                    .and()

                    .formLogin()
                    .loginPage(loginPage)
                    .loginProcessingUrl("/login")
                    .successHandler(successHandler())
                    .failureHandler(failureHandler())
                    .and()

                    .logout()
                    .logoutSuccessUrl(loginPage + "?logout")
                    .and()

                    .exceptionHandling()
                    .defaultAuthenticationEntryPointFor(new Http403ForbiddenEntryPoint(), new Http403RequestMatcher())
                    .defaultAuthenticationEntryPointFor(
                            new LoginUrlAuthenticationEntryPoint(loginPage),
                            new AnyRequestMatcher())
            ;
        }

        @Bean
        public OeAuthenticationSuccessHandler successHandler() {
            return new OeAuthenticationSuccessHandler();
        }

        @Bean
        public Http401AuthenticationFailureHandler failureHandler() {
            return new Http401AuthenticationFailureHandler();
        }
    }
}
