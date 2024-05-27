package com.tripply.Auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = false)
@Builder
@Table(name = "token", schema = "identity")
public class Token extends BaseEntity{

    @Column(name="blocked_token")
    private String blockedToken;

    @Column(name="expires_in")
    private long expirationTime;
}
