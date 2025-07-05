package com.sportshop.api.Service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Collections;
import java.util.Map;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Repository.UserRepository;

@Service
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== CustomOAuth2UserService.loadUser() START ===");
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            Map<String, Object> attributes = oAuth2User.getAttributes();

            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String picture = (String) attributes.get("picture");

            System.out.println("=== CustomOAuth2UserService.loadUser ===");
            System.out.println("Email: " + email);
            System.out.println("Name: " + name);
            System.out.println("Picture: " + picture);

            // Lưu user Google vào database
            Users user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                try {
                    System.out.println("Creating new user for Google OAuth2");
                    user = new Users();
                    user.setEmail(email);
                    user.setFullName(name);
                    user.setAvatar(picture);
                    user.setProvider(Users.Provider.GOOGLE);
                    user.setRoleId(2L); // Role user thường
                    user.setActive(true); // Tự động kích hoạt cho Google login
                    user.setFirstLogin(false); // Không cần OTP cho Google login
                    user.setGender(Users.Gender.OTHER); // Default gender
                    user.setPassword(passwordEncoder.encode("GOOGLE_LOGIN_" + System.currentTimeMillis()));

                    System.out.println("About to save user to database...");
                    user = userRepository.save(user);
                    System.out.println("New user created with ID: " + user.getId());

                    // Verify user was saved
                    Users savedUser = userRepository.findByEmail(email).orElse(null);
                    if (savedUser != null) {
                        System.out.println("User successfully saved to database with ID: " + savedUser.getId());
                    } else {
                        System.err.println("ERROR: User was not found in database after save!");
                    }
                } catch (Exception e) {
                    System.err.println("ERROR saving user to database: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            } else {
                System.out.println("User already exists with ID: " + user.getId());
                // Nếu user đã tồn tại, cập nhật avatar và provider nếu cần
                boolean changed = false;
                if (user.getAvatar() == null && picture != null) {
                    user.setAvatar(picture);
                    changed = true;
                    System.out.println("Updated avatar");
                }
                if (user.getProvider() != Users.Provider.GOOGLE) {
                    user.setProvider(Users.Provider.GOOGLE);
                    changed = true;
                    System.out.println("Updated provider to GOOGLE");
                }
                if (changed) {
                    userRepository.save(user);
                    System.out.println("User updated in database");
                }
            }

            return new DefaultOAuth2User(
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "email");
        } catch (Exception e) {
            System.err.println("ERROR in CustomOAuth2UserService.loadUser(): " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            System.out.println("=== CustomOAuth2UserService.loadUser() END ===");
        }
    }
}