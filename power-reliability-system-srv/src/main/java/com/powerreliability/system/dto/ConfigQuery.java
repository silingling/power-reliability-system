package com.powerreliability.system.dto;

import lombok.Data;

@Data
public class ConfigQuery {
    private String keyword;
    private int page = 1;
    private int pageSize = 20;
}
