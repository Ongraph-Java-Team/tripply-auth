package com.tripply.Auth.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.tripply.Auth.constants.AuthConstants.EXPIRATION;

@Data
@Entity
@Table(name = "password_reset_token", schema = "identity")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String token;

    private UUID userId;

    private Date expiryDate;

    private Date createdOn;

    public PasswordResetToken(){}

    public PasswordResetToken(String token, UUID userId) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public boolean isExpired(){
        Date now = new Date();
        return now.after(expiryDate);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
        return calendar.getTime();
    }
}
