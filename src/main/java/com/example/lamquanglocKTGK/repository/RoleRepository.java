package com.example.lamquanglocKTGK.repository;

import com.example.lamquanglocKTGK.model.Role;
import com.example.lamquanglocKTGK.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
