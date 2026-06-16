package com.powerreliability.system.dto;

import lombok.Data;

@Data
public class ConfigUpdateRequest {
    private Long id;
    private String configKey;
    private String configValue;
    private String configDesc;
}
