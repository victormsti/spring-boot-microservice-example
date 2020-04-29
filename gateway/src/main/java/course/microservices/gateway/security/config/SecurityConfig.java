package course.microservices.gateway.security.config;

import course.microservices.core.property.JWTConfiguration;
import course.microservices.gateway.security.filter.GatewayJWTTokenAuthorizationFilter;
import course.microservices.security.config.SecurityTokenConfig;
import course.microservices.security.token.converter.TokenConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class SecurityConfig extends SecurityTokenConfig {
    private final TokenConverter tokenConverter;

    public SecurityConfig(JWTConfiguration jwtConfiguration, TokenConverter tokenConverter){
        super(jwtConfiguration);
        this.tokenConverter = tokenConverter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(new GatewayJWTTokenAuthorizationFilter(jwtConfiguration,tokenConverter), UsernamePasswordAuthenticationFilter.class);
        super.configure(http);
    }


}
