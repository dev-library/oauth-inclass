package com.makersworld.oauth_inclass.service;  

import com.makersworld.oauth_inclass.dto.GoogleUserInfoResponse;
import com.makersworld.oauth_inclass.enums.Provider;
import com.makersworld.oauth_inclass.enums.Role;
import com.makersworld.oauth_inclass.model.User;
import com.makersworld.oauth_inclass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createOrUpdateGoogleUser(GoogleUserInfoResponse googleUser) {
        Optional<User> existingUser = findByEmail(googleUser.getEmail());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.updateName(googleUser.getName());
            return userRepository.save(user);
        } else {
            User newUser = User.builder()
                    .email(googleUser.getEmail())
                    .name(googleUser.getName())
                    .provider(Provider.GOOGLE)
                    .providerId(googleUser.getId())
                    .role(Role.USER)
                    .build();
            return userRepository.save(newUser);
        }
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}