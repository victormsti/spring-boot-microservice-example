package course.microservices.gateway.security.filter;

import com.netflix.zuul.context.RequestContext;
import com.nimbusds.jwt.SignedJWT;
import course.microservices.core.property.JWTConfiguration;
import course.microservices.security.filter.JWTTokenAuthorizationFilter;
import course.microservices.security.token.converter.TokenConverter;
import course.microservices.security.token.creator.SecurityContextUtil;
import lombok.SneakyThrows;
import org.springframework.lang.NonNull;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static course.microservices.security.token.creator.SecurityContextUtil.setSecurityContext;
import static org.apache.commons.lang.StringUtils.endsWithIgnoreCase;

public class GatewayJWTTokenAuthorizationFilter extends JWTTokenAuthorizationFilter {

    public GatewayJWTTokenAuthorizationFilter(JWTConfiguration jwtConfiguration, TokenConverter tokenConverter) {
        super(jwtConfiguration, tokenConverter);
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("Duplicates")
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = httpServletRequest.getHeader(jwtConfiguration.getHeader().getName());

        if(header == null || !header.startsWith(jwtConfiguration.getHeader().getPrefix())){
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        String token = header.replace(jwtConfiguration.getHeader().getPrefix(),"").trim();

        String signedToken = tokenConverter.decryptToken(token);

        tokenConverter.validateTokenSignature(signedToken);

        //Validate the roles
        setSecurityContext(SignedJWT.parse(signedToken));

        if(jwtConfiguration.getType().equalsIgnoreCase("signed")){
            RequestContext.getCurrentContext().addZuulRequestHeader(
                    "Authorization",jwtConfiguration.getHeader().getPrefix() + signedToken);
        }

        filterChain.doFilter(httpServletRequest,httpServletResponse);
    }
}
