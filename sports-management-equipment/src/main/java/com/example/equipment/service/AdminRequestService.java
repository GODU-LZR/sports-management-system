package com.example.equipment.service;

import com.example.common.constant.UserConstant;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.ReviewRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;

public interface AdminRequestService {


    void reviewRequest(ReviewRequestDTO requestDTO, UserConstant currentUser);





//    void getUserRequest(UserConstant currentUser);
}
