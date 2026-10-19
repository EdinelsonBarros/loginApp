package org.example.loginapp.security;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

public class RefreshTokenCookieFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "refresh_token";
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!"/oauth2/token".equals(request.getRequestURI())) {
            System.out.println(">>> RefreshTokenCookieFilter rodou para: " + request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }
        System.out.println(">>> Processando /oauth2/token");
        HttpServletRequest requestToUse = request;

        if ("refresh_token".equals(request.getParameter("grant_type"))) {
            String cookieValue = extractCookie(request);
            if (cookieValue != null) {
                requestToUse = new RefreshTokenParameterWrapper(request, cookieValue);
            }
        }

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        chain.doFilter(requestToUse, wrappedResponse);

        byte[] body = wrappedResponse.getContentAsByteArray();
        System.out.println(">>> Status: " + wrappedResponse.getStatus() + " | Body length: " + body.length);

        if (body.length > 0 && wrappedResponse.getStatus() == HttpServletResponse.SC_OK) {
            System.out.println(">>> BODY BRUTO: " + new String(body, StandardCharsets.UTF_8));

            Map<String, Object> json = mapper.readValue(body, new TypeReference<>() {});
            Object refreshToken = json.remove("refresh_token");
            System.out.println(">>> refresh_token encontrado: " + (refreshToken != null));
            if (refreshToken != null) {
                ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, refreshToken.toString())
                        .httpOnly(true)
                        .secure(false) // true em produção (HTTPS)
                        .sameSite("Strict")
                        .path("/oauth2/token")
                        .maxAge(Duration.ofDays(7))
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                System.out.println(">>> Cookie adicionado ao header");
            }

            byte[] newBody = mapper.writeValueAsBytes(json);
            response.setContentLength(newBody.length);
            response.getOutputStream().write(newBody);
        } else {
            wrappedResponse.copyBodyToResponse();
        }
    }

    private String extractCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(COOKIE_NAME))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}