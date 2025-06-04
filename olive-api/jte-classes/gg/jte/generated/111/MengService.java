package com.olive

@Service
public class TestService {
    private final JSqlClient sqlClient;

    public TestService(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }
}