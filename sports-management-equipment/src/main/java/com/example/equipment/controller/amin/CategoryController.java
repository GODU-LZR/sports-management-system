package com.example.equipment.controller.amin;


import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.CategoryDTO;
import com.example.equipment.service.CategoryService;
import com.example.equipment.service.impl.CategoryServiceImpl;
import com.example.equipment.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@Slf4j
public class CategoryController {


    @Autowired
    private CategoryServiceImpl categoryService;

    @PostMapping("/addCategory")
    public Result addCategory(@RequestBody CategoryDTO categoryDTO, @Parameter(hidden = true) UserConstant currentUser) {

        log.info("前端拿到的器材分类为:{}", categoryDTO);

        categoryService.addCateGory(categoryDTO, currentUser);

        return Result.success();
    }

    /**
     * 查询所有的器材分类
     * @return
     */
    @GetMapping("/selectAll")
    public Result<List<CategoryVO>> selectAll()
    {
         List<CategoryVO> list = categoryService.selectAll();
         log.info("查询到的器材分类为:{}",list);
        return Result.success(list);
    }

    @PutMapping("/updateCategory")
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO,@Parameter(hidden = true) UserConstant currentUser){

        log.info("Controller层拿到的更新器材分类的信息为：{}",categoryDTO);

        categoryService.updateCategory(categoryDTO,currentUser);

        return Result.success();
    }

}
