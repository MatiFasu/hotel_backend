package com.hackacode.hotel.security;

import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.repository.EmpleadoRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de autenticación JWT que intercepta cada petición HTTP,
 * extrae el token del header 'Authorization: Bearer <token>', valida su firma y expiración,
 * comprueba que el empleado permanezca activo en la base de datos y establece el SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final EmpleadoRepository empleadoRepository;

    public JwtAuthenticationFilter(JwtService jwtService, EmpleadoRepository empleadoRepository) {
        this.jwtService = jwtService;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();

        try {
            Claims claims = jwtService.validarToken(jwt);
            String username = claims.getSubject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Empleado empleado = empleadoRepository.findByUsername(username).orElse(null);

                // Si el empleado existe y está activo, se establece la autenticación en el contexto
                if (empleado != null && Boolean.TRUE.equals(empleado.getActivo())) {
                    String roleAuthority = "ROLE_" + empleado.getRol().name();
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            empleado.getUsername(),
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(roleAuthority))
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Token inválido, expirado o alterado: se limpia el contexto y continúa la cadena
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
