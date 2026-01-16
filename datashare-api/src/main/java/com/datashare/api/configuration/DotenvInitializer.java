package com.datashare.api.configuration;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

public class DotenvInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    ConfigurableEnvironment environment = applicationContext.getEnvironment();
    String dotenvPath = Paths.get("..", ".env").toAbsolutePath().toString(); // path to .env
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(dotenvPath)) {
      properties.load(fis);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load .env file from path: " + dotenvPath, e);
    }
    environment.getPropertySources().addFirst(new PropertiesPropertySource("dotenv", properties));
  }
}
