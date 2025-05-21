package dev.codingstoic.receiptwise;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestcontainersConfiguration extends PostgreSQLContainer<TestcontainersConfiguration> {
    public static final String DB_CONNECTION_URL = "DB_URL";
    /**
     * Database system user
     */
    public static final String DB_USERNAME = "DB_USERNAME";
    /**
     * Database system user password
     */
    public static final String DB_PASSWORD = "DB_PASSWORD";
    public static final String DB_NAME = "DB_NAME";


    private static final String IMAGE_VERSION = "postgres:15.3-alpine";
    private static TestcontainersConfiguration instance;


    private TestcontainersConfiguration() {
        // Private constructor to prevent instantiation
        super(IMAGE_VERSION);
    }

    public static TestcontainersConfiguration getInstance(String initScript) {
        if(instance == null) {
            instance = new TestcontainersConfiguration();
            instance.withInitScript(initScript);
            instance.start();
        }
        return instance;
    }


    @Override
    public void start() {
        super.start();
        System.setProperty(DB_CONNECTION_URL, instance.getJdbcUrl());
        System.setProperty(DB_USERNAME, instance.getUsername());
        System.setProperty(DB_PASSWORD, instance.getPassword());
        System.setProperty(DB_NAME, instance.getDatabaseName());
    }

}
