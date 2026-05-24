/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.docker.controller;

import com.ubaidsample.docker.dto.request.UserUpdateRequestDTO;
import com.ubaidsample.docker.dto.request.UserCreateRequestDTO;
import com.ubaidsample.docker.dto.response.UserResponseDTO;
import com.ubaidsample.docker.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponseDTO> save(
            @Valid @RequestBody UserCreateRequestDTO request) {
        log.info("UserController -> save() called");
        var response = service.save(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getUserId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        log.info("UserController -> findAll() called");
        var response = service.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(
            @PathVariable(value = "id") @Positive Long id) {
        log.info("UserController -> findById() called with ID: {}", id);
        var response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable(value = "id") @Positive Long id,
            @Valid @RequestBody UserUpdateRequestDTO request) {
        log.info("UserController -> update() called with ID: {}", id);
        service.validateFullUpdate(request);
        var response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> partialUpdate(
            @PathVariable(value = "id") @Positive Long id,
            @Valid @RequestBody UserUpdateRequestDTO request) {
        log.info("UserController -> partialUpdate() called with ID: {}", id);
        service.validatePartialUpdate(request);
        var response = service.partialUpdate(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable(value = "id") @Positive Long id) {
        log.info("UserController -> delete() called with ID: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}