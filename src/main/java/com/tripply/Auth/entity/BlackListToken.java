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
@Table(name = "blackListToken", schema = "identity")
public class BlackListToken extends BaseEntity{

    @Column(name="token_value", length = 1000)
    private String tokenValue;

    @Column(name="expires_in")
    private long expirationTime;
}
