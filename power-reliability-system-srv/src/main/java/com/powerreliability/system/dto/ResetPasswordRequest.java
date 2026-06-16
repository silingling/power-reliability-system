package com.powerreliability.system.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private Long userId;
    private String newPassword;
}
