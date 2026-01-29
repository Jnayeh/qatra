package com.zayenha.qatra.shared.web;

import com.zayenha.qatra.shared.domain.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageHelperTest {

    @Test
    void toZeroIndexedConvertsClientPageToZeroIndexed() {
        assertThat(PageHelper.toZeroIndexed(1)).isZero();
        assertThat(PageHelper.toZeroIndexed(2)).isEqualTo(1);
        assertThat(PageHelper.toZeroIndexed(0)).isEqualTo(4);
    }

    @Test
    void toZeroIndexedTreatsNonPositiveAsZero() {
        assertThat(PageHelper.toZeroIndexed(0)).isZero();
        assertThat(PageHelper.toZeroIndexed(-1)).isZero();
    }

    @Test
    void fromDomainConvertsPageResultToPaginated() {
        var result = new PageResult<String>(List.of("a", "b"), 0, 10, 25, 3);
        var paginated = PageHelper.fromDomain(result);

        assertThat(paginated.number()).isEqualTo(1);
        assertThat(paginated.size()).isEqualTo(10);
        assertThat(paginated.totalElements()).isEqualTo(25);
        assertThat(paginated.totalPages()).isEqualTo(3);
    }

    @Test
    void fromDomainHandlesEmptyResult() {
        var result = new PageResult<>(List.of(), 0, 20, 0, 0);
        var paginated = PageHelper.fromDomain(result);

        assertThat(paginated.number()).isEqualTo(1);
        assertThat(paginated.totalElements()).isZero();
        assertThat(paginated.totalPages()).isZero();
    }
}
