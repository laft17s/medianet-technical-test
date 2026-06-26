package com.laft.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "movementEntityManagerFactory",
        transactionManagerRef = "movementTransactionManager",
        basePackages = {"com.laft.movement.repository.dao"}
)
public class MovementDbConfig {

    @Bean(name = "movementDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.movement")
    public DataSource movementDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "movementEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean movementEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("movementDataSource") DataSource movementDataSource) {
        return builder
                .dataSource(movementDataSource)
                .packages("com.laft.movement.repository.entity")
                .persistenceUnit("movement")
                .build();
    }

    @Bean(name = "movementTransactionManager")
    public PlatformTransactionManager movementTransactionManager(
            @Qualifier("movementEntityManagerFactory") EntityManagerFactory movementEntityManagerFactory) {
        return new JpaTransactionManager(movementEntityManagerFactory);
    }
}
