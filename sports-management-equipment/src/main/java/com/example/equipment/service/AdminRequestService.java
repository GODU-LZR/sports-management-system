package com.example.equipment.service;

import com.example.common.constant.UserConstant;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.ReviewRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;
import com.example.equipment.dto.SelectAllRequestDTO;
import com.example.equipment.vo.AdminRequestVO;

import java.util.List;

public interface AdminRequestService {


    void reviewRequest(ReviewRequestDTO requestDTO, UserConstant currentUser);

    List<AdminRequestVO> getAllRequest(SelectAllRequestDTO requestDTO);


//    void getUserRequest(UserConstant currentUser);
}
