package com.yumyumcoach.domain.image.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignRequest {
    private ImagePurpose purpose;   // PROFILE/POST/MEAL
    private String fileName;        // test.png (확장자 추출용)
    private String contentType;     // image/png
}

