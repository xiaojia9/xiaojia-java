package com.tlias.controller;

import com.tlias.common.Result;
import com.tlias.dto.LoginDTO;
import com.tlias.service.EmpService;
import com.tlias.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录
 */
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final EmpService empService;

    /**
     * 员工登录：POST /login
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(empService.login(loginDTO));
    }
}
