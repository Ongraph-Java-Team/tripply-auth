package com.tripply.Auth.entity;

import com.tripply.Auth.constants.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user", schema = "identity")
public class User extends BaseEntity implements UserDetails {

    @Column(name="first_name", length=100)
    private String firstName;

    @Column(name="last_name", length=100)
    private String lastName;

    @Column(name="phone_number", unique = true, length = 20)
    private String phoneNumber;

    @Column(name="email", unique = true, length=100)
    private String email;

    @Column(name="country_code")
    private String countryCode;

    @Column(name="password")
    private String password;

    @Column(name="role", length=20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name="enabled")
    private boolean enabled;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
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
        return enabled;
    }
}
