package com.zipup.server.funding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class LetterData {
    private String id;
    private String content;

}
