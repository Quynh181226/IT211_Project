package com.it211_ss21_exam3.models.services;


import com.it211_ss21_exam3.models.constants.RoleName;
import com.it211_ss21_exam3.models.entities.Role;

public interface IRoleService {
    Role findByRoleName(RoleName roleName);
}
