package com.zayenha.qatra.shared.web;

import com.zayenha.qatra.shared.domain.PageResult;

public class PageHelper {

    public static int toZeroIndexed(int clientPage) {
        return clientPage > 0 ? clientPage - 1 : 0;
    }

    public static Paginated fromDomain(PageResult<?> result) {
        return new Paginated(result.page() + 1, result.size(),
            result.totalElements(), result.totalPages());
    }
}
