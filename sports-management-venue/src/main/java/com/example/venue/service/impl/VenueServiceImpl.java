package com.example.venue.service.impl;

import com.example.venue.dto.VenueAddRequest;
import com.example.venue.dto.VenueQueryRequest;
import com.example.venue.dto.VenueUpdateRequest;
import com.example.venue.mapper.VenueMapper;
import com.example.venue.pojo.mysql.Venue;
import com.example.venue.service.VenueService;
import com.example.venue.vo.Result;
import com.example.venue.vo.VenueOption;
import com.example.venue.vo.VenuePage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Venue 服务接口的实现类
 * 实现了 VenueService 接口中定义的方法
 */
@Slf4j
@Service
public class VenueServiceImpl implements VenueService {

    @Autowired
    private VenueMapper venueMapper;

    // 查询场地
    public VenuePage listVenues(VenueQueryRequest venueQueryRequest) {
        VenuePage venuePage = new VenuePage();
        List<Venue> list = venueMapper.listVenues(venueQueryRequest);
        Integer total = venueMapper.listVenuesPage(venueQueryRequest);
        venuePage.setData(list);
        venuePage.setTotal(total);
        return venuePage;
    }

    // 新增场地
    @Transactional
    public boolean addVenue(VenueAddRequest venueAddRequest){
        Integer rowsAffected = venueMapper.addVenue(venueAddRequest);
        // 根据影响的行数判断是否成功，而不是直接返回 mapper 的 boolean
        return rowsAffected > 0;
    }

    // 修改场地
    @Transactional
    public boolean updateVenue(VenueUpdateRequest venueUpdateRequest){
        Integer rowsAffected = venueMapper.updateVenue(venueUpdateRequest);
        return rowsAffected > 0;
    }

    // 删除场地
    @Transactional
    public boolean deleteVenue(String venueId, String auditId){
        Integer rowsAffected = venueMapper.deleteVenue(venueId, auditId);
        return rowsAffected > 0;
    }

    // 根据关键字搜索场地选项列表。
    public List<VenueOption> listVenueOptions(String key){
        return venueMapper.listVenueOptions(key);
    }

    // 根据一组venueId查询场地
    public List<Venue> listVenuesByIds(List<String> venueIds) {
        return venueMapper.listVenuesByIds(venueIds);
    }
}
