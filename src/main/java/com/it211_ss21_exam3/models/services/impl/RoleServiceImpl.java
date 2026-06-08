package com.it211_ss21_exam3.models.services.impl;


import com.it211_ss21_exam3.exceptions.HttpNotFoundException;
import com.it211_ss21_exam3.models.constants.RoleName;
import com.it211_ss21_exam3.models.entities.Role;
import com.it211_ss21_exam3.models.repositories.IRoleRepository;
import com.it211_ss21_exam3.models.services.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {
    private final IRoleRepository roleRepository;

    @Override
    public Role findByRoleName(RoleName roleName) {
        return roleRepository.findByRoleName(roleName).orElseThrow(() -> new HttpNotFoundException("role: " + roleName + " not found"));
    }
}