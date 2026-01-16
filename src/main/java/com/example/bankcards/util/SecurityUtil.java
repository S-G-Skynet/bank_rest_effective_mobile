package com.example.bankcards.util;


import com.example.bankcards.security.CustomUserPrinciple;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public Long getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        CustomUserPrinciple principal =
                (CustomUserPrinciple) authentication.getPrincipal();

        assert principal != null;
        return principal.getUserId();
    }
}

