package course.microservices.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import course.microservices.core.model.ApplicationUser;
import course.microservices.core.property.JWTConfiguration;
import course.microservices.security.token.creator.TokenCreator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Collections.emptyList;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JWTUserNameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    //$2a$10$nluVAVydDe2eI257h0TZ7O.RmV2IdiO2BeAjj7g8jIlsyiHBO.sN6
    private final AuthenticationManager authenticationManager;
    private final JWTConfiguration jwtConfiguration;
    private final TokenCreator tokenCreator;

    @Override
    @SneakyThrows
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response){
        log.info("Attemping authentication...");
        ApplicationUser applicationUser = new ObjectMapper().readValue(request.getInputStream(), ApplicationUser.class);
        if(applicationUser.getUsername() == null)
            throw new UsernameNotFoundException("Unable to retrieve username or password");

        log.info("Creating the authentication object for the user '{}' and calling UserDetailsServiceImp loadByUsername",applicationUser.getUsername());
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(applicationUser.getUsername(),applicationUser.getPassword(), emptyList());
        usernamePasswordAuthenticationToken.setDetails(applicationUser);
        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    @Override
    @SneakyThrows
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        log.info("Authentication successful for the user '{}', generating JWE token",authResult.getName());

        SignedJWT signedJWT = tokenCreator.createSignedJWT(authResult);
        String encryptedToken = tokenCreator.encryptToken(signedJWT);
        log.info("Token generated successfully, adding it to the Response Header");
        response.addHeader("Access-Control-Expose-Headers","XSRF-TOKEN, " + jwtConfiguration.getHeader().getName());
        response.addHeader(jwtConfiguration.getHeader().getName(),jwtConfiguration.getHeader().getPrefix() + encryptedToken);
    }
}
