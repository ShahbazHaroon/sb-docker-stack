/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.docker.config;

import com.ubaidsample.docker.util.SecretsUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${SPRING_DATASOURCE_PASSWORD_FILE:}") String passwordFile,
            @Value("${spring.datasource.password:}") String passwordDirect) {
        String password = (passwordFile != null && !passwordFile.isBlank())
                ? SecretsUtil.readSecret(passwordFile)
                : passwordDirect;
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        //ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return ds;
    }
}