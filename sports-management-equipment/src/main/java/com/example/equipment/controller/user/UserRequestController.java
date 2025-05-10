package com.example.equipment.controller.user;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;
import com.example.equipment.dto.SelectAllRequestDTO;
import com.example.equipment.dto.SelectUserRequestDTO;
import com.example.equipment.dto.utilDTO.RequestPageQuery;
import com.example.equipment.service.UserRequestService;
import com.example.equipment.service.impl.UserRequestServiceImpl;
import com.example.equipment.service.impl.UserRequestServiceImpl;
import com.example.equipment.vo.AdminRequestVO;
import com.example.equipment.vo.RequestVO;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping()
public class UserRequestController {

    @Autowired
    private UserRequestServiceImpl requestService;


    /**
     * 一次租用器材请求
     * 包含一类器材  总数量
     * @param borrowRequestDTO
     * @return
     */
    @PostMapping("/borrowRequest")
    public Result borrowRequest(@RequestBody BorrowRequestDTO borrowRequestDTO, @Parameter(hidden = true) UserConstant currentUser) {

        //用户对于器材的租借的请求

        // 可以在这里进行一些基本的参数校验，或者依赖Service层的校验
        if (borrowRequestDTO == null) {
            return Result.error("请求参数不能为空");
        }
        //系统根据用户需要租借的数量 自动分配可使用的器材 给用户

        try {
            // 调用 Service 层方法处理业务逻辑
            requestService.borrowEquipment(borrowRequestDTO, currentUser);

            // 如果Service方法成功执行（没有抛出异常），则返回成功结果
            log.info("用户 {} 的器材借用请求处理成功。", currentUser.getUserId());
            return Result.success("借用申请已提交"); // 可以返回一个成功的消息

        } catch (IllegalArgumentException e) {
            // 捕获 Service 层抛出的参数校验异常
            log.warn("用户 {} 的借用请求参数无效: {}", currentUser.getUserId(), e.getMessage());
            // 返回错误结果，使用异常中的消息
            return Result.error(e.getMessage());

        } catch (IllegalStateException e) {
            // 捕获 Service 层抛出的业务状态异常 (例如库存不足)
            log.warn("用户 {} 的借用请求业务处理失败: {}", currentUser.getUserId(), e.getMessage());
            // 返回错误结果，使用异常中的消息
            return Result.error(e.getMessage());

        } catch (Exception e) {
            // 捕获其他未知异常，避免将敏感信息返回给前端
            log.error("用户 {} 的借用请求发生未知错误", currentUser.getUserId(), e);
            // 返回一个通用的错误信息
            return Result.error("系统内部错误，请稍后再试");
        }
    }
    /**
     * 查询当前用户下 提交的 器材申请 (分页)
     * @param currentUser 通过拦截器或AOP获取的当前用户对象
     * @return 分页的器材申请列表
     */
    @GetMapping("/getUserRequest")
    //展示所有用户的请求
    public Result<List<AdminRequestVO>> getUserRequest(SelectUserRequestDTO requestDTO, @Parameter(hidden = true) UserConstant currentUser)
    {
        //系统根据用户需要租借的数量 自动分配可使用的器材 给用户

        try {
            // 调用 Service 层方法处理业务逻辑
            List<AdminRequestVO> list = requestService.getUserRequest(requestDTO,currentUser);

            // 可以返回一个成功的消息
            return Result.success(list);

        } catch (IllegalArgumentException e) {
            // 捕获 Service 层抛出的参数校验异常
            // 返回错误结果，使用异常中的消息
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户 想对 自己发 发起申请的请求 进行撤销
     *   包括 未审核(无需赔偿费用)
     *   审核通过(包含未到预约时间、超过了预约时间---赔偿费用)
     * @param requestDTO
     * @param currentUser
     * @return
     */
//    用户可以对为审核的 申请器材进行 撤销
    @PutMapping("/revokeRequest")
    public Result revokeRequest(@RequestBody RevokeRequestDTO requestDTO, @Parameter(hidden = true) UserConstant currentUser){

        String revoke = requestService.revoke(requestDTO, currentUser);

        return Result.success(revoke);
    }


}
