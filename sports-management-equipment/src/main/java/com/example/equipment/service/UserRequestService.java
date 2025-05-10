package com.example.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.constant.UserConstant;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;
import com.example.equipment.dto.SelectAllRequestDTO;
import com.example.equipment.dto.SelectUserRequestDTO;
import com.example.equipment.dto.utilDTO.RequestPageQuery;
import com.example.equipment.vo.AdminRequestVO;
import com.example.equipment.vo.RequestVO;

import java.util.List;

public interface UserRequestService extends IService<RequestVO> {

    void borrowEquipment(BorrowRequestDTO borrowRequestDTO, UserConstant currentUser);

    String revoke(RevokeRequestDTO requestDTO, UserConstant currentUser);

//    IPage<RequestVO> getUserRequestsPage(RequestPageQuery query, Long userId);

    List<AdminRequestVO> getUserRequest(SelectUserRequestDTO requestDTO, UserConstant currentUser);
}
