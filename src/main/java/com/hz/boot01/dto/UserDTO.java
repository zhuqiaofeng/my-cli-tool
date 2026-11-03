package com.hz.boot01.dto;

import lombok.Data;

/**
 * 用户数据传输对象
 * 用于前后端数据传输，不包含敏感信息
 */
@Data
public class UserDTO {

    private Integer id; // 用户ID

    private String userCode; // 用户编码

    private String userName; // 用户名称

    private Integer gender; // 性别（1:男 2:女）

    private String birthday; // 出生日期

    private String phone; // 联系电话

    private String address; // 联系地址

    private Integer userRole; // 用户角色ID

    private String roleName; // 角色名称（关联查询）

    private String creationDate; // 创建时间
}
