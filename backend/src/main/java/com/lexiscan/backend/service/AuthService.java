package com.lexiscan.backend.service;
import com.lexiscan.backend.dto.*; import com.lexiscan.backend.entity.User; import com.lexiscan.backend.repository.UserRepository; import com.lexiscan.backend.security.JwtService; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service;
@Service public class AuthService{
 private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
 public AuthService(UserRepository u,PasswordEncoder e,JwtService j){users=u;encoder=e;jwt=j;}
 public AuthResponse register(RegisterRequest r){String e=r.email().toLowerCase().trim();if(users.existsByEmail(e))throw new IllegalArgumentException("Email already registered");User u=new User();u.setName(r.name().trim());u.setEmail(e);u.setPassword(encoder.encode(r.password()));users.save(u);return new AuthResponse(jwt.generate(e),u.getName(),e);}
 public AuthResponse login(LoginRequest r){String e=r.email().toLowerCase().trim();User u=users.findByEmail(e).orElseThrow(()->new IllegalArgumentException("Invalid email or password"));if(!encoder.matches(r.password(),u.getPassword()))throw new IllegalArgumentException("Invalid email or password");return new AuthResponse(jwt.generate(e),u.getName(),e);}
}
