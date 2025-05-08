package com.example.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.user.dto.UserProfileDTO; // 确保 DTO 路径正确

import java.util.Map;

/**
 * 管理员相关服务接口
 * (可以根据需要决定是否继承 IService<User>，如果此 Service 也负责 User 的通用 CRUD)
 */
public interface AdminService { // 如果需要通用 User CRUD，可以 extends com.baomidou.mybatisplus.extension.service.IService<com.example.user.pojo.User>

    /**
     * 分页、条件查询【普通用户】列表 (roleId=9)
     *
     * @param currentPage  当前页码
     * @param pageSize     每页显示条数
     * @param searchQuery  搜索关键字
     * @param filterStatus 筛选状态
     * @param sortBy       排序字段
     * @param sortOrder    排序顺序 ('asc' 或 'desc')
     * @return IPage<UserProfileDTO> 包含普通用户信息的分页结果
     */
    IPage<UserProfileDTO> listNormalUsers(Integer currentPage, Integer pageSize, String searchQuery, Integer filterStatus, String sortBy, String sortOrder);

    /**
     * 管理员部分更新指定用户信息
     * @param userId 要更新的用户 ID
     * @param updateData 包含要更新字段和值的 Map (键: 字段名, 值: 新值)
     * @return 更新后的用户信息 DTO
     * @throws RuntimeException 如果用户不存在或发生冲突
     */
    UserProfileDTO updateUserProfileByAdmin(Long userId, Map<String, Object> updateData); // 修改方法签名


}
