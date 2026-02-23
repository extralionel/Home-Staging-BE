package io.home.staging.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageQuality {
    STANDARD("1K"),
    HD_2K("2K"),
    UHD_4K("4K");

    private String value;
}
