package com.github.deniskoriavets.sportvision.security;

import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ParentRepository parentRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username)
        throws UsernameNotFoundException {
        return parentRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
