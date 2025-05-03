package com.olive.api.system;

import com.olive.base.response.R;
import com.olive.service.aop.log.Log;
import com.olive.model.SysNotice;
import com.olive.model.constant.BusinessType;
import com.olive.model.dto.SysNoticeSearch;
import com.olive.model.record.PageQuery;
import com.olive.service.SysNoticeService;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/notice")
@AllArgsConstructor
public class SysNoticeController {
    private final SysNoticeService noticeService;

    /**
     * 获取通知公告列表
     */
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/list")
    public Page<SysNotice> list(SysNoticeSearch search, PageQuery page) {
        return noticeService.page(search, page);
    }

    /**
     * 根据通知公告编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public R getInfo(@PathVariable(name = "noticeId") long noticeId) {
        return R.ok(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public int add(@Validated @RequestBody SysNotice notice) {
        return noticeService.insertNotice(notice);
    }

    /**
     * 修改通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public int edit(@Validated @RequestBody SysNotice notice) {
        return noticeService.updateNotice(notice);
    }

    /**
     * 删除通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public void remove(@PathVariable(name = "noticeIds") Long[] noticeIds) {
        noticeService.deleteNoticeByIds(List.of(noticeIds));
    }
}
