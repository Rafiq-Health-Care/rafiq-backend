package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.enums.Roles;

public interface RoleService {
    Role getRole(Roles roles);
}
