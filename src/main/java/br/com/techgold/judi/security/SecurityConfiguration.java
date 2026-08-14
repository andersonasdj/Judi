package br.com.techgold.judi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	
	private final UserAuthenticationFilter filter;
	private final TwoFactorRedirectFilter twoFactorRedirectFilter;
	private final CustomAuthenticationSuccessHandler customSuccessHandler;

	SecurityConfiguration(UserAuthenticationFilter filter, TwoFactorRedirectFilter twoFactorRedirectFilter, CustomAuthenticationSuccessHandler customSuccessHandler) {
		this.filter = filter;
		this.twoFactorRedirectFilter = twoFactorRedirectFilter;
		this.customSuccessHandler = customSuccessHandler;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
	    httpSecurity
	        .csrf(AbstractHttpConfigurer::disable)
	        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
	        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
	        .addFilterAfter(twoFactorRedirectFilter, UserAuthenticationFilter.class)
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/templates/**", "/assets/**").permitAll()
	            .requestMatchers("/2fa", "/2fa/**", "/verify-2fa").hasAuthority("PRE_2FA")
	            .requestMatchers(HttpMethod.POST, "/login", "/create").permitAll()
	            .requestMatchers(HttpMethod.GET, "/create").permitAll()
	            .anyRequest().authenticated()
	        )
	        .formLogin(form -> form
	            .loginPage("/login")
	            .successHandler(customSuccessHandler)
	            .permitAll()
	        )
	        .sessionManagement(session -> session
	            .invalidSessionUrl("/login")
	            .maximumSessions(1)
	            .sessionRegistry(sessionRegistry())
	        )
	        .logout(logout -> logout
	            .logoutUrl("/logout")
	            .logoutSuccessUrl("/login")
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID")
	        );

	    return httpSecurity.build();
	}

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

    @Bean
    PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	/**
	 * Sem isso, o SessionRegistry só é avisado de sessões encerradas no logout
	 * explícito — uma sessão que expira por timeout de inatividade continuaria
	 * aparecendo como "online" até a próxima tentativa de acesso com o cookie.
	 */
	@Bean
	HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

}
