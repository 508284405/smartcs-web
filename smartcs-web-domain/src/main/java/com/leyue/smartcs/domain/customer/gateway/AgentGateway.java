package com.leyue.smartcs.domain.customer.gateway;

import com.leyue.smartcs.domain.customer.CustomerService;
import java.util.List;
import java.util.Map;

/**
 * 客服Gateway接口
 */
public interface AgentGateway {
    
    /**
     * 从用户中心获取客服基础信息
     * @param serviceIds 客服ID列表
     * @return 客服基础信息列表
     */
    List<CustomerService> getAgentFromUserCenter(List<String> serviceIds);
    
    /**
     * 获取客服统计数据
     * @param serviceIds 客服ID列表
     * @return 客服统计数据Map，key为客服ID
     */
    Map<String, CustomerServiceStatistics> getAgentStatistics(List<String> serviceIds);
    
    /**
     * 获取所有客服ID列表
     * @return 客服ID列表
     */
    List<String> getAllAgentIds();
    
    /**
     * 客服统计数据内部类
     */
    class CustomerServiceStatistics {
        private Integer activeSessions;
        private Integer totalSessions;
        private Long lastActiveTime;
        
        public CustomerServiceStatistics() {}
        
        public CustomerServiceStatistics(Integer activeSessions, Integer totalSessions, Long lastActiveTime) {
            this.activeSessions = activeSessions;
            this.totalSessions = totalSessions;
            this.lastActiveTime = lastActiveTime;
        }
        
        public Integer getActiveSessions() {
            return activeSessions;
        }
        
        public void setActiveSessions(Integer activeSessions) {
            this.activeSessions = activeSessions;
        }
        
        public Integer getTotalSessions() {
            return totalSessions;
        }
        
        public void setTotalSessions(Integer totalSessions) {
            this.totalSessions = totalSessions;
        }
        
        public Long getLastActiveTime() {
            return lastActiveTime;
        }
        
        public void setLastActiveTime(Long lastActiveTime) {
            this.lastActiveTime = lastActiveTime;
        }
    }
} 