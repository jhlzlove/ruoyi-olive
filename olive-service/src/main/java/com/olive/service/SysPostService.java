package com.olive.service;

import com.olive.model.SysPost;
import com.olive.model.SysPostTable;
import com.olive.model.SysUserTable;
import com.olive.model.exception.SysPostException;
import com.olive.model.record.PageQuery;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class SysPostService {
    private final JSqlClient sqlClient;

    private final SysPostTable table = SysPostTable.$;

    public Page<SysPost> page(PageQuery page) {
        return sqlClient.createQuery(table)
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public List<SysPost> list() {
        return sqlClient.getEntities().findAll(SysPost.class);
    }

    public SysPost info(long postId) {
        return sqlClient.findById(SysPost.class, postId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean add(SysPost post) {
        if (checkPostNameUnique(post)) {
            throw SysPostException.postNameExist("新增岗位失败，岗位名称已存在", post.postName());
        }
        if (checkPostCodeUnique(post)) {
            throw SysPostException.postCodeExist("新增岗位失败，岗位编码已存在", post.postCode());
        }
        return sqlClient.getEntities().save(post).isModified();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysPost post) {
        if (checkPostNameUnique(post)) {
            throw SysPostException.postNameExist("新增岗位失败，岗位名称已存在", post.postName());
        }
        if (checkPostCodeUnique(post)) {
            throw SysPostException.postCodeExist("新增岗位失败，岗位编码已存在", post.postCode());
        }
        return sqlClient.getEntities().save(post).getTotalAffectedRowCount() > 0;
    }

    public List<Long> selectPostListByUserId(long userId) {
        // select p.post_id
        // from sys_post p
        // left join sys_user_post up on up.post_id = p.post_id
        // left join sys_user u on u.user_id = up.user_id
        // where u.user_id = #{userId}
        return sqlClient.createQuery(table)
                .where(table.userList(u -> u.userId().eq(userId)))
                .select(table.postId())
                .execute();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(List<Long> postIds) {
        SysUserTable userTable = SysUserTable.$;
        postIds.forEach(postId -> {
            SysPost obj = sqlClient.createQuery(userTable)
                    .where(userTable.posts(post -> post.postId().eq(postId)))
                    .select(table)
                    .fetchOneOrNull();
            if (Objects.nonNull(obj)) {
                throw new RuntimeException(String.format("%1$s已分配", obj.postName()));
            }
        });
        return sqlClient.deleteByIds(SysPost.class, postIds).getTotalAffectedRowCount() > 0;
    }

    private boolean checkPostNameUnique(SysPost post) {
        return sqlClient.createQuery(table)
                .where(table.status().eq("0"))
                .where(table.postId().ne(post.postId()))
                .where(table.postName().eq(post.postName()))
                .exists();
    }

    private boolean checkPostCodeUnique(SysPost post) {
        return sqlClient.createQuery(table)
                .where(table.status().eq("0"))
                .where(table.postId().ne(post.postId()))
                .where(table.postCode().eq(post.postCode()))
                .exists();
    }
}
