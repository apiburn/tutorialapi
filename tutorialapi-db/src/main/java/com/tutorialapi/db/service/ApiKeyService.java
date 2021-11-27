package com.tutorialapi.db.service;

import com.tutorialapi.model.user.ApiKey;

import java.util.Optional;

public interface ApiKeyService {
    Optional<ApiKey> get(String key);
    boolean create(ApiKey apikey);
    boolean update(ApiKey apikey);
    Optional<ApiKey> delete(String key);
    int truncate();
}
