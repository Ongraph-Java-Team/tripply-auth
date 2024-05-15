package com.tripply.Auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class RoleDto {

    @NotNull(message = "Role name is required")
    private String roleName;
}
