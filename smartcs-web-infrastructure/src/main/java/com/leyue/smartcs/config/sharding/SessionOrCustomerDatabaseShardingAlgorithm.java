package com.leyue.smartcs.config.sharding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

/**
 * Composite database sharding algorithm for chat session domain.
 *
 * Routing priority: session_id → customer_id → broadcast (all targets).
 *
 * Props:
 * - databaseShards: number of databases (default 2)
 * - dsPrefix: data source prefix (default "ds_")
 */
public class SessionOrCustomerDatabaseShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    private int databaseShards = 2;
    private String dsPrefix = "ds_";

    @Override
    public void init(Properties props) {
        if (props != null) {
            Object shards = props.get("databaseShards");
            if (shards != null) {
                this.databaseShards = Integer.parseInt(String.valueOf(shards));
            }
            Object prefix = props.get("dsPrefix");
            if (prefix != null) {
                this.dsPrefix = String.valueOf(prefix);
            }
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         ComplexKeysShardingValue<Long> shardingValue) {
        Map<String, Collection<Long>> valuesMap;
        //noinspection unchecked
        valuesMap = (Map) shardingValue.getColumnNameAndShardingValuesMap();

        Collection<Long> sessionIds = valuesMap.get("session_id");
        Collection<Long> customerIds = valuesMap.get("customer_id");

        Set<String> result = new HashSet<>();

        if (sessionIds != null && !sessionIds.isEmpty()) {
            for (Long sid : sessionIds) {
                if (sid == null) continue;
                int idx = (int) Math.floorMod(sid, databaseShards);
                String target = dsPrefix + idx;
                if (availableTargetNames.contains(target)) {
                    result.add(target);
                }
            }
        } else if (customerIds != null && !customerIds.isEmpty()) {
            for (Long cid : customerIds) {
                if (cid == null) continue;
                int idx = (int) Math.floorMod(cid, databaseShards);
                String target = dsPrefix + idx;
                if (availableTargetNames.contains(target)) {
                    result.add(target);
                }
            }
        }

        if (result.isEmpty()) {
            // Fallback: broadcast to all data sources when no key present.
            return new ArrayList<>(availableTargetNames);
        }
        return result;
    }

    @Override
    public String getType() {
        // Not used for CLASS_BASED strategy, but required by interface.
        return "SESSION_OR_CUSTOMER_DB";
    }

    @Override
    public Properties getProps() {
        Properties props = new Properties();
        props.put("databaseShards", databaseShards);
        props.put("dsPrefix", dsPrefix);
        return props;
    }
}

