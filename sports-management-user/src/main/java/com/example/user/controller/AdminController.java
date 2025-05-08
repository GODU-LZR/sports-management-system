package com.example.user.controller; // 确保包名正确

import com.baomidou.mybatisplus.core.metadata.IPage;
// 引入 Result 和 ResultCode
import com.example.common.response.Result;
import com.example.common.response.ResultCode;
import com.example.user.dto.UserProfileDTO;
import com.example.user.service.AdminService;
// 引入 Swagger 3 注解 (如果使用)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
// 引入 Spring Security 权限注解 (如果使用)
import org.springframework.security.access.prepost.PreAuthorize;
// 引入 Spring Web 注解
import org.springframework.web.bind.annotation.*;

import java.util.Map; // 导入 Map

@RestController
@RequestMapping("/admin") // 管理员操作用户的基本路径
@Slf4j
@Tag(name = "AdminController", description = "管理员用户管理接口") // Swagger 3 注解
public class AdminController {

    @Autowired
    private AdminService adminService; // 注入 AdminService

    /**
     * 分页、条件查询【普通用户】列表 (roleId=9)
     * (此方法保持不变)
     */
    @GetMapping("/user/list/normal")
    @Operation(summary = "获取普通用户列表（分页）", description = "根据条件筛选、搜索、排序并分页获取角色为普通用户(roleId=9)的列表")
    @Parameters({
            @Parameter(name = "currentPage", description = "当前页码", example = "1"),
            @Parameter(name = "pageSize", description = "每页数量", example = "12"),
            @Parameter(name = "searchQuery", description = "搜索关键字(编号/姓名/邮箱/用户名)", example = "张三"),
            @Parameter(name = "filterStatus", description = "按状态筛选(0:正常, 1:封15天, 2:封30天, 3:永久)", example = "0"),
            @Parameter(name = "sortBy", description = "排序字段(name, code, username, default)", example = "createTime"),
            @Parameter(name = "sortOrder", description = "排序顺序(asc, desc)", example = "desc")
    })
    @PreAuthorize("hasAuthority('ADMIN_USER_READ') or hasRole('SUPER_ADMIN')") // 示例权限控制
    public Result<IPage<UserProfileDTO>> listNormalUsers(
            @RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage,
            @RequestParam(value = "pageSize", defaultValue = "12") Integer pageSize,
            @RequestParam(value = "searchQuery", required = false) String searchQuery,
            @RequestParam(value = "filterStatus", required = false) Integer filterStatus,
            @RequestParam(value = "sortBy", defaultValue = "default") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder
    ) {
        log.info("收到获取普通用户列表请求: currentPage={}, pageSize={}, searchQuery={}, filterStatus={}, sortBy={}, sortOrder={}",
                currentPage, pageSize, searchQuery, filterStatus, sortBy, sortOrder);
        try {
            IPage<UserProfileDTO> userPage = adminService.listNormalUsers(currentPage, pageSize, searchQuery, filterStatus, sortBy, sortOrder);
            log.info("成功获取普通用户列表，总记录数: {}", userPage.getTotal());
            return Result.success(userPage);
        } catch (Exception e) {
            log.error("获取普通用户列表时发生错误", e);
            // 注意：如果 Service 层抛出了 BusinessException，全局异常处理器会处理
            // 这里可以捕获未预料的异常
            return Result.error(ResultCode.ERROR.getCode(), "获取用户列表失败，请稍后重试");
        }
    }

    /**
     * 管理员部分更新指定用户信息
     * 使用 PATCH 方法，通过路径传递用户 ID，请求体为包含更新字段的 Map
     *
     * @param userId     要更新的用户 ID (从路径获取)
     * @param updateData 包含要更新字段和值的 Map (请求体)
     * @return Result<UserProfileDTO> 更新后的用户信息
     */
    @PatchMapping("/user/{userId}") // 使用 PATCH 方法，路径包含 userId
    @Operation(summary = "管理员部分更新用户信息", description = "根据传入的字段更新指定用户的信息")
    @Parameter(name = "userId", description = "要更新的用户ID", required = true)
    @PreAuthorize("hasAuthority('ADMIN_USER_UPDATE') or hasRole('SUPER_ADMIN')") // 示例权限控制
    public Result<UserProfileDTO> updateUserProfileByAdmin(
            @PathVariable Long userId, // 从路径中获取 userId
            @RequestBody Map<String, Object> updateData) { // 从请求体中获取更新数据的 Map

        log.info("管理员请求更新用户 ID: {} 的信息, 更新字段: {}", userId, updateData.keySet());

        try {
            // 调用 Service 层方法进行更新
            UserProfileDTO updatedUserProfile = adminService.updateUserProfileByAdmin(userId, updateData);
            log.info("用户信息更新成功, User ID: {}", updatedUserProfile.getId());
            // 返回成功结果和更新后的用户信息
            return Result.success(updatedUserProfile);

        }
        // 注意：BusinessException 应该由全局异常处理器处理并返回统一格式的 Result.error
        // 这里可以捕获其他未预料的异常
        catch (Exception e) {
            log.error("管理员更新用户 ID: {} 信息时发生错误", userId, e);
            // 返回通用错误信息
            return Result.error(ResultCode.ERROR.getCode(), "更新用户信息失败，请稍后重试");
        }
    }

}
