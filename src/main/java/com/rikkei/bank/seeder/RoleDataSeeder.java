package com.rikkei.bank.seeder;

import com.rikkei.bank.constants.RoleName;
import com.rikkei.bank.entity.Role;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.repository.RoleRepository;
import com.rikkei.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleDataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role adminRole = Role.builder().roleName(RoleName.ROLE_ADMIN).build();
            Role staffRole = Role.builder().roleName(RoleName.ROLE_STAFF).build();
            Role customerRole = Role.builder().roleName(RoleName.ROLE_CUSTOMER).build();

            roleRepository.save(adminRole);
            roleRepository.save(staffRole);
            roleRepository.save(customerRole);

            log.info("Seeded roles: ADMIN, STAFF, CUSTOMER");
        }

        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN).orElseThrow();

            User admin = User.builder()
                    .fullName("System Administrator")
                    .username("admin")
                    .email("quynh2682@icloud.com")
                    .password(passwordEncoder.encode("Qazqaz147147"))
                    .pin(passwordEncoder.encode("147147"))
                    .isKyc(true)
                    .isLocked(false)
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(admin);
            log.info("Seeded admin user: admin/adminQuynh");
        }
    }
}