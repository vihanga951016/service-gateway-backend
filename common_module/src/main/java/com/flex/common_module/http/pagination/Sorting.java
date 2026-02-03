package com.flex.common_module.http.pagination;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sorting {
    @JsonAlias({"column", "property"})
    private String name;
    @JsonAlias({"direction", "order"})
    private String direction;

    public static Sort.Order getSort(Sorting sorting) {
        if (sorting != null && !isNullOrEmpty(sorting.getName()))
            if (!isNullOrEmpty(sorting.getDirection())
                    && sorting.getDirection().toLowerCase().contains("desc")) {
                return new Sort.Order(Sort.Direction.DESC, sorting.getName().trim());
            } else
                return new Sort.Order(Sort.Direction.ASC, sorting.getName().trim());

        return new Sort.Order(Sort.Direction.ASC, "id");
    }

    private static boolean isNullOrEmpty(String arg) {
        return arg == null || arg.isEmpty();
    }
}
