package com.example.common.constant;

import com.example.common.dto.UserRoleWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConstant extends UserRoleWrapper {
    private Long userId;
    private String userCode;
    private String username;
    private String email;
    private Integer status;
    private List<RoleInfo> roles;

}
