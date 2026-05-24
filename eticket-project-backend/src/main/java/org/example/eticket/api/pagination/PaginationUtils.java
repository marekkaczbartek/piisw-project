package org.example.eticket.api.pagination;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;

@UtilityClass
public class PaginationUtils {
    
    public <T, R> Page<R> paginateAndMap(List<T> list, Pageable pageable, Function<T, R> mapper) {
        int start = (int) pageable.getOffset();

        if (start >= list.size()) {
            return Page.empty(pageable);
        }

        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<R> content = list.subList(start, end).stream()
                .map(mapper)
                .toList();

        return new PageImpl<>(content, pageable, list.size());
    }
}
