package com.nexaworks.rafiq.user.service.implementation;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.user.entity.model.Role;
import com.nexaworks.rafiq.user.entity.enums.Roles;
import com.nexaworks.rafiq.user.repository.RoleRepository;
import com.nexaworks.rafiq.user.service.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role getRole(Roles roles) {
        return roleRepository.findByName(roles.toString());
    }
}
