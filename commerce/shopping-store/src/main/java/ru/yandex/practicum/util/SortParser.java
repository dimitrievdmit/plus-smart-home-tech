package ru.yandex.practicum.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class SortParser {

    public static final String ASC = "asc";
    public static final String DESC = "desc";

    public static Sort parseSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        String pendingProperty = null;

        for (String token : sort) {
            if (token == null || token.isBlank()) {
                continue;
            }
            String trimmed = token.trim();
            String lower = trimmed.toLowerCase();

            if (ASC.equals(lower) || DESC.equals(lower)) {
                if (pendingProperty != null) {
                    Sort.Direction direction = DESC.equals(lower) ? Sort.Direction.DESC : Sort.Direction.ASC;
                    orders.add(new Sort.Order(direction, pendingProperty));
                    pendingProperty = null;
                }
            } else {
                if (pendingProperty != null) {
                    orders.add(new Sort.Order(Sort.Direction.ASC, pendingProperty));
                }
                pendingProperty = trimmed;
            }
        }
        if (pendingProperty != null) {
            orders.add(new Sort.Order(Sort.Direction.ASC, pendingProperty));
        }

        log.info("Разобранные параметры сортировки: {}", orders);
        return Sort.by(orders);
    }
}