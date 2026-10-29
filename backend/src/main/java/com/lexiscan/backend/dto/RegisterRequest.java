package com.lexiscan.backend.dto;
import jakarta.validation.constraints.*;
public record RegisterRequest(@NotBlank String name,@Email @NotBlank String email,@Size(min=6,max=100) String password){}
