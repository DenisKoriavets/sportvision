package com.github.deniskoriavets.sportvision.security;

import com.github.deniskoriavets.sportvision.entity.Parent;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityFacade {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public Parent getCurrentUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Parent parent) {
            return parent;
        }
        throw new IllegalStateException("User is not authenticated");
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean hasRole(String role) {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        return auth != null && auth.isAuthenticated() && 
               !(auth instanceof AnonymousAuthenticationToken);
    }
}