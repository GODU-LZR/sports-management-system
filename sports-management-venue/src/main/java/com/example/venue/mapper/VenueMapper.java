package com.example.venue.mapper;

import com.example.venue.dto.VenueAddRequest;
import com.example.venue.dto.VenueQueryRequest;
import com.example.venue.dto.VenueUpdateRequest;
import com.example.venue.pojo.mysql.Venue;
import com.example.venue.vo.VenueOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VenueMapper {

    /**
     * 根据查询条件分页查询场地信息
     * @param venueQueryRequest 查询请求对象
     * @return 场地分页结果
     */
    List<Venue> listVenues(VenueQueryRequest venueQueryRequest);

    /**
     * 根据查询条件查询符合条件的场地信息
     * @param venueQueryRequest 查询请求对象
     * @return 符合条件的场地总数量
     */
    Integer listVenuesPage(VenueQueryRequest venueQueryRequest);

    /**
     * 新增场地
     * @param venueAddRequest 新增场地请求对象
     * @return 是否新增成功
     */
    Integer addVenue(VenueAddRequest venueAddRequest);

    /**
     * 修改场地
     * @param venueUpdateRequest 修改场地请求对象
     * @return 是否修改成功
     */
    Integer updateVenue(VenueUpdateRequest venueUpdateRequest);

    /**
     * 根据场地ID删除场地
     * @param venueId 场地ID
     * @return 是否删除成功
     */
    Integer deleteVenue(@Param("venueId") String venueId, @Param("auditId") String auditId);

    /**
     * 根据关键字搜索场地选项列表 (用于下拉框等)
     * @param key 关键字 (可选)
     * @return 场地选项列表
     */
    List<VenueOption> listVenueOptions(String key);

    /**
     * 根据一组场地ID查询场地列表
     * @param venueIds 场地ID列表
     * @return 场地列表
     */
    List<Venue> listVenuesByIds(List<String> venueIds);

    /**
     * es搜索场地
     * @return 场地列表
     */
    List<Venue> searchVenue();

}
