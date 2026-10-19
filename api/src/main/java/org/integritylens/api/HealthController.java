package org.integritylens.api;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final JdbcTemplate jdbc;
    private final ObjectStorage storage;

    public HealthController(JdbcTemplate jdbc, ObjectStorage storage) {
        this.jdbc = jdbc;
        this.storage = storage;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        jdbc.queryForObject("select 1", Integer.class);
        storage.check();
        return Map.of("status", "ok");
    }
}
