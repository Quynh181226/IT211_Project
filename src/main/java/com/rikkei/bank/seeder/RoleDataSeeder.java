//package com.rikkei.bank.seeder;
//
//
//import com.rikkei.bank.models.constants.RoleName;
//import com.rikkei.bank.models.entities.Role;
//import com.rikkei.bank.models.repositories.IRoleRepository;
//import lombok.RequiredArgsConstructor;
//import org.jspecify.annotations.NonNull;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//
//@Component
//@RequiredArgsConstructor
//public class RoleDataSeeder implements CommandLineRunner {
//    private final IRoleRepository roleRepository;
//
//    @Override
//    public void run(String @NonNull ... args) throws Exception {
//        if (roleRepository.count() == 0) {
//
//            Role adminRole = new Role();
//            adminRole.setRoleName(RoleName.ROLE_ADMIN);
//
//            Role userRole = new Role();
//            userRole.setRoleName(RoleName.ROLE_USER);
//
//            roleRepository.saveAll(Arrays.asList(adminRole, userRole));
//
//            System.out.println("Đã seed dữ liệu Role thành công!");
//        } else {
//            System.out.println("Dữ liệu Role đã tồn tại, bỏ qua bước seeder.");
//        }
//    }
//}

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
        // Seed Roles
        if (roleRepository.count() == 0) {
            Role adminRole = Role.builder().roleName(RoleName.ROLE_ADMIN).build();
            Role staffRole = Role.builder().roleName(RoleName.ROLE_STAFF).build();
            Role customerRole = Role.builder().roleName(RoleName.ROLE_CUSTOMER).build();

            roleRepository.save(adminRole);
            roleRepository.save(staffRole);
            roleRepository.save(customerRole);

            log.info("Seeded roles: ADMIN, STAFF, CUSTOMER");
        }

        // Seed Admin User (nếu chưa có)
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN).orElseThrow();

            User admin = User.builder()
                    .fullName("System Administrator")
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .pin(passwordEncoder.encode("123456"))
                    .isKyc(true)
                    .isLocked(false)
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(admin);
            log.info("Seeded admin user: admin/admin123");
        }
    }
}