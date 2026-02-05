/**
 * Configuration package for Spring application setup.
 *
 * <p>This package contains configuration classes for various Spring components including:
 *
 * <ul>
 *   <li>Security configuration with JWT authentication
 *   <li>JWT encoder and decoder beans
 *   <li>Request logging filter setup
 *   <li>Environment initialization from .env files
 * </ul>
 *
 * <p>Key components:
 *
 * <ul>
 *   <li>{@link com.datashare.api.configuration.SecurityConfig} - Security filter chain
 *       configuration
 *   <li>{@link com.datashare.api.configuration.JwtConfig} - JWT encoder/decoder configuration
 *   <li>{@link com.datashare.api.configuration.DotenvInitializer} - Environment variable
 *       initialization
 *   <li>{@link com.datashare.api.configuration.RequestLoggingFilterConfig} - Request logging setup
 * </ul>
 */
package com.datashare.api.configuration;
