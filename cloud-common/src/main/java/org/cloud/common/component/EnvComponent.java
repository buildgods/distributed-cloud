package org.cloud.common.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class EnvComponent {
//    private static Environment environment;
//    @Autowired
//    public void setEnvironment(Environment env) {
//        environment = env;
//    }
//    public static String getDirectoryPath(){
//        return environment.getProperty("kkFile.path");
//    }
private static Properties properties;

    static {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yaml"));
        properties = yaml.getObject();
    }

    public static String getDirectoryPath() {
        return properties.getProperty("kkFile.path");
    }
}
