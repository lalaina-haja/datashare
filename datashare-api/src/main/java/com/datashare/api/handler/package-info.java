/**
 * Exception handling and error management package.
 *
 * <p>This package contains custom exceptions, error details DTOs, and global exception handlers for
 * the application. It provides centralized error handling and logging.
 *
 * <p>Key components:
 *
 * <ul>
 *   <li>{@link com.datashare.api.handler.RestExceptionHandler} - Global exception handler for REST
 *       endpoints
 *   <li>{@link com.datashare.api.exception.ResourceNotFoundException} - Custom exception for
 *       missing resources
 *   <li>{@link com.datashare.api.handler.ErrorDetails} - Error response data transfer object
 *   <li>{@link com.datashare.api.handler.ActiveProfileLogger} - Spring profile logger component
 * </ul>
 */
package com.datashare.api.handler;
