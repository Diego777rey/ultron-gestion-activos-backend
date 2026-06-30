package com.dev.ultron.generic;

import org.springframework.data.domain.Page;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Respuesta genérica paginada para ser utilizada en cualquier módulo del sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> content;
    private PageInfo pageInfo;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.pageInfo = new PageInfo(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
