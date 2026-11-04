package com.tlias.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录成功返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO implements Serializable {

    private Integer id;
    private String username;
    private String name;
    private String token;
}
