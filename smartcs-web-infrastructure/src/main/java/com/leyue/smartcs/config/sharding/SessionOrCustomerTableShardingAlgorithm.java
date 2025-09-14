package com.leyue.smartcs.config.sharding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

/**
 * Composite table sharding algorithm.
 *
 * Works for logic tables like t_cs_session and t_cs_message.
 * Uses suffix match by computed index, e.g. selects ..._0 when index = 0.
 *
 * Props:
 * - tableShards: number of table shards per database (default 4)
 */
public class SessionOrCustomerTableShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    private int tableShards = 4;

    @Override
    public void init(Properties props) {
        if (props != null) {
            Object shards = props.get("tableShards");
            if (shards != null) {
                this.tableShards = Integer.parseInt(String.valueOf(shards));
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
                int idx = (int) Math.floorMod(sid, tableShards);
                selectByIndexSuffix(availableTargetNames, result, idx);
            }
        } else if (customerIds != null && !customerIds.isEmpty()) {
            for (Long cid : customerIds) {
                if (cid == null) continue;
                int idx = (int) Math.floorMod(cid, tableShards);
                selectByIndexSuffix(availableTargetNames, result, idx);
            }
        }

        if (result.isEmpty()) {
            // Fallback: no key, return all (broadcast)
            return new ArrayList<>(availableTargetNames);
        }

        return result;
    }

    private static void selectByIndexSuffix(Collection<String> available,
                                            Set<String> result,
                                            int index) {
        String suffix = "_" + index;
        for (String each : available) {
            if (each != null && each.endsWith(suffix)) {
                result.add(each);
            }
        }
    }

    @Override
    public String getType() {
        return "SESSION_OR_CUSTOMER_TBL";
    }

    @Override
    public Properties getProps() {
        Properties props = new Properties();
        props.put("tableShards", tableShards);
        return props;
    }
}

