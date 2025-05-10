package com.example.equipment.service.impl;


import com.example.common.constant.UserConstant;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.ReviewRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;
import com.example.equipment.dto.SelectAllRequestDTO;
import com.example.equipment.mapper.CategoryMapper;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.mapper.RequestMapper;
import com.example.equipment.pojo.EquipmentBorrowRequest;
import com.example.equipment.pojo.EquipmentId;
import com.example.equipment.service.AdminRequestService;
import com.example.equipment.vo.AdminRequestVO;
import com.example.equipment.vo.RequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AdminRequestServiceImpl implements AdminRequestService {

    @Autowired
    RequestMapper requestMapper;

    @Autowired
    EquipmentMapper equipmentMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    SnowflakeIdGenerator snowflakeIdGenerator;



    /**
     * 管理员审核
     * 审核通过器材的申请
     * @param currentUser
     */
    @Override
    public void reviewRequest(ReviewRequestDTO requestDTO, UserConstant currentUser) {

        Long userId = currentUser.getUserId();
        requestDTO.setUserId(userId);  //表示管理员要对该请求的状态更新

        Integer status = requestDTO.getStatus();

        //根据请求的Id查询所有的请求未审核的列表
        List<RequestVO> list1 = requestMapper.getStatus_0(requestDTO.getRequestId());

        //对于需要归还的器材  根据Id查询已 审核通过的器材
        List<RequestVO> list3 = requestMapper.getStatus_1(requestDTO.getRequestId());

        if(status==1)
        {
            //如果 发过来的请求为 1 即表示通过
            //库存信息及时更新

            //对Equipment 以及equipment_category表进行变动 对应的equipment_id
            for(RequestVO request :list1){

                //审核通过，将器材状态设置为不可用
                equipmentMapper.setEquipment_Status_To_0(request.getEquipmentId());//将请求列表的器材Id 传入 进行状态的修改

//                categoryMapper.BorrowEqp(request.getEquipmentId());
            }
        }
        if(status == 2){
            //拒接请求

            //依旧是对未审核的请求进行操作
            for(RequestVO requestVO : list1){
                /**
                 * 尽管不需要对器材的状态等信息修改
                 * 但是依旧需要对器材的账面库存修改
                 */
                categoryMapper.raiseBookStock(requestVO.getEquipmentId());
            }
        }
        //如果是归还
        if(status == 3){

            for(RequestVO request :list3)
            {
                log.info("归还的器材ID为:{}",request.getEquipmentId());
                //对该器材的状态进行更新
                equipmentMapper.setEquipment_Status_To_1(request.getEquipmentId());

                //对器材分类的数量进行更新
                categoryMapper.ReturnEqp(request.getEquipmentId());
            }
        }

//        根据传入的状态进行操作
        requestMapper.reviewRequest(requestDTO);
        log.info("管理员已对请求做出操作");

    }

    /**
     * 模糊获取所有的请求
     * @param requestDTO
     * @return
     */
    @Override
    public List<AdminRequestVO> getAllRequest(SelectAllRequestDTO requestDTO) {

        List<AdminRequestVO> requestList =requestMapper.selectRequestsByCriteria(requestDTO);

        return requestList;
    }



}
