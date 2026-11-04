package com.tlias.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.tlias.common.BizException;
import com.tlias.config.OssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务
 * <p>优先使用阿里云 OSS（tlias.oss.enabled=true 且配置了 AK 时），
 * 否则自动回退到本地磁盘存储（通过 /files/** 静态资源映射访问）。</p>
 */
@Slf4j
@Service
public class UploadService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    private final OssProperties ossProperties;

    @Value("${tlias.upload.dir:./upload}")
    private String localDir;

    public UploadService(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    /**
     * 上传文件，返回可直接访问的图片地址
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("上传文件不能超过 10MB");
        }
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("仅支持上传 jpg、jpeg、png、gif、bmp、webp 格式的图片");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        if (useOss()) {
            return uploadToOss(file, fileName);
        }
        return uploadToLocal(file, fileName);
    }

    private boolean useOss() {
        return ossProperties.isEnabled()
                && StringUtils.hasText(ossProperties.getEndpoint())
                && StringUtils.hasText(ossProperties.getAccessKeyId())
                && StringUtils.hasText(ossProperties.getAccessKeySecret())
                && StringUtils.hasText(ossProperties.getBucketName());
    }

    private String uploadToOss(MultipartFile file, String fileName) {
        String dir = ossProperties.getDir() == null ? "" : ossProperties.getDir().trim();
        if (!dir.isEmpty() && !dir.endsWith("/")) {
            dir = dir + "/";
        }
        String objectName = dir + fileName;
        OSS client = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
        try (InputStream in = file.getInputStream()) {
            client.putObject(ossProperties.getBucketName(), objectName, in);
            log.info("文件已上传到阿里云 OSS: {}", objectName);
            return buildOssUrl(objectName);
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            throw new BizException("文件上传失败：读取文件异常");
        } catch (Exception e) {
            log.error("上传文件到 OSS 失败", e);
            throw new BizException("文件上传失败：" + e.getMessage());
        } finally {
            client.shutdown();
        }
    }

    private String buildOssUrl(String objectName) {
        String domain = ossProperties.getDomain();
        if (StringUtils.hasText(domain)) {
            domain = domain.trim();
            while (domain.endsWith("/")) {
                domain = domain.substring(0, domain.length() - 1);
            }
            return domain + "/" + objectName;
        }
        String endpoint = ossProperties.getEndpoint().trim();
        if (endpoint.startsWith("https://")) {
            endpoint = endpoint.substring("https://".length());
        } else if (endpoint.startsWith("http://")) {
            endpoint = endpoint.substring("http://".length());
        }
        while (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return "https://" + ossProperties.getBucketName() + "." + endpoint + "/" + objectName;
    }

    private String uploadToLocal(MultipartFile file, String fileName) {
        try {
            Path dir = Paths.get(localDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("文件已保存到本地: {}", dir.resolve(fileName));
            return "/files/" + fileName;
        } catch (IOException e) {
            log.error("保存上传文件到本地失败", e);
            throw new BizException("文件上传失败：本地存储异常");
        }
    }

    private String getExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
