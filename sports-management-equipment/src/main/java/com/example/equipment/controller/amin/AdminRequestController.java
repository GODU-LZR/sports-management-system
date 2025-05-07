package com.example.equipment.controller.amin;

import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.ReviewRequestDTO;
import com.example.equipment.service.impl.AdminRequestServiceImpl;
import com.example.equipment.service.impl.AdminRequestServiceImpl;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AdminRequestController {

    @Autowired
    private AdminRequestServiceImpl requestService;


    /**
     * 后端根据请求的 对象  动态的根据 状态参数  进行审核通过或拒绝 和回收
     * @param requestDTO
     * @param currentUser
     * @return
     */
    @PutMapping("/reviewRequest")
    public Result reviewRequest(@RequestBody ReviewRequestDTO requestDTO, @Parameter(hidden = true) UserConstant currentUser)
    {

        requestService.reviewRequest(requestDTO,currentUser);

        return Result.success();
    }



}
