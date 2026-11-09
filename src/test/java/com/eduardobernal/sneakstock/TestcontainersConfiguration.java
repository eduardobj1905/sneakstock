package com.eduardobernal.sneakstock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * MySQL de usar y tirar para los tests de integración: un contenedor nuevo y vacío
 * en cada ejecución, con la misma versión que en desarrollo. Flyway crea las tablas al arrancar.
 * {@code @ServiceConnection} hace que Spring se conecte a él en lugar de a la base de datos de desarrollo.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer mysqlContainer() {
        return new MySQLContainer(DockerImageName.parse("mysql:8.4"));
    }
}
