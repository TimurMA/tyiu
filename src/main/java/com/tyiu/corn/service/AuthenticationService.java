package com.tyiu.corn.service;

import com.tyiu.corn.model.entities.User;
import com.tyiu.corn.model.enums.Role;
import com.tyiu.corn.model.requests.LoginRequest;
import com.tyiu.corn.model.requests.RegisterRequest;
import com.tyiu.corn.model.responses.AuthenticationResponse;
import com.tyiu.corn.repository.UserRepository;
import com.tyiu.corn.util.security.JwtCore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;

    public AuthenticationResponse login(LoginRequest request){
        Authentication authentication = null;
        User user = null;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        }
        catch (Exception e){
            log.error(e.toString());
        }
        if (authentication != null) {
            String jwt = jwtCore.generateToken(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Not Found"));
            return AuthenticationResponse.builder()
                    .email(user.getEmail())
                    .jwt(jwt)
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();
        }
        else return null;
    }
    public AuthenticationResponse register(RegisterRequest request){
        Authentication authentication = null;
        User user = User.builder()
                .roles(List.of(Role.ADMIN))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        }
        catch (Exception e){
            log.error(e.toString());
        }
        if (authentication != null) {
            String jwt = jwtCore.generateToken(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Not Found"));
            return AuthenticationResponse.builder()
                    .email(user.getEmail())
                    .jwt(jwt)
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();
        }
        else return null;
    }
}
