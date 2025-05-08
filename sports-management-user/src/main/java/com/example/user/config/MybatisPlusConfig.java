package com.example.user.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 插件集合
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 1. 初始化核心拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 2. 创建分页插件的内部拦截器实例
        //    !!! 注意：一定要指定正确的 DbType，否则分页不起作用 !!!
        //    根据你的数据库类型选择 DbType 枚举，常见的有：
        //    DbType.MYSQL, DbType.POSTGRE_SQL, DbType.ORACLE, DbType.SQL_SERVER, DbType.SQLITE, DbType.MARIADB ...
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL); // <--- 这里指定数据库类型，以 MySQL 为例

        // -------- 可选配置 START --------
        // paginationInnerInterceptor.setMaxLimit(500L); // 设置单页最大限制数量，-1L 表示不受限制（默认为 -1）
        // paginationInnerInterceptor.setOverflow(false); // 请求页码大于最大页码时是否处理（默认 false，不处理，返回空）
        // paginationInnerInterceptor.setOptimizeJoin(true); // 优化 left join 查询总数（默认为 true，部分复杂 join 可能需要关闭）
        // -------- 可选配置 END ----------

        // 3. 将分页内部拦截器添加到核心拦截器集合中
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // -------- 如果还需要其他插件，可以继续添加 --------
        // 例如：乐观锁插件 OptimisticLockerInnerInterceptor
        // interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        //防止全表更新与删除插件 BlockAttackInnerInterceptor
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // -------------------------------------------------

        // 4. 返回配置好的拦截器 Bean
        return interceptor;
    }
}