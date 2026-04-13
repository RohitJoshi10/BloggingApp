package com.BloggingApp.BloggingApp.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MediaDownloadResource {
    private byte[] data;
    private String fileName;
    private String contentType;
}
