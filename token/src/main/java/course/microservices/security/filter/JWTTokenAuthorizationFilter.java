package course.microservices.security.filter;

import com.nimbusds.jwt.SignedJWT;
import course.microservices.core.property.JWTConfiguration;
import course.microservices.security.token.converter.TokenConverter;
import course.microservices.security.token.creator.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;


@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JWTTokenAuthorizationFilter extends OncePerRequestFilter {
    protected final JWTConfiguration jwtConfiguration;
    protected final TokenConverter tokenConverter;
    @Override
    @SuppressWarnings("Duplicates")
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, @NonNull FilterChain filterChain) throws ServletException, IOException {
    String header = httpServletRequest.getHeader(jwtConfiguration.getHeader().getName());

    if(header == null || !header.startsWith(jwtConfiguration.getHeader().getPrefix())){
        filterChain.doFilter(httpServletRequest,httpServletResponse);
        return;
    }

    String token = header.replace(jwtConfiguration.getHeader().getPrefix(),"").trim();
    SecurityContextUtil.setSecurityContext(endsWithIgnoreCase("signed",jwtConfiguration.getType()) ? validate(token) : decryptAndValidate(token));

    filterChain.doFilter(httpServletRequest,httpServletResponse);
    }

    @SneakyThrows
    private SignedJWT decryptAndValidate(String encryptedToken){
        String signedToken = tokenConverter.decryptToken(encryptedToken);
        tokenConverter.validateTokenSignature(signedToken);

        return SignedJWT.parse(signedToken);
    }

    @SneakyThrows
    private SignedJWT validate(String signedToken){
        tokenConverter.validateTokenSignature(signedToken);
        return SignedJWT.parse(signedToken);
    }
}
