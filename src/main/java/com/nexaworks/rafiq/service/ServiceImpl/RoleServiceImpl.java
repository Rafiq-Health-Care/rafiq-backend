package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.enums.Roles;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
