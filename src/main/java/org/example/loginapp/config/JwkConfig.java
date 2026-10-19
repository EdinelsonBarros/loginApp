package org.example.loginapp.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;
import org.example.loginapp.security.RefreshTokenParameterWrapper;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
public class JwkConfig {
    @Value("${bff.client.secret}")
    private String bffClientSecret;

    @Value("${bff.client.redirect-uri:http://localhost:9000/bff/callback}")
    private String bffCallbackUri;

    @Value("${bff.signing.keystore}")
    private Resource signingKeyStore;

    @Value("${bff.signing.alias}")
    private String signingKeyAlias;

    @Value("${bff.signing.password}")
    private String signingKeyStorePassword;

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,RegisteredClientRepository registeredClientRepository,
                                                                      OAuth2AuthorizationService authorizationService) throws Exception {
        http
                .oauth2AuthorizationServer(authorizationServer -> {
                    http.securityMatcher(authorizationServer.getEndpointsMatcher());

                    authorizationServer
                            .registeredClientRepository(registeredClientRepository)
                            .authorizationService(authorizationService)
                            .oidc(Customizer.withDefaults());
                })
                .oauth2AuthorizationServer((authorizationServer) -> {
                    http.securityMatcher(authorizationServer.getEndpointsMatcher());
                    authorizationServer.oidc(Customizer.withDefaults()); // habilita OpenID Connect
                })
                .authorizeHttpRequests((authorize) ->
                        authorize.anyRequest().authenticated()
                )
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityContext(context ->
                        context.securityContextRepository(securityContextRepository())
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/register"))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/auth/register",  "/register", "/bff/login", "/bff/callback", "/error", "/css/**", "/fonts/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/bff/login", true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        char[] password = signingKeyStorePassword.toCharArray();

        try (InputStream input = signingKeyStore.getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(input, password);

            PrivateKey privateKey =
                    (PrivateKey) keyStore.getKey(signingKeyAlias, password);

            X509Certificate certificate =
                    (X509Certificate) keyStore.getCertificate(signingKeyAlias);

            if (privateKey == null || certificate == null) {
                throw new IllegalStateException(
                        "A chave ou o certificado não foi encontrado no arquivo PKCS12"
                );
            }

            RSAKey rsaKey = new RSAKey.Builder(
                    (RSAPublicKey) certificate.getPublicKey()
            )
                    .privateKey((RSAPrivateKey) privateKey)
                    .keyID(signingKeyAlias)
                    .build();

            return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar a chave de assinatura",
                    exception
            );
        }
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${APP_PUBLIC_URL:http://localhost:9000}") String publicUrl
    ) {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:9000")
                .build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${bff.client.secret}") String clientSecret,
            @Value("${bff.client.redirect-uri:http://localhost:9000/bff/callback}")
            String redirectUri
    ) {
        JdbcRegisteredClientRepository repository =
                new JdbcRegisteredClientRepository(jdbcTemplate);

        if (repository.findByClientId("bff-client") == null) {
            RegisteredClient bffClient = RegisteredClient
                    .withId(UUID.randomUUID().toString())
                    .clientId("bff-client")
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .clientAuthenticationMethod(
                            ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(
                            AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(
                            AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri(redirectUri)
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .requireProofKey(true)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(10))
                            .refreshTokenTimeToLive(Duration.ofDays(7))
                            .reuseRefreshTokens(false)
                            .build())
                    .build();

            repository.save(bffClient);
        }

        return repository;
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository
    ) {
        return new JdbcOAuth2AuthorizationService(
                jdbcTemplate,
                registeredClientRepository
        );
    }
}