package com.BloggingApp.BloggingApp.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDTO {
    private  int id;
    private String name;
    private String about;
}
