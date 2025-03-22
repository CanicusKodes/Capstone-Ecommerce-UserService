package com.project.userservice.security;

import com.project.userservice.models.User;
import com.project.userservice.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomSpringUserDetails implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomSpringUserDetails(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Optional<User> userDetails =  userRepository.findByEmail(username);
//
//        if(!userDetails.isPresent()) {
//            throw new UsernameNotFoundException(username);
//        }

        //lambda expression for above lines
        User userDetails =userRepository.findByEmail(username).orElseThrow(() -> {throw new UsernameNotFoundException("User not found");});

        return new CustomUserDetails(userDetails);
    }
}
