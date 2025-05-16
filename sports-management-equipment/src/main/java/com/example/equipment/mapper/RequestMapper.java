package com.example.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.equipment.dto.ReviewRequestDTO;
import com.example.equipment.dto.SelectAllRequestDTO;
import com.example.equipment.dto.SelectUserRequestDTO;
import com.example.equipment.pojo.EquipmentBorrowRequest;
import com.example.equipment.vo.AdminRequestVO;
import com.example.equipment.vo.RequestVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RequestMapper extends BaseMapper<RequestVO> {

    @Insert("insert into  equipment_request (status,request_id, user_id, equipment_id, quantity, start_time, end_time, create_time, modified_time, canceller_id)" +
            "VALUES(#{status},#{requestId},#{userId},#{equipmentId},#{quantity},#{startTime},#{endTime},#{createTime},#{modifiedTime},#{cancellerId})")
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

    @Update("update equipment_request set is_revoked = 1,status = 4 where request_id = #{requestId}")
    void setRevoke(Long requestId);

//    IPage<RequestVO> selectPage(IPage<RequestVO> page, LambdaQueryWrapper<RequestVO> queryWrapper);
//    IPage<RequestVO> selectPage(@Param("page") IPage<RequestVO> page, @Param("ew") Wrapper<RequestVO> queryWrapper);



//    /**
//     * 定义 AdminRequestVO 的结果映射
//     * 使用 @Results 和 @Result 映射主字段
//     * 使用 @Collection 映射 equipmentList 集合
//     */
//    @Results(id = "adminRequestVoMap", value = {
//            // @Id 用于指定主键，MyBatis 根据主键进行分组
//            @Result(property = "requestId", column = "request_id", id = true),
//            @Result(property = "userName", column = "user_id"), // 映射 user_id 到 userName
//            @Result(property = "startTime", column = "start_time"),
//            @Result(property = "endTime", column = "end_time"),
//            @Result(property = "status", column = "status"),
//            @Result(property = "createTime", column = "create_time"),
//            @Result(property = "modifiedTime", column = "modified_time"),
//            @Result(property = "cancellerId", column = "canceller_id"),
//            @Result(property = "reviewId", column = "review_id"),
//            @Result(property = "isRevoked", column = "is_revoked"),
//
//            // @Collection 用于映射集合字段 (List<BorrowEquipment>)
//            // property: AdminRequestVO 中的 List 字段名
//            // ofType: List 中元素的类型
//            // nestedResults: 引用另一个 @Results 或 @ResultMap 的 ID，这里直接内嵌定义
//            @Collection(property = "equipmentList", ofType = BorrowEquipment.class,
//                    results = {
//                            // 映射 BorrowEquipment 的字段，列名来自 JOIN 后的结果集
//                            @Result(property = "equipmentName", column = "equipment_name"), // 对应 SQL 中的 ec.name AS equipment_name
//                            @Result(property = "quantity", column = "quantity") // 对应 SQL 中的 er.quantity
//                            // 如果 BorrowEquipment 需要 equipmentId，可以添加 @Result(property = "equipmentId", column = "equipment_id")
//                    }
//            )
//    })
//    /**
//     * 根据查询条件查询请求列表
//     * 使用 JOIN 和 @Collection 映射将多个 equipment_request 行聚合到 AdminRequestVO
//     * @param dto 查询条件 DTO
//     * @return 符合条件的 AdminRequestVO 列表
//     */
//    @SelectProvider(type = RequestSqlProvider.class, method = "selectRequestsByCriteriaSql")
//    List<AdminRequestVO> selectRequestsByCriteria(@Param("dto") SelectAllRequestDTO dto);
//    // 使用 @Param("dto") 是因为在 RequestSqlProvider 中需要通过参数名访问 DTO 的属性


    /**
     * 根据查询条件查询请求列表
     * SQL 和结果映射在 RequestMapper.xml 中定义
     * @param dto 查询条件 DTO
     * @return 符合条件的 AdminRequestVO 列表
     */
    List<AdminRequestVO> selectRequestsByCriteria(@Param("dto") SelectAllRequestDTO dto);
    // @Param("dto") 仍然需要，以便在 XML 中通过 #{dto.propertyName} 访问 DTO 的属性


    /**
     * 查询指定用户的所有器材借用请求，并根据DTO中的条件进行筛选
     * @param requestDTO 筛选条件DTO
     * @param userId 用户ID
     * @return 用户请求列表，每个请求包含借用器材详情
     */
    List<AdminRequestVO> selectUserRequestsByCriteria(
            @Param("dto") SelectUserRequestDTO requestDTO,
            @Param("userId") Long userId
    );

    @Select("select * from equipment_request " +
            "where user_id = #{userId} " +
            "and equipment_id = #{equipmentId} " +
            "and status = 1 " +
            "AND #{now} BETWEEN start_time AND end_time " +
            "ORDER BY start_time ASC " +
            "limit 1") //在那个时间段内
    List<RequestVO> findPassedRequestByEquipmentId(@Param("equipmentId") Long equipmentId, @Param("userId") Long userId, @Param("now")LocalDateTime now);

    //设置为出库
    @Update("update equipment_request set status = 5 where request_id = #{requestId} and equipment_id = #{equipmentId}")
    void updateStatus_TO_5( @Param("requestId")Long requestId, @Param("equipmentId")Long equipmentId);

    //设置为已归还
    @Update("update equipment_request set status = 3 where request_id = #{requestId} and equipment_id = #{equipmentId}")
    void updateStatus_TO_3( @Param("requestId")Long requestId, @Param("equipmentId")Long equipmentId);

    @Select("select * from equipment_request " +
            "where user_id = #{userId} " +
            "and equipment_id = #{equipmentId} " +
            "and status = 5 " +
            "limit 1") //在那个时间段内
    List<RequestVO> findBorrowed_RequestByEquipmentId(@Param("equipmentId") Long equipmentId, @Param("userId") Long userId);
}
