// (可以放在 common 包或 user 服务的 dto 包下)
package com.example.user.dto; // 或者 com.example.common.dto

import com.example.user.pojo.User; // 引入 User Pojo
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class UserProfileDTO {
    // 包含 User Pojo 的所有或部分字段
    private Long id;
    private String userCode;
    private String username;
    // 注意：通常不返回 password
    private String email;
    private String avatar;
    private String realName;
    private Integer status;
    private LocalDateTime banEndTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 新增：包含角色信息 (可以是角色代码列表或更详细的角色对象列表)
    private List<String> roleCodes; // 例如: ["SUPER_ADMIN", "USER"]
    // 或者 private List<RoleInfoDTO> roles; // 如果需要返回角色名等更多信息

    // 可以添加一个静态工厂方法或构造函数方便转换
    public static UserProfileDTO fromUser(User user, List<String> roleCodes) {
        UserProfileDTO dto = new UserProfileDTO();
        if (user != null) {
            dto.setId(user.getId());
            dto.setUserCode(user.getUserCode());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setAvatar(user.getAvatar());
            dto.setRealName(user.getRealName());
            dto.setStatus(user.getStatus());
            dto.setBanEndTime(user.getBanEndTime());
            dto.setCreateTime(user.getCreateTime());
            dto.setUpdateTime(user.getUpdateTime());
        }
        dto.setRoleCodes(roleCodes);
        return dto;
    }
}