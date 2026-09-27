package za.co.espaza.backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // This will be injected once Lisakhanya creates UserRepository
    // private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: Replace this with real DB lookup once UserRepository exists:
        // UserEntity user = userRepository.findByUsername(username)
        //     .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        // return new User(
        //     user.getUsername(),
        //     user.getPasswordHash(),
        //     List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        // );

        throw new UsernameNotFoundException("User not found: " + username);

    }
}
