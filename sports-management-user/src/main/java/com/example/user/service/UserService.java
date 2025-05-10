package com.example.user.service;

import com.example.common.response.Result;
import com.example.user.dto.UserProfileDTO;
import com.example.user.pojo.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    Result<User> createUser(User user);
    Result<Void> deleteUser(Long id);
    Result<User> updateUser(User user);
    Result<User> getUserById(Long id);
    Result<User> getUserByUsername(String username);
    Result<User> getUserByEmail(String email);
    Result<List<User>> getAllUsers();
    /**
     * 新增：根据用户ID获取用户的详细信息，包含角色代码
     * @param userId 用户ID
     * @return 包含角色信息的 UserProfileDTO
     */
    Result<UserProfileDTO> getUserProfileById(Long userId);
    /**
     * 新增/修改：更新当前登录用户的个人资料 (部分更新)
     * @param userId 当前用户ID
     * @param updateData 包含要更新字段的 Map (key 为字段名，可能包含 fileRecordId)
     * @return 更新成功后返回包含最新信息的 UserProfileDTO
     */
    Result<UserProfileDTO> updateCurrentUserProfile(Long userId, Map<String, Object> updateData);
    /**
     * 注销（软删除）当前登录用户
     * @param userId 当前登录用户的ID
     * @param emailCode 用于验证操作的邮箱验证码
     * @param password 用户输入的当前密码，用于校验
     * @return 操作是否成功 (Result<Boolean>)
     */
    Result<Boolean> deactivateCurrentUser(Long userId, String emailCode, String password); // 添加 password 参数


}
