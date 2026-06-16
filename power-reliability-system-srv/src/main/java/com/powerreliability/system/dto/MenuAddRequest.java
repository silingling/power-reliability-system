package com.powerreliability.system.dto;

import lombok.Data;

@Data
public class MenuAddRequest {
    private String menuName;
    private Long parentId;
    private Integer menuType;
    private String path;
    private String component;
    private String permissionKey;
    private String icon;
    private Integer sortOrder;
    private Integer visible;
}
