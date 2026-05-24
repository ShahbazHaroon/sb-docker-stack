/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.docker.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class SecretsUtil {

    public static String readSecret(String secretPath) {
        try {
            Path path = Path.of(secretPath);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Secret file not found: " + secretPath);
            }
            String value = Files.readString(path).trim();
            if (value.isEmpty()) {
                throw new IllegalArgumentException("Secret file is empty: " + secretPath);
            }
            log.info("Successfully loaded secret from: {}", secretPath);
            return value;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read secret from: " + secretPath, e);
        }
    }
}