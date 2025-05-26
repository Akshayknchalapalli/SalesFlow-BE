package com.salesflow.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {
    /**
     * The base domain for the application (e.g., salesflow.com)
     */
    @NotBlank(message = "Base domain must not be blank")
    private String baseDomain = "salesflow.com";
    
    /**
     * Whether to enable local development mode, which modifies how 
     * tenant subdomains are processed
     */
    private boolean localDevelopmentMode = true;
    
    /**
     * The port to use in local development mode (e.g., 8081)
     */
    private int localPort = 8081;
    
    /**
     * Whether to support tenant resolution via the X-Tenant-ID header
     */
    private boolean enableHeaderResolution = true;
    
    /**
     * The name of the header to use for tenant resolution
     */
    private String tenantHeader = "X-Tenant-ID";
    
    /**
     * The prefix to use for tenant schemas (e.g., tenant_)
     */
    private String schemaPrefix = "tenant_";
    
    /**
     * The default tenant to use when no tenant is specified
     */
    private String defaultTenant = "acmecorp";
    
    public String getBaseDomain() {
        return this.baseDomain;
    }
    
    public boolean isLocalDevelopmentMode() {
        return this.localDevelopmentMode;
    }
    
    public int getLocalPort() {
        return this.localPort;
    }
    
    public boolean isEnableHeaderResolution() {
        return this.enableHeaderResolution;
    }
    
    public String getTenantHeader() {
        return this.tenantHeader;
    }
    
    public String getSchemaPrefix() {
        return this.schemaPrefix;
    }
    
    public String getDefaultTenant() {
        return this.defaultTenant;
    }
}