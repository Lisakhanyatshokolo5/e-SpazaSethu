package za.co.espaza.backend.controller;
import za.co.espaza.backend.dto.request.LoginRequest;
import za.co.espaza.backend.dto.response.LoginResponse;
import za.co.espaza.backend.security.CustomUserDetailsService;
import za.co.espaza.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // This line does two things: checks the username exists,
            // and verifies the password matches the BCrypt hash in the DB
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Incorrect username or password");
        }

        // If we get here, credentials are valid — generate a token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtUtil.generateToken(userDetails);

        // TODO: Once UserRepository exists, load the real userId and role from DB
        // For now this returns a placeholder userId
        LoginResponse response = new LoginResponse(
                token,
                new LoginResponse.UserInfo("temp-id", request.username(),
                        userDetails.getAuthorities().iterator().next().getAuthority())
        );

        return ResponseEntity.ok(response);
    }
}
