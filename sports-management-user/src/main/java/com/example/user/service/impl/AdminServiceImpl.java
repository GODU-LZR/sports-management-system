package com.example.user.service.impl; // 确保包名正确

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.exception.BusinessException;
import com.example.user.dto.UserProfileDTO;
import com.example.user.mapper.UserMapper;
import com.example.user.pojo.User;
import com.example.user.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // 注入密码编码器

    @Override
    public IPage<UserProfileDTO> listNormalUsers(Integer currentPage, Integer pageSize, String searchQuery, Integer filterStatus, String sortBy, String sortOrder) {

        // 1. 参数校验与处理 (保持不变)
        String validatedSortOrder;
        if ("desc".equalsIgnoreCase(sortOrder)) {
            validatedSortOrder = "DESC";
        } else {
            validatedSortOrder = "ASC";
        }
        String validatedSortBy = ("default".equalsIgnoreCase(sortBy) || !StringUtils.hasText(sortBy)) ? null : sortBy;

        // 确保页码和大小有效
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (pageSize == null || pageSize < 1) pageSize = 12; // 或者你的默认值

        // 2. 先执行 Count 查询获取总数 (保持不变)
        long totalRecords = userMapper.countUsersByPage(searchQuery, filterStatus);
        log.debug("Manual count query returned total records: {}", totalRecords);

        List<User> userRecords;
        // 3. 【修改】计算 offset
        long offset = (long)(currentPage - 1) * pageSize;

        if (totalRecords > 0 && offset < totalRecords) { // 只有在总数大于0且偏移量小于总数时才查询
            // 4. 【修改】调用手动分页的 Mapper 方法
            log.debug("Executing findUsersManuallyPaginated with params: offset={}, limit={}, searchQuery={}, filterStatus={}, sortBy={}, sortOrder={}",
                    offset, pageSize, searchQuery, filterStatus, validatedSortBy, validatedSortOrder);
            userRecords = userMapper.findUsersManuallyPaginated(searchQuery, filterStatus, validatedSortBy, validatedSortOrder, offset, pageSize);
            log.debug("Manual pagination query returned {} records", userRecords.size());
        } else {
            // 如果总数为 0 或请求的页码超出了范围，则返回空列表
            log.debug("Total records is 0 or offset exceeds total, returning empty list.");
            userRecords = Collections.emptyList();
        }

        // 5. 【修改】手动构建 IPage<User> 对象用于后续转换
        IPage<User> userPageResult = new Page<>(currentPage, pageSize, totalRecords);
        userPageResult.setRecords(userRecords);
        // setPages 会基于 total 和 size 自动计算

        // 6. 结果转换 (保持不变)
        return convertToUserProfileDTOPage(userPageResult);
    }

    // convertToUserProfileDTOPage 方法保持不变...
    private IPage<UserProfileDTO> convertToUserProfileDTOPage(IPage<User> userPage) {
        IPage<UserProfileDTO> dtoPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        dtoPage.setPages(userPage.getPages());

        if (userPage.getRecords() != null && !userPage.getRecords().isEmpty()) {
            List<UserProfileDTO> dtoList = userPage.getRecords().stream()
                    .map(user -> {
                        List<String> roleCodes = List.of("USER"); // 简化处理
                        return UserProfileDTO.fromUser(user, roleCodes);
                    })
                    .collect(Collectors.toList());
            dtoPage.setRecords(dtoList);
        } else {
            dtoPage.setRecords(Collections.emptyList());
        }
        return dtoPage;
    }

    @Transactional // 添加事务管理
    @Override
    public UserProfileDTO updateUserProfileByAdmin(Long userId, Map<String, Object> updateData) {
        log.info("管理员尝试更新用户 ID: {} 的信息，更新字段: {}", userId, updateData.keySet());

        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (updateData == null || updateData.isEmpty()) {
            log.warn("用户 ID: {} 的更新请求未包含任何更新字段。", userId);
            User currentUser = userMapper.selectById(userId);
            if(currentUser == null) throw new BusinessException("用户id不能为空");
            return UserProfileDTO.fromUser(currentUser, Collections.emptyList()); // 传递空列表
        }

        // 1. 检查用户是否存在且未被删除
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            log.warn("用户 ID: {} 不存在或已被删除。", userId);
            throw new BusinessException("用户不存在");
        }

        // 2. 准备要更新的 User 对象
        User userToUpdate = new User();
        userToUpdate.setId(userId); // 必须设置ID

        boolean changed = false; // 标记是否有有效字段被修改

        // 3. 遍历 Map，处理允许更新的字段
        for (Map.Entry<String, Object> entry : updateData.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            switch (fieldName) {
                case "username":
                    if (value instanceof String && StringUtils.hasText((String) value) && !Objects.equals(value, existingUser.getUsername())) {
                        String newUsername = (String) value;
                        if(newUsername.length() < 3 || newUsername.length() > 20) {
                            throw new BusinessException("用户名长度必须在3到20之间");
                        }
                        userToUpdate.setUsername(newUsername);
                        log.debug("用户 ID: {} 用户名将更新为: {}", userId, newUsername);
                        changed = true;
                    }
                    break;
                case "realName":
                    if (value instanceof String && StringUtils.hasText((String) value) && !Objects.equals(value, existingUser.getRealName())) {
                        userToUpdate.setRealName((String) value);
                        log.debug("用户 ID: {} 真实姓名将更新为: {}", userId, value);
                        changed = true;
                    }
                    break;
                case "email":
                    if (value instanceof String && StringUtils.hasText((String) value) && !Objects.equals(value, existingUser.getEmail())) {
                        String newEmail = (String) value;
                        if (!newEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                            throw new BusinessException("邮箱格式不正确");
                        }
                        User userWithSameEmail = userMapper.selectByEmail(newEmail);
                        if (userWithSameEmail != null) {
                            throw new BusinessException("邮箱已经存在");
                        }
                        userToUpdate.setEmail(newEmail);
                        log.debug("用户 ID: {} 邮箱将更新为: {}", userId, newEmail);
                        changed = true;
                    }
                    break;
                case "avatar":
                    if (value == null || (value instanceof String && !Objects.equals(value, existingUser.getAvatar()))) {
                        userToUpdate.setAvatar((String) value);
                        log.debug("用户 ID: {} 头像将更新为: {}", userId, value);
                        changed = true;
                    }
                    break;
                case "status":
                    if (value instanceof Number) {
                        Integer newStatus = ((Number) value).intValue();
                        if (newStatus < 0 || newStatus > 3) { // 假设状态值范围是 0-3
                            throw new BusinessException("无效的用户状态值: " + newStatus);
                        }
                        if (!Objects.equals(newStatus, existingUser.getStatus())) {
                            userToUpdate.setStatus(newStatus);
                            log.debug("用户 ID: {} 状态将更新为: {}", userId, newStatus);
                            if (newStatus != 0 && existingUser.getBanEndTime() != null) {
                                userToUpdate.setBanEndTime(null);
                                log.debug("用户 ID: {} 状态非正常，清除封禁时间", userId);
                            }
                            changed = true;
                        }
                    } else if (value != null) {
                        log.warn("用户 ID: {} 提供的 status 值类型不正确: {}", userId, value.getClass().getName());
                        throw new BusinessException("用户状态值类型必须为数字");
                    }
                    break;
                case "password":
                    if (value instanceof String && StringUtils.hasText((String) value)) {
                        String newPassword = (String) value;
                        if(newPassword.length() < 6) {
                            throw new BusinessException("新密码长度不能少于6位");
                        }
                        userToUpdate.setPassword(passwordEncoder.encode(newPassword));
                        log.debug("用户 ID: {} 将更新密码", userId);
                        changed = true;
                    }
                    break;
                default:
                    log.warn("用户 ID: {} 的更新请求中包含无法识别或不允许更新的字段: {}", userId, fieldName);
                    break;
            }
        }

        // 4. 如果有有效字段被修改，则执行更新
        if (changed) {
            log.info("检测到用户 ID: {} 的信息变更，执行更新操作...", userId);
            userToUpdate.setUpdateTime(LocalDateTime.now()); // 设置更新时间
            int updatedRows = userMapper.updateByIdSelective(userToUpdate);
            if (updatedRows == 0) {
                log.error("更新用户 ID: {} 信息失败，数据库未返回影响行数。", userId);
                throw new BusinessException("更新用户信息数据库操作失败");
            }
            log.info("用户 ID: {} 信息更新成功。", userId);
        } else {
            log.info("用户 ID: {} 的信息未发生有效变化，无需更新数据库。", userId);
        }

        // 5. 查询最终用户信息并返回 DTO
        User finalUser = userMapper.selectById(userId);
        if (finalUser == null) {
            throw new BusinessException("更新后未能找到用户数据");
        }

        return UserProfileDTO.fromUser(finalUser, Collections.emptyList()); // 传递空的角色列表
    }

}
