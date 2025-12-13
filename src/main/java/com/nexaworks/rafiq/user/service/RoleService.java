package com.nexaworks.rafiq.user.service;

import com.nexaworks.rafiq.user.entity.enums.Roles;
import com.nexaworks.rafiq.user.entity.model.Role;

public interface RoleService {
    Role getRole(Roles roles);
}
