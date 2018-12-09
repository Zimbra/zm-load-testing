/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2018 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.locktester;

import java.util.Properties;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

public class RedisInitializer 
{
    private RedissonClient _client;
    public RedisInitializer(Properties prop) {    	
        try {
            Config config = new Config();      
        	if(Boolean.parseBoolean(prop.getProperty("single_server_redis","true"))) {
        		System.out.println("RedisInitializer Single server enabled using uri " + prop.getProperty("redis_service_uri","redis://zmc-redismaster1:6379"));
                SingleServerConfig clusterServersConfig = config.useSingleServer();
                clusterServersConfig.setAddress(prop.getProperty("redis_service_uri","redis://zmc-redismaster1:6379"));
                clusterServersConfig.setSubscriptionConnectionPoolSize(Integer.parseInt(prop.getProperty("redis_subscription_connection_pool_size","100")));
                clusterServersConfig.setSubscriptionConnectionMinimumIdleSize(Integer.parseInt(prop.getProperty("redis_subscription_idle_connection_pool_size","50")));
                clusterServersConfig.setSubscriptionsPerConnection(Integer.parseInt(prop.getProperty("redis_subscriptions_per_connection","5")));
                clusterServersConfig.setRetryInterval(Integer.parseInt(prop.getProperty("redis_retry_interval","3000")));
                clusterServersConfig.setTimeout(Integer.parseInt(prop.getProperty("redis_connection_timeout","10000")));
                clusterServersConfig.setRetryAttempts(Integer.parseInt(prop.getProperty("redis_num_retries","10")));
        	}
        	else {
        		System.out.println("RedisInitializer Cluster server enabled using uri " + prop.getProperty("redis_service_uri","redis://zmc-redismaster1:6379 "
        				+ "redis://zmc-redismaster2:6379 redis://zmc-redismaster3:6379 redis://zmc-redismaster4:6379 redis://zmc-redismaster5:6379 redis://zmc-redismaster6:6379"));
                ClusterServersConfig clusterServersConfig = config.useClusterServers();
                clusterServersConfig.setScanInterval(Integer.parseInt(prop.getProperty("redis_cluster_scan_interval","2000")));
                clusterServersConfig.addNodeAddress(prop.getProperty("redis_service_uri","redis://zmc-redismaster1:6379"
        				+ " redis://zmc-redismaster2:6379 redis://zmc-redismaster3:6379 redis://zmc-redismaster4:6379 redis://zmc-redismaster5:6379"
        				+ " redis://zmc-redismaster6:6379").split(" "));
                clusterServersConfig.setMasterConnectionPoolSize(Integer.parseInt(prop.getProperty("redis_master_connection_pool_size","1")));
                clusterServersConfig.setMasterConnectionMinimumIdleSize(Integer.parseInt(prop.getProperty("redis_master_idle_connection_pool_size","1")));
                clusterServersConfig.setSubscriptionConnectionPoolSize(Integer.parseInt(prop.getProperty("redis_subscription_connection_pool_size","100")));
                clusterServersConfig.setSubscriptionConnectionMinimumIdleSize(Integer.parseInt(prop.getProperty("redis_subscription_idle_connection_pool_size","50")));
                clusterServersConfig.setSubscriptionsPerConnection(Integer.parseInt(prop.getProperty("redis_subscriptions_per_connection","5")));
                clusterServersConfig.setRetryInterval(Integer.parseInt(prop.getProperty("redis_retry_interval","3000")));
                clusterServersConfig.setTimeout(Integer.parseInt(prop.getProperty("redis_connection_timeout","10000")));
                clusterServersConfig.setRetryAttempts(Integer.parseInt(prop.getProperty("redis_num_retries","10")));
        	}
            _client = Redisson.create(config);
        } catch (Exception ex) {
            System.out.println("RedisInitializer Please check the connection between client and server" + ex);
            System.exit(1);
        }
        
    }

    public RedissonClient getRedissonClient() {
        return _client;
    }
}
