package com.malgn.dto.contents;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ContentRequestDto {

    private String title;
    private String description;

    @Builder
    public ContentRequestDto(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
