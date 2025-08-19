package ru.hotdog.SecureHighloadAPI.dtos;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
