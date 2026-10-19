package org.example.loginapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

public class RefreshTokenParameterWrapper extends HttpServletRequestWrapper {

    private final String refreshToken;

    public RefreshTokenParameterWrapper(HttpServletRequest request, String refreshToken) {
        super(request);
        this.refreshToken = refreshToken;
    }

    @Override
    public String getParameter(String name) {
        return "refresh_token".equals(name) ? refreshToken : super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return "refresh_token".equals(name) ? new String[]{refreshToken} : super.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new HashMap<>(super.getParameterMap());
        map.put("refresh_token", new String[]{refreshToken});
        return map;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> names = new HashSet<>(Collections.list(super.getParameterNames()));
        names.add("refresh_token");
        return Collections.enumeration(names);
    }
}