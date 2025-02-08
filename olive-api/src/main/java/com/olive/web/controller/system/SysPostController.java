package com.olive.web.controller.system;

import com.olive.framework.enums.BusinessType;
import com.olive.framework.log.Log;
import com.olive.framework.record.PageQuery;
import com.olive.framework.web.system.SysPost;
import com.olive.system.service.SysPostService;
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
    public com.olive.framework.web.system.SysPost getInfo(@PathVariable("postId") long postId) {
        return sysPostService.getInfo(postId);
    }

    /**
     * 新增岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:add')")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public boolean add(@Validated @RequestBody com.olive.framework.web.system.SysPost post) {
        return sysPostService.insertPost(post);
    }

    /**
     * 修改岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:edit')")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public boolean edit(@Validated @RequestBody SysPost post) {
        return sysPostService.updatePost(post);
    }

    /**
     * 删除岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:remove')")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public boolean remove(@PathVariable("postIds") Long[] postIds) {
        return sysPostService.delete(List.of(postIds));
    }

    /**
     * 获取岗位选择框列表
     */
    @GetMapping("/optionselect")
    public List<com.olive.framework.web.system.SysPost> optionselect() {
        return sysPostService.list();
        // List<SysPost> posts = postService.selectPostAll();
        // return success(posts);
    }
}
