package org.example.gcsj4.controller;

import org.example.gcsj4.model.entity.Info;
import org.example.gcsj4.service.InfoService;
import org.example.gcsj4.common.PageRequest;
import org.example.gcsj4.common.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Info 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/info")
public class InfoController {

    private final InfoService infoService;

    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }

    /**
     * 新增 Info 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @PostMapping
    public ApiResponse<Boolean> add(@RequestBody Info entity) {
        return ApiResponse.success(infoService.save(entity));
    }

    /**
     * 更新 Info 记录
     * @param entity 实体对象（必须包含主键 ID）
     * @return 是否更新成功
     */
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody Info entity) {
        return ApiResponse.success(infoService.updateById(entity));
    }

    /**
     * 删除指定 ID 的 Info 记录
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Integer id) {
        return ApiResponse.success(infoService.removeById(id));
    }

    /**
     * 根据 ID 获取 Info 详情
     * @param id 主键 ID
     * @return 匹配的实体对象
     */
    @GetMapping("/{id}")
    public ApiResponse<Info> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(infoService.getById(id));
    }

    /**
     * 获取所有 Info 列表（不分页）
     * @return 实体列表
     */
    @GetMapping
    public ApiResponse<List<Info>> list() {
        return ApiResponse.success(infoService.list());
    }

    /**
     * 分页查询 Info 列表
     * 支持关键字模糊搜索与排序
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ApiResponse<Page<Info>> getPage(@RequestBody PageRequest pageRequest) {
        Page<Info> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<Info> wrapper = new QueryWrapper<>();

        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("name", pageRequest.getKeyword()); // 可自定义字段
        }

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().isEmpty()) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(pageRequest.getSortOrder()), pageRequest.getSortBy());
        }

        return ApiResponse.success(infoService.page(page, wrapper));
    }
}
