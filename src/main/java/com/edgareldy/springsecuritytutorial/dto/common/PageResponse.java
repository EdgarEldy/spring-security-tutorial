package com.edgareldy.springsecuritytutorial.dto.common;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Generic paginated content DTO used as the {@code data} payload of
 * {@link ApiResponse} on every list endpoint, instead of a plain list.
 * <p>
 * Created by edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-security-tutorial
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
