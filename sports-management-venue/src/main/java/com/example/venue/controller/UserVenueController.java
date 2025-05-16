package com.example.venue.controller; // 根据你的项目结构修改包名

import com.example.venue.pojo.NearVenueParam; // 导入 NearVenueParam DTO/POJO
import com.example.venue.pojo.NearVenue; // 导入 NearVenue POJO
import com.example.venue.service.UserVenueServer; // 导入你的 Service 接口
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // 导入 HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // 用于返回空列表
import java.util.List; // 导入 List

/**
 * 用户场馆相关的 REST 控制器。
 * 负责接收客户端POST请求，调用 Service 层处理业务逻辑，并返回响应。
 */
@RestController // 标记这是一个 REST 控制器
@RequestMapping("/user") // 定义这个控制器所有接口的基础路径
public class UserVenueController { // 保留用户指定的类名

    // 注入 UserVenueServer 接口
    private final UserVenueServer userVenueServer; // 使用用户指定的 Service 接口名

    // 使用构造器注入是 Spring 推荐的方式
    // @Autowired // 在 Spring Boot 2.x+ 如果只有一个构造器，@Autowired 可以省略
    public UserVenueController(UserVenueServer userVenueServer) {
        this.userVenueServer = userVenueServer;
    }

    /**
     * 根据关键字和地理位置搜索附近场馆的接口。
     * 接收前端发送的 POST 请求到 /api/user-venue/searchNear.
     * 请求体是一个 JSON 对象，映射到 NearVenueParam 对象。
     *
     * @param param 包含搜索关键字、位置等参数的请求体对象 (NearVenueParam)。
     * @return 包含附近场馆搜索结果列表的 ResponseEntity。
     */
    @PostMapping("/searchNear") // 映射 POST 请求到 /api/user-venue/searchNear
    public ResponseEntity<?> searchNearVenues(@RequestBody NearVenueParam param) {
        // @RequestBody 注解会自动将请求体中的 JSON 数据转换成 NearVenueParam 对象

        // 简单的输入校验
        if (param == null) {
            return ResponseEntity.badRequest().body("Request body is missing.");
        }
        // 根据你的业务需求，可以添加对 param.getKey() 或 param.getLocation() 的非空校验
        // 例如：
        // if ((param.getKey() == null || param.getKey().trim().isEmpty()) &&
        //     (param.getLocation() == null || param.getLocation().trim().isEmpty())) {
        //      return ResponseEntity.badRequest().body("Either key or location must be provided for search.");
        // }


        try {
            // 调用 Service 层的方法处理业务逻辑
            // Service 方法 searchNearVenue 接收 NearVenueParam 对象
            List<NearVenue> searchResults = userVenueServer.searchNearVenue(param);

            // Service 方法返回 List<NearVenue>
            // 即使没有找到结果，Service 返回的也是一个空的 ArrayList，而不是 null
            // 所以直接返回 OK 状态码和结果列表即可
            return ResponseEntity.ok(searchResults);
            // 如果你希望在列表为空时返回 204 No Content，可以这样做：
            // if (searchResults != null && !searchResults.isEmpty()) {
            //     return ResponseEntity.ok(searchResults);
            // } else {
            //     return ResponseEntity.noContent().build(); // 或者 ResponseEntity.ok(Collections.emptyList());
            // }


        } catch (Exception e) {
            // 捕获 Service 层可能抛出的异常
            // 可以在这里记录日志
            System.err.println("Error during nearby venue search: " + e.getMessage()); // 使用 System.err 简单输出
            e.printStackTrace(); // 实际项目中应该使用日志框架，如 SLF4j

            // 返回 Internal Server Error (500) 状态码和错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during nearby venue search.");
            // 或者返回自定义的错误响应结构
            // return ResponseEntity.ok(Result.error("Search failed due to internal error"));
        }
    }

    // 你可以根据需要添加其他用户或场馆相关的接口
}
