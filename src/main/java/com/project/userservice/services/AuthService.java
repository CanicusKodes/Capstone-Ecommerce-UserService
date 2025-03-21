package com.project.userservice.services;

import com.project.userservice.dtos.UserDto;
import com.project.userservice.models.Role;
import com.project.userservice.models.Session;
import com.project.userservice.models.SessionStatus;
import com.project.userservice.models.User;
import com.project.userservice.repositories.SessionRepository;
import com.project.userservice.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<UserDto> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User user = userOptional.get();

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        List<Session> totalActiveSessions = sessionRepository.findAllByUser_IdAndSessionStatus(user.getId(), SessionStatus.ACTIVE);

        if(totalActiveSessions.size() >= 2){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }


//        String jwsToken = RandomStringUtils.randomAlphanumeric(30);
         //Create a test key suitable for the desired HMAC-SHA algorithm:
        MacAlgorithm alg = Jwts.SIG.HS512; //or HS384 or HS256
        SecretKey key = alg.key().build();

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("user_id", user.getId());
        claims.put("roles", List.of(user.getRoles()));
//        String message = "Hello World!";
//        byte[] content = message.getBytes(StandardCharsets.UTF_8);

// Create the compact JWS:
//        String jwsToken = Jwts.builder().content(content, "text/plain").signWith(key, alg).compact();
        String jwsToken = Jwts.builder().claims(claims).signWith(key,alg).compact();

// Parse the compact JWS:
//        content = Jwts.parser().verifyWith(key).build().parseSignedContent(jwsToken).getPayload();
//
//        assert message.equals(new String(content, StandardCharsets.UTF_8));

        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(jwsToken);
        session.setUser(user);
        session.setExpiringAt(DateUtils.addMinutes(new Date(),2));

        sessionRepository.save(session);

        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());

        //        Map<String, String> headers = new HashMap<>();
        //        headers.put(HttpHeaders.SET_COOKIE, token);

        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + jwsToken);



        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, headers, HttpStatus.OK);
        //        response.getHeaders().add(HttpHeaders.SET_COOKIE, token);

        return response;
    }

    public ResponseEntity<Void> logout(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        Session session = sessionOptional.get();

        session.setSessionStatus(SessionStatus.ENDED);

        sessionRepository.save(session);

        return ResponseEntity.ok().build();
    }

    public UserDto signUp(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }

    public SessionStatus validate(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        //session expiry check
        Session session = sessionOptional.get();
        Date currDate = new Date();
        if(currDate.after(session.getExpiringAt())){
            session.setSessionStatus(SessionStatus.ENDED);
            sessionRepository.save(session);
            return SessionStatus.ENDED;
        }


        //jwt decoding
        Jws<Claims> jwsClaims = Jwts.parser().build().parseSignedClaims(token);

        String email = jwsClaims.getPayload().get("email", String.class);

        List<Role> role = jwsClaims.getPayload().get("roles",List.class);

        return SessionStatus.ACTIVE;
    }

}

/*
Task-1 : Implement limit on number of active sessions for a user.
Task-2 : Implement login workflow using the token details with validation of expiry date.
*/