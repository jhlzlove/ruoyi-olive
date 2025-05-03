package com.olive.service.interceptor;

import com.olive.base.utils.LocalDateUtil;
import com.olive.model.BaseEntity;
import com.olive.model.BaseEntityDraft;
import com.olive.model.BaseEntityProps;
import com.olive.model.LoginUser;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 审计功能
 * @author jhlz
 * @version x.x.x
 */
@Component
public class BaseEntityInterceptor implements DraftInterceptor<BaseEntity, BaseEntityDraft> {
    @Override
    public void beforeSave(@NotNull BaseEntityDraft draft, @Nullable BaseEntity entity) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.UPDATE_TIME)) {
            draft.setUpdateTime(LocalDateUtil.dateTime());
        }
        if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.UPDATE_BY)) {
            draft.setUpdateBy(loginUser.getUsername());
        }
        if (entity == null) {
            if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.CREATE_TIME)) {
                draft.setCreateTime(LocalDateUtil.dateTime());
            }
            if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.CREATE_BY)) {
                draft.setCreateBy(loginUser.getUsername());
            }
        }
    }
}
