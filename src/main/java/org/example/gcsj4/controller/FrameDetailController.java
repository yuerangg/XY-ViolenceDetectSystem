package org.example.gcsj4.controller;

import org.example.gcsj4.model.entity.FrameDetail;
import org.example.gcsj4.service.FrameDetailService;
import org.example.gcsj4.common.PageRequest;
import org.example.gcsj4.common.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FrameDetail 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/framedetail")
public class FrameDetailController {

    private final FrameDetailService frameDetailService;

    public FrameDetailController(FrameDetailService frameDetailService) {
        this.frameDetailService = frameDetailService;
    }

    /**
     * 新增 FrameDetail 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @PostMapping
    public ApiResponse<Boolean> add(@RequestBody FrameDetail entity) {
        return ApiResponse.success(frameDetailService.save(entity));
    }

    /**
     * 更新 FrameDetail 记录
     * @param entity 实体对象（必须包含主键 ID）
     * @return 是否更新成功
     */
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody FrameDetail entity) {
        return ApiResponse.success(frameDetailService.updateById(entity));
    }

    /**
     * 删除指定 ID 的 FrameDetail 记录
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Integer id) {
        return ApiResponse.success(frameDetailService.removeById(id));
    }

    /**
     * 根据 ID 获取 FrameDetail 详情
     * @param id 主键 ID
     * @return 匹配的实体对象
     */
    @GetMapping("/{id}")
    public ApiResponse<FrameDetail> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(frameDetailService.getById(id));
    }

    /**
     * 获取所有 FrameDetail 列表（不分页）
     * @return 实体列表
     */
    @GetMapping
    public ApiResponse<List<FrameDetail>> list() {
        return ApiResponse.success(frameDetailService.list());
    }

    /**
     * 分页查询 FrameDetail 列表
     * 支持关键字模糊搜索与排序
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ApiResponse<Page<FrameDetail>> getPage(@RequestBody PageRequest pageRequest) {
        Page<FrameDetail> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<FrameDetail> wrapper = new QueryWrapper<>();

        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("name", pageRequest.getKeyword()); // 可自定义字段
        }

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().isEmpty()) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(pageRequest.getSortOrder()), pageRequest.getSortBy());
        }

        return ApiResponse.success(frameDetailService.page(page, wrapper));
    }
}
