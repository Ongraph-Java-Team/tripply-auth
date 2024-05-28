package com.tripply.Auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role", schema = "identity")
public class Role extends BaseEntity {

    @Column(name="role_name")
    private String roleName;

}
