package com.securepay.securepayment.controller;

import com.securepay.securepayment.dto.LoginRequest;
import com.securepay.securepayment.dto.LoginResponse;
import com.securepay.securepayment.dto.RegisterRequest;
import com.securepay.securepayment.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}