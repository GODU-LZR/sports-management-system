package com.example.equipment.controller.user;


import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;
import com.example.equipment.service.impl.UserRequestServiceImpl;
import com.example.equipment.service.impl.UserRequestServiceImpl;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
public class UserRequestController {

    @Autowired
    private UserRequestServiceImpl requestService;


    /**
     * 一次租用器材请求
     * 包含各个器材  总数量
     * @param borrowRequestDTO
     * @return
     */
    @PostMapping("/borrowRequest")
    public Result borrowRequest(@RequestBody BorrowRequestDTO borrowRequestDTO, @Parameter(hidden = true) UserConstant currentUser) {
        //用户对于器材的租借的请求
        requestService.borrowEquipment(borrowRequestDTO, currentUser);

        return Result.success();
    }

    /**
     * 查询当前用户下 提交的 器材申请
     * @param currentUser
     * @return
     */
    @GetMapping("/getAllRequest")
    public Result getUserRequest(@Parameter(hidden = true) UserConstant currentUser) {
//        requestService.getUserRequest(currentUser);

        return Result.success();
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
