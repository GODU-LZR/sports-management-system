package com.example.equipment.service;

import com.example.common.constant.UserConstant;
import com.example.equipment.dto.CategoryDTO;
import com.example.equipment.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    void addCateGory(CategoryDTO categoryDTO, UserConstant currentUser);

    List<CategoryVO> selectAll();

    void updateCategory(CategoryDTO categoryDTO, UserConstant currentUser);
}
