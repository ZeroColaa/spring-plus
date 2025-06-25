package org.example.expert.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.security.util.JwtUtil;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.security.userdetails.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String url = request.getRequestURI();
        if (url.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 토큰이 필요합니다.");
            return;
        }

        try {
            String jwt = jwtUtil.substringToken(bearerToken);
            Claims claims = jwtUtil.extractClaims(jwt);

            Long userId = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            String nickname = claims.get("nickname", String.class);
            UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

            UserPrincipal userPrincipal = new UserPrincipal(userId, email, nickname, userRole);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증에 실패했습니다.");
        }
    }
}
