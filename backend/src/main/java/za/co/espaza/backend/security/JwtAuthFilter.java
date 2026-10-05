package za.co.espaza.backend.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
@Component
public class JwtAuthFilter extends OncePerRequestFilter{
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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
        final String username = jwtUtil.extractUsername(token);

        // 4. If we got a username and the user isn't already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtUtil.isTokenValid(token)) {

            UUID userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            List<GrantedAuthority> authorities = (role == null)
                    ? List.of()
                    : List.of(new SimpleGrantedAuthority(role));

            // The signed token is trusted for the userId + role it carries.
            // TODO Backend Issue 1: load the user from the database here and reject
            //  users that no longer exist or have been deactivated.
            UserPrincipal principal = new UserPrincipal(userId, username, null, true, authorities);

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
