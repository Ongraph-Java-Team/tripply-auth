package com.tripply.Auth.config;

import com.tripply.Auth.constants.UserRole;
import com.tripply.Auth.service.AuthService;
import com.tripply.Auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Service
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuthService authService;
    private final LoggedInUser loggedInUser;

    public JwtFilter(HandlerExceptionResolver handlerExceptionResolver,
                     JwtUtil jwtUtil,
                     UserDetailsService userDetailsService,
                     AuthService authService,
                     LoggedInUser loggedInUser) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authService = authService;
        this.loggedInUser = loggedInUser;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        log.info(String.valueOf(request.getRequestURL()));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtUtil.extractUsername(jwt);

            if (authService.checkTokenIsBlocked(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("Access Denied, Token is blocked");
                return;
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                loggedInUser.setUserName(userDetails.getUsername());
                loggedInUser.setRole(UserRole.REGULAR_USER);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }


    }
}
