package com.datashare.api;

import com.datashare.api.configuration.DotenvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatashareApiApplication {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(DatashareApiApplication.class);
    application.addInitializers(new DotenvInitializer());
    application.run(args);
  }
}
