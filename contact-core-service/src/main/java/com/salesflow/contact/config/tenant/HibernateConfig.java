package com.salesflow.contact.config.tenant;

import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Hibernate configuration for multi-tenancy support.
 * 
 * This configuration sets up Hibernate to use the SCHEMA approach for multi-tenancy,
 * where each tenant's data is stored in a separate schema within the same database.
 * 
 * The new schema structure organizes data in two ways:
 * 1. Service-specific schema (contact) for shared reference data
 * 2. Tenant-specific schemas (tenant_X) with table namespaces (contact_data.*)
 */
@Configuration
public class HibernateConfig {

    @Value("${multitenancy.entity.packages}")
    private String entityPackages;

    @Value("${multitenancy.namespace}")
    private String namespace;

    @Value("${multitenancy.schemas}")
    private String schemas;

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            MultiTenantConnectionProvider multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {

        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(dataSource);
        emfBean.setPackagesToScan(entityPackages.split(","));
        emfBean.setJpaVendorAdapter(jpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.multiTenancy", "SCHEMA");
        properties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        properties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        properties.put(Environment.FORMAT_SQL, true);
        properties.put(Environment.SHOW_SQL, true);
        properties.put(Environment.HBM2DDL_AUTO, "validate");
        properties.put(Environment.DEFAULT_SCHEMA, "contact");
        properties.put(Environment.PHYSICAL_NAMING_STRATEGY, "com.salesflow.contact.config.tenant.NamespacePrefixNamingStrategy");

        emfBean.setJpaPropertyMap(properties);
        return emfBean;
    }
}