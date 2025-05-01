package com.example.stormpaws.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class OAuthCodeRequest {
  @NotBlank(message = "Authorization code must not be blank")
  private String code;
}
