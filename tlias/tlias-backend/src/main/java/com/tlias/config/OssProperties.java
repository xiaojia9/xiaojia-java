package com.tlias.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置
 * <p>全部配置项均可在 application.yml 中通过 {@code tlias.oss.*} 修改，无需改动代码。</p>
 */
@Data
@ConfigurationProperties(prefix = "tlias.oss")
public class OssProperties {

    /** 是否启用 OSS；为 false 或未配置 AK 时自动回退到本地磁盘存储 */
    private boolean enabled = false;

    /** 地域节点，例如 https://oss-cn-hangzhou.aliyuncs.com */
    private String endpoint;

    /** AccessKey ID */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;

    /** Bucket 名称 */
    private String bucketName;

    /** 上传目录前缀，例如 tlias/ */
    private String dir = "tlias/";

    /** 访问域名（CDN 或 bucket 外网域名），例如 https://tlias-demo.oss-cn-hangzhou.aliyuncs.com；为空时自动拼接 */
    private String domain;
}
