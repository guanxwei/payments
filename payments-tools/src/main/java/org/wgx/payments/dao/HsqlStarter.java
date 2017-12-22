package org.wgx.payments.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import lombok.Data;

@Data
public class HsqlStarter {

    private String path;
    private DataSource dataSource;

    public void init() {
        try (InputStream input = this.getClass().getResourceAsStream(path); Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            while (line != null) {
               statement.execute(line);
               line = reader.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
