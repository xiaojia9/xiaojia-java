package com.tlias.controller;

import com.tlias.common.Result;
import com.tlias.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传（阿里云 OSS，可在 application.yml 中切换为本地存储）
 */
@RestController
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * POST /upload，表单字段名 file，返回图片访问地址
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(uploadService.upload(file));
    }
}
