package com.zayenha.qatra.center.domain.port.in;

import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.shared.domain.PageResult;
import com.zayenha.qatra.shared.domain.SearchCriteria;

public interface CenterQueryUseCases {
    DonationCenter getById(Long id);
    PageResult<DonationCenter> getAll(SearchCriteria criteria);
}
