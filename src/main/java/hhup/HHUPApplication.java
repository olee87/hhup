package hhup;

import hhup.service.RememberMeService;
import hhup.service.UserService;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
@ComponentScan
public class HHUPApplication extends SpringBootServletInitializer {

	public static void main(String... args) {
		SpringApplication.run(HHUPApplication.class, args);
	}

//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//		return application.sources(HHUPApplication.class);
//	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new StandardPasswordEncoder();
	}

	@Bean
	public SecurityConfiguration security() {
		return new SecurityConfiguration();
	}

	@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
	private class SecurityConfiguration extends WebSecurityConfigurerAdapter {

		private final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

		@Autowired
		private RememberMeService rememberMeServices;

		@Autowired
		public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder, UserService userDetailsService) throws Exception {
			auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http.exceptionHandling()
				.authenticationEntryPoint((request, response, exception) -> {
					log.debug("Pre-authenticated entry point called. Rejecting access");
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
				})
				.and().rememberMe()
					.rememberMeServices(rememberMeServices)
					.key(HHUPConstants.REMEMBER_ME_KEY)
				.and().formLogin()
					.loginProcessingUrl("/rest/login")
					.successHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_OK))
					.failureHandler((request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed"))
					.usernameParameter("username")
					.passwordParameter("password")
				.and().logout()
					.logoutUrl("/logout")
					.logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_OK))
					.deleteCookies("JSESSIONID")
				.and().csrf().disable()
				.headers()
					.frameOptions()
					.disable();
		}
	}
}