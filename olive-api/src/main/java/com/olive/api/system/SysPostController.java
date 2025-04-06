package com.olive.api.system;

import com.olive.framework.log.Log;
import com.olive.model.SysPost;
import com.olive.model.constant.BusinessType;
import com.olive.model.record.PageQuery;
import com.olive.service.SysPostService;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位信息操作处理
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/system/post")
public class SysPostController {
    private final SysPostService sysPostService;

    /**
     * 获取岗位列表
     */
    @PreAuthorize("@ss.hasPermi('system:post:list')")
    @GetMapping("/list")
    public Page<SysPost> list(PageQuery page) {
        return sysPostService.page(page);
    }

    // @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
    // @PreAuthorize("@ss.hasPermi('system:post:export')")
    // @PostMapping("/export")
    // public void export(HttpServletResponse response, SysPost post) {
    //     List<SysPost> list = postService.selectPostList(post);
    //     ExcelUtil<SysPost> util = new ExcelUtil<SysPost>(SysPost.class);
    //     util.exportExcel(response, list, "岗位数据");
    // }

    /**
     * 根据岗位编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:post:query')")
    @GetMapping(value = "/{postId}")
    public SysPost getInfo(@PathVariable("postId") long postId) {
        return sysPostService.info(postId);
    }

    /**
     * 新增岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:add')")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public boolean add(@Validated @RequestBody SysPost post) {
        return sysPostService.add(post);
    }

    /**
     * 修改岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:edit')")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public boolean edit(@Validated @RequestBody SysPost post) {
        return sysPostService.update(post);
    }

    /**
     * 删除岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:remove')")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public boolean remove(@PathVariable("ids") List<Long> ids) {
        return sysPostService.delete(ids);
    }

    /**
     * 获取岗位选择框列表
     */
    @GetMapping("/optionselect")
    public List<SysPost> optionselect() {
        return sysPostService.list();
        // List<SysPost> posts = postService.selectPostAll();
        // return success(posts);
    }
}
