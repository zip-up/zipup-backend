package com.zipup.server.user.domain;

import com.zipup.server.global.util.entity.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CustomUserDetails extends User implements UserDetails {
    private UUID id;
    private String email;
    private UserRole role;
    private String password;

    private CustomUserDetails(User User) {
        this.id = User.getId();
        this.email = User.getEmail();
        this.password = User.getPassword();
        this.role = User.getRole();
    }
    public static CustomUserDetails of(User user) {
        return new CustomUserDetails(user);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }


    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
