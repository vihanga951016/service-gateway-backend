package com.flex.common_module.http.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
    private Integer id;
    private int page;
    private int size;
    private Sorting sort;
    private String searchText;
    private String specialSearchOne;
    private String specialSearchTwo;
}
