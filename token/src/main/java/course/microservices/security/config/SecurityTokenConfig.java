package course.microservices.security.config;

import course.microservices.core.property.JWTConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityTokenConfig extends WebSecurityConfigurerAdapter {
    protected final JWTConfiguration jwtConfiguration;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().configurationSource(request-> new CorsConfiguration().applyPermitDefaultValues())
                .and()
                    .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                    .exceptionHandling().authenticationEntryPoint((req,resp,e)->resp.sendError(SC_UNAUTHORIZED))
                .and()
                .authorizeRequests()
                    .antMatchers(jwtConfiguration.getLoginUrl(),"/**/swagger-ui.html").permitAll()
                    .antMatchers(HttpMethod.GET,"/**/swagger-resources/**","/**/webjars/springfox-swagger-ui/**","/**/v2/api-docs/").permitAll()
                    .antMatchers("/learning/v1/admin/**").hasRole("ADMIN")
                    .antMatchers("/auth/user/**").hasAnyRole("ADMIN","USER")
                    .anyRequest().authenticated();

    }
}
