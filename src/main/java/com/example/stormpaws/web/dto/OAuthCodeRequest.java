package com.example.stormpaws.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthCodeRequest {
  @NotBlank(message = "Authorization code must not be blank")
  private String code;
}
