package com.example.equipment.controller.amin;

import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.ReviewRequestDTO;
import com.example.equipment.dto.SelectAllRequestDTO;
import com.example.equipment.dto.SelectUserRequestDTO;
import com.example.equipment.service.impl.AdminRequestServiceImpl;
import com.example.equipment.service.impl.AdminRequestServiceImpl;
import com.example.equipment.vo.AdminRequestVO;
import com.example.equipment.vo.RequestVO;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
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

    //展示所有用户的请求
    @GetMapping("/allRequest")
    public Result<List<AdminRequestVO>> getAllRequest(SelectAllRequestDTO requestDTO)
    {
        //系统根据用户需要租借的数量 自动分配可使用的器材 给用户

        try {
            // 调用 Service 层方法处理业务逻辑
            List<AdminRequestVO> list = requestService.getAllRequest(requestDTO);
           // 可以返回一个成功的消息
            return Result.success(list);

        } catch (IllegalArgumentException e) {
            // 捕获 Service 层抛出的参数校验异常
            // 返回错误结果，使用异常中的消息
            return Result.error(e.getMessage());
        }
    }



}
