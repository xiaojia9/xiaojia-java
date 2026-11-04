package com.tlias.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 图形报表通用键值对：{name, value}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameValueVO implements Serializable {

    private String name;
    private Long value;
}
