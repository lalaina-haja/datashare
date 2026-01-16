package com.datashare.api.configuration;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * Initializer for loading environment variables from a .env file.
 *
 * <p>This class implements {@link ApplicationContextInitializer} to initialize the Spring
 * application context by loading properties from a .env file located at the project root. The
 * loaded properties are added to the environment with the highest precedence.
 *
 * <p>The .env file is expected to be at {@code ../.env} relative to the application working
 * directory.
 */
public class DotenvInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  /**
   * Initializes the application context by loading properties from the .env file.
   *
   * <p>This method reads the .env file from the path {@code ../.env} and adds its properties as the
   * first (highest priority) property source in the application environment.
   *
   * @param applicationContext the application context to initialize
   * @throws RuntimeException if the .env file cannot be found or loaded
   */
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
