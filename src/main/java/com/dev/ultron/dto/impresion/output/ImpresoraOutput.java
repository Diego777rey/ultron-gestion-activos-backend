package com.dev.ultron.dto.impresion.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpresoraOutput implements Serializable {
    private String name;
    private String displayName;
    private Boolean isDefault;
}
