package com.example.equipment.service;

import com.example.common.constant.UserConstant;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;

public interface UserRequestService {

    void borrowEquipment(BorrowRequestDTO borrowRequestDTO, UserConstant currentUser);

    String revoke(RevokeRequestDTO requestDTO, UserConstant currentUser);

}
