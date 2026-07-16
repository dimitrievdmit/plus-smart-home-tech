package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageableObject {
    private long offset;
    private List<SortObject> sort;
    private boolean unpaged;
    private boolean paged;
    private int pageNumber;
    private int pageSize;
}