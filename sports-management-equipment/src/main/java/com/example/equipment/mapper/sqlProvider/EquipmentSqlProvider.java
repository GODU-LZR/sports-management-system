package com.example.equipment.mapper.sqlProvider;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.equipment.dto.utilDTO.EquipmentPageQuery;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import java.util.Map;

// SQL Provider 类，用于动态构建 SQL
public class EquipmentSqlProvider implements ProviderMethodResolver {

    /**
     * 构建分页查询器材的 SQL
     *
     * @param parameter MyBatis 传递过来的参数 Map，包含 Mapper 方法的所有参数
     * @return 构建好的 SQL 字符串
     */
    public String selectEquipmentVOPage(Map<String, Object> parameter) {
        EquipmentPageQuery query = (EquipmentPageQuery) parameter.get("query");

        return new SQL() {{
            // SELECT 部分：选择需要的字段，注意使用别名 AS 匹配 VO 属性名
            SELECT("e.equipment_id AS equipmentId");
            SELECT("e.category_id AS categoryId");
            SELECT("e.picture_url AS pictureUrl");
            SELECT("e.specification AS specification");
            SELECT("e.status AS status");
            SELECT("e.is_deleted AS isDeleted");
            SELECT("e.create_time AS createTime");
            SELECT("e.modified_time AS modifiedTime");

            // 从 equipment_category 表选择字段 - 取消对 value, total, stock 的注释
            SELECT("ec.name AS name"); // 器材分类名称
            // SELECT("ec.description AS description"); // 如果需要描述字段的话
            SELECT("ec.value AS value"); // 如果分类有 value 字段
            SELECT("ec.total AS total"); // 如果分类有 total 字段
            SELECT("ec.stock AS stock"); // 如果分类有 stock 字段


            // FROM 部分：指定主表并设置别名
            FROM("equipment e");

            // JOIN 部分：连接 category 表
            JOIN("equipment_category ec ON e.category_id = ec.category_id");

            // WHERE 部分：根据查询条件动态添加
            WHERE("e.is_deleted = 0"); // 通常只查询未删除的器材

            // 添加根据 器材分类名称 (equipment_category.name) 的模糊查询条件
            if (query != null && StringUtils.hasText(query.getName())) {
                // 使用 #{query.name} 引用 DTO 中的 name 属性
                WHERE("ec.name LIKE CONCAT('%', #{query.name}, '%')");
            }

            // 添加根据 器材状态 (equipment.status) 的精确查询条件
            if (query != null && query.getStatus() != null) {
                // 使用 #{query.status} 引用 DTO 中的 status 属性
                WHERE("e.status = #{query.status}");
            }


        }}.toString();
    }

    // 如果还有其他自定义查询方法，可以在这里继续添加对应的构建方法
}
