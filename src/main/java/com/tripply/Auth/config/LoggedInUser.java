package com.tripply.Auth.config;

import com.tripply.Auth.constants.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class LoggedInUser {
    private String userName;
    private List<String> role;
    private String userId;

}
