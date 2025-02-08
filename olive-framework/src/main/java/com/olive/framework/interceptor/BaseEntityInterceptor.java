package com.olive.framework.interceptor;

import com.olive.common.utils.LocalDateUtil;
import com.olive.framework.util.SecurityUtils;
import com.olive.framework.web.system.BaseEntity;
import com.olive.framework.web.system.BaseEntityDraft;
import com.olive.framework.web.system.BaseEntityProps;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.UPDATE_TIME)) {
            draft.setUpdateTime(LocalDateUtil.dateTime());
        }
        if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.UPDATE_BY)) {
            draft.setUpdateBy(SecurityUtils.getUsername());
        }
        if (entity == null) {
            if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.CREATE_TIME)) {
                draft.setCreateTime(LocalDateUtil.dateTime());
            }
            if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.CREATE_BY)) {
                draft.setCreateBy(SecurityUtils.getUsername());
            }
        }
    }
}
