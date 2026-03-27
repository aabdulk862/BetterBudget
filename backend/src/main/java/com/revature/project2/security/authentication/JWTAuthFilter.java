package com.revature.project2.security.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;



@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    private final ObjectProvider<AuthenticationManager> authManagerProvider;

    public JWTAuthFilter(ObjectProvider<AuthenticationManager> authManagerProvider) {
        this.authManagerProvider = authManagerProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader!=null ? authHeader.toLowerCase().contains("bearer") ? authHeader.substring(7) : null : null;
        if(token!=null){
            // authentication logic here
            AuthenticationManager authenticationManager = authManagerProvider.getObject();
            try {
                var result = authenticationManager.authenticate(new JWTAuthObj(token));
                if (result.isAuthenticated()) SecurityContextHolder.getContext().setAuthentication(result);
            }catch (BadCredentialsException e) {
                // Handle the case where JWT is invalid
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"%s\"}".formatted(e.getMessage()));
                return;  // Terminate filter chain here since authentication failed
            }
        }

        filterChain.doFilter(request,response);
    }
}
