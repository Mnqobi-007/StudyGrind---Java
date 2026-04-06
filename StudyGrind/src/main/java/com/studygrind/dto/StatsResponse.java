package com.studygrind.dto;

import java.util.Map;

public class StatsResponse {

    private Map<String, Object> stats;

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }
}
