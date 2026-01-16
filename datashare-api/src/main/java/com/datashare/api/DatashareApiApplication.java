package com.datashare.api;

import com.datashare.api.configuration.DotenvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Datashare API Spring Boot application.
 *
 * <p>Initializes the application with custom environment variable loading from a .env file through
 * the DotenvInitializer.
 */
@SpringBootApplication
public class DatashareApiApplication {

  /**
   * Main method to start the Datashare API application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(DatashareApiApplication.class);
    application.addInitializers(new DotenvInitializer());
    application.run(args);
  }
}
