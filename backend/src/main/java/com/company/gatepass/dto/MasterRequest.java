package com.company.gatepass.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterRequest {
    private String code;
    private String name;
    private String location;
}
