package org.example.loginapp.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.util.Map;
import org.springframework.security.core.Authentication;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.web.csrf.CsrfToken;

@Controller
public class BffController {

    private final SecureRandom secureRandom = new SecureRandom();

    private final RestClient restClient = RestClient.create();

    @Value("${bff.token-uri:http://localhost:9000/oauth2/token}")
    private String tokenUri;

    @Value("${bff.client.secret}")
    private String clientSecret;


    @Value("${bff.authorization-uri:http://localhost:9000/oauth2/authorize}")
    private String authorizationUri;

    @Value("${bff.client.id:bff-client}")
    private String clientId;

    @Value("${bff.client.redirect-uri:http://localhost:9000/bff/callback}")
    private String redirectUri;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @GetMapping("/bff/login")
    public String login(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(true);

        String state = randomValue();
        String codeVerifier = randomValue();
        String codeChallenge = createCodeChallenge(codeVerifier);

        session.setAttribute("bff_oauth_state", state);
        session.setAttribute("bff_code_verifier", codeVerifier);

        String authorizationRequest = UriComponentsBuilder
                .fromUriString(authorizationUri)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid profile")
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build()
                .encode()
                .toUriString();

        return "redirect:" + authorizationRequest;
    }

    private String randomValue() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String createCodeChallenge(String codeVerifier) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
    }

    @GetMapping("/bff/callback")
    public String callback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session
    ) {
        String expectedState = (String) session.getAttribute("bff_oauth_state");
        String codeVerifier = (String) session.getAttribute("bff_code_verifier");

        // Consome os valores para que não possam ser reutilizados.
        session.removeAttribute("bff_oauth_state");
        session.removeAttribute("bff_code_verifier");

        if (expectedState == null
                || codeVerifier == null
                || !MessageDigest.isEqual(
                expectedState.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                state.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        )) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Retorno OAuth inválido");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("code_verifier", codeVerifier);

        Map<?, ?> tokens = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .body(form)
                .retrieve()
                .body(Map.class);

        if (tokens == null
                || tokens.get("access_token") == null
                || tokens.get("refresh_token") == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "O servidor de autorização não retornou access token e refresh token"
            );
        }

        session.setAttribute("bff_access_token", tokens.get("access_token"));
        session.setAttribute("bff_refresh_token", tokens.get("refresh_token"));
        session.setAttribute("bff_id_token", tokens.get("id_token"));

        return "redirect:" + frontendUrl + "/";
    }

    @GetMapping("/bff/me")
    @ResponseBody
    public Map<String, Object> me(
            Authentication authentication,
            HttpSession session
    ) {
        Object accessToken = session.getAttribute("bff_access_token");

        if (authentication == null || accessToken == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário não autenticado no BFF"
            );
        }

        return Map.of(
                "authenticated", true,
                "username", authentication.getName()
        );
    }

    @PostMapping("/bff/refresh")
    @ResponseBody
    public Map<String, Object> refresh(HttpSession session) {
        Object currentRefreshToken = session.getAttribute("bff_refresh_token");

        if (currentRefreshToken == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Não há refresh token na sessão. Faça login novamente."
            );
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", currentRefreshToken.toString());

        Map<?, ?> tokens;

        try {
            tokens = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                    .body(form)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                session.removeAttribute("bff_access_token");
                session.removeAttribute("bff_refresh_token");

                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "A sessão expirou. Faça login novamente."
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível renovar os tokens"
            );
        }

        if (tokens == null
                || tokens.get("access_token") == null
                || tokens.get("refresh_token") == null) {
            session.removeAttribute("bff_access_token");
            session.removeAttribute("bff_refresh_token");

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "A resposta não continha os tokens renovados esperados"
            );
        }

        // O refresh token também é atualizado porque ele é rotacionado.
        session.setAttribute("bff_access_token", tokens.get("access_token"));
        session.setAttribute("bff_refresh_token", tokens.get("refresh_token"));

        return Map.of("refreshed", true);
    }
    @PostMapping("/bff/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpSession session) {
        session.removeAttribute("bff_access_token");
        session.removeAttribute("bff_refresh_token");
        session.removeAttribute("bff_id_token");

        return Map.of("loggedOut", true);
    }

    @GetMapping("/bff/csrf")
    @ResponseBody
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}