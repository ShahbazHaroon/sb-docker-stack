/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.docker.service;

import com.ubaidsample.docker.dto.request.UserUpdateRequestDTO;
import com.ubaidsample.docker.dto.request.UserCreateRequestDTO;
import com.ubaidsample.docker.dto.response.UserResponseDTO;
import com.ubaidsample.docker.entity.User;
import com.ubaidsample.docker.exception.MissingInputException;
import com.ubaidsample.docker.exception.ResourceNotFoundException;
import com.ubaidsample.docker.repository.UserRepository;
import com.ubaidsample.docker.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    @Transactional
    public UserResponseDTO save(UserCreateRequestDTO request) {
        log.info("UserService -> save() called");
        User entity = MapperUtil.map(request, User.class);
        repository.save(entity);
        return MapperUtil.map(entity, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        log.info("UserService -> findAll() called");
        List<User> entity = repository.findAll();
        return MapperUtil.mapAll(entity, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        log.info("UserService -> findById() called");
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        return MapperUtil.map(entity, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserUpdateRequestDTO request) {
        log.info("UserService -> update() called");
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        MapperUtil.map(request, entity);
        return MapperUtil.map(repository.save(entity), UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO partialUpdate(Long id, UserUpdateRequestDTO request) {
        log.info("UserService -> partialUpdate() called");
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        Optional.ofNullable(request.getUserName()).ifPresent(entity::setUserName);
        Optional.ofNullable(request.getEmail()).ifPresent(entity::setEmail);
        Optional.ofNullable(request.getDateOfBirth()).ifPresent(entity::setDateOfBirth);
        Optional.ofNullable(request.getDateOfLeaving()).ifPresent(entity::setDateOfLeaving);
        Optional.ofNullable(request.getPostalCode()).ifPresent(entity::setPostalCode);
        return MapperUtil.map(repository.save(entity), UserResponseDTO.class);
    }

    @Transactional
    public void delete(Long id) {
        log.info("UserService -> delete() called");
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Nothing found in the database with id " + id);
        }
        repository.deleteById(id);
    }

    public void validateFullUpdate(UserUpdateRequestDTO request) {
        log.info("UserService -> validateFullUpdate() called");
        if (request.getUserName() == null
                || request.getEmail() == null
                || request.getDateOfBirth() == null
                || request.getDateOfLeaving() == null
                || request.getPostalCode() == null) {
            throw new MissingInputException(
                    "All fields must be provided for full update");
        }
    }

    public void validatePartialUpdate(UserUpdateRequestDTO request) {
        log.info("UserService -> validatePartialUpdate() called");
        if (request.getUserName() == null
                && request.getEmail() == null
                && request.getDateOfBirth() == null
                && request.getDateOfLeaving() == null
                && request.getPostalCode() == null) {
            throw new MissingInputException(
                    "At least one field must be provided");
        }
    }
}