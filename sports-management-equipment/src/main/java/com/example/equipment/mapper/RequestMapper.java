package com.example.equipment.mapper;


import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.ReviewRequestDTO;
import com.example.equipment.pojo.EquipmentBorrowRequest;
import com.example.equipment.vo.RequestVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RequestMapper {

    @Insert("insert into  equipment_request (request_id, user_id, equipment_id, quantity, start_time, end_time, create_time, modified_time, canceller_id)" +
            "VALUES(#{requestId},#{userId},#{equipmentId},#{quantity},#{startTime},#{endTime},#{createTime},#{modifiedTime},#{cancellerId})")
    void insertBorrowRequest(EquipmentBorrowRequest request);

    /**
     * 用0123分别表示‘审核中’、‘已通过’、‘已拒绝’、‘已归还
     * @param requestDTO
     */
    @Update("update equipment_request set status = #{status},review_id = #{userId} where request_id = #{requestId}")
    void reviewRequest(ReviewRequestDTO requestDTO);

    /**
     * 根据请求的Id查询 未通过审核、已通过审核的租借请求
     * @param requestId
     * @return
     */
    @Select("select * from equipment_request where request_id = #{requestId} " +
            " and status = 0 or status = 1")
    List<RequestVO> selectUnrevoke(Long requestId);



    @Select("select * from equipment_request where status = 0 and request_id = #{requestId}")
    List<RequestVO> getStatus_0(Long requestId);

    @Select("select * from equipment_request where status = 1 and request_id = #{requestId}")
    List<RequestVO> getStatus_1(Long requestId);

    @Update("update equipment_request set is_revoked = 1 where request_id = #{requestId}")
    void setRevoke(Long requestId);

//    @Select("select * from equipment_request where user_id = {userId}")
}
