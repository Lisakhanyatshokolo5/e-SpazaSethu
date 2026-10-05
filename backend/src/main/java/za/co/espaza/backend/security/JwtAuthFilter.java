package za.co.espaza.backend.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Read the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2. If there's no header or it doesn't start with "Bearer ", skip —
        //    Spring Security will handle the 401 automatically
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Pull the token out (everything after "Bearer ")
        final String token = authHeader.substring(7);
        final String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception ex) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. If we got a username and the user isn't already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtUtil.isTokenValid(token)) {

            UserPrincipal principal;
            try {
                principal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
            } catch (Exception ex) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!principal.isEnabled() || !jwtUtil.validateToken(token, principal)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 5. Create an authentication object and put it in the SecurityContext
            //    This is what tells Spring "this request is authenticated"
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 7. Continue to the next filter or controller
        filterChain.doFilter(request, response);
    }
}
