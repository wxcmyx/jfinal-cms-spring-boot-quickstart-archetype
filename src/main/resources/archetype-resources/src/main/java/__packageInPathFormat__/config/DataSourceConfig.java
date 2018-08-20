package ${package}.config;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import ${package}.jfinal.model._MappingKit;
import ${package}.jfinal.model._MappingKit2;

import javax.sql.DataSource;

/**
 * HikariCP连接池配置
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.ds1")
    public DataSourceProperties firstDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "ds1")
    @Primary
    @ConfigurationProperties("spring.datasource.ds1")
    public DataSource firstDataSource() {
        HikariDataSource ds= (HikariDataSource) firstDataSourceProperties().initializeDataSourceBuilder().build();
        return ds;
    }

    @Bean
    @ConfigurationProperties("spring.datasource.ds2")
    public DataSourceProperties secondDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "ds2")
    @ConfigurationProperties("spring.datasource.ds2")
    public DataSource secondDataSource() {
        HikariDataSource ds= (HikariDataSource) secondDataSourceProperties().initializeDataSourceBuilder().build();
        return ds;
    }
    @Bean
    @DependsOn("ds1")
    public ActiveRecordPlugin activeFirstDatasource() {
        ActiveRecordPlugin plugin = new ActiveRecordPlugin("ds1",firstDataSource());
        _MappingKit.mapping(plugin);
        plugin.start();
        return plugin;
    }
    @Bean
    @DependsOn("ds2")
    public ActiveRecordPlugin activeSecordDatasource() {
        ActiveRecordPlugin plugin = new ActiveRecordPlugin("ds2",secondDataSource());
        _MappingKit2.mapping(plugin);
        plugin.start();
        return plugin;
    }
}
