package com.zayenha.qatra._shared.web;

import com.zayenha.qatra._shared.domain.PageResult;

public class PageHelper {

    public static int toPageIndex(int clientPage) {
        return clientPage > 0 ? clientPage - 1 : 0;
    }

    public static Paginated fromDomain(PageResult<?> result) {
        return new Paginated(result.page() + 1, result.size(),
            result.totalElements(), result.totalPages());
    }
}
