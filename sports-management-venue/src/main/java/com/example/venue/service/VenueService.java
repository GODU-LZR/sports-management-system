package com.example.venue.service;

// 可能需要导入其他接口或类，取决于你未来要定义的方法签名
// import ...;

import com.example.venue.dto.VenueAddRequest;
import com.example.venue.dto.VenueQueryRequest;
import com.example.venue.dto.VenueUpdateRequest;
import com.example.venue.pojo.mysql.Venue;
import com.example.venue.vo.VenueOption;
import com.example.venue.vo.VenuePage;
import java.util.List;

/**
 * Venue 服务接口
 * 定义了对 Venue 实体进行操作的契约
 */
public interface VenueService {

    /**
     * 根据查询条件分页查询场地信息
     * @param venueQueryRequest 查询请求对象
     * @return 场地分页结果
     */
    VenuePage listVenues(VenueQueryRequest venueQueryRequest);

    /**
     * 新增场地
     * @param venueAddRequest 新增场地请求对象
     * @return 是否新增成功
     */
    boolean addVenue(VenueAddRequest venueAddRequest);

    /**
     * 修改场地
     * @param venueUpdateRequest 修改场地请求对象
     * @return 是否修改成功
     */
    boolean updateVenue(VenueUpdateRequest venueUpdateRequest);

    /**
     * 根据场地ID删除场地
     * @param venueId 场地ID
     * @return 是否删除成功
     */
    boolean deleteVenue(String venueId, String auditId);

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
}
