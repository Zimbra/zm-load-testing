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
import java.util.Random;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

public class ResourceManipulator {
	private final RedissonClient _redisson;
	private final Random _randomWorkDelay;
	private int _upperBound;
	private int _lowerBound;
	private int _workPoolSize;
	
	ResourceManipulator(RedisInitializer redisInitializer, Properties prop) {
		_redisson = redisInitializer.getRedissonClient();
		_randomWorkDelay = new Random();
		_upperBound = Integer.parseInt(prop.getProperty("work_upper_bound_delay","1000"));
		_lowerBound = Integer.parseInt(prop.getProperty("work_lower_bound_delay","100"));
		_workPoolSize = Integer.parseInt(prop.getProperty("work_pool_size","10"));
	}
	
	public void updateResource(String key, Resource resource)
	{
		RBucket<Resource> bucket = _redisson.getBucket(key);
		bucket.set(resource);
	}
	
	public Resource getResource(String key)
	{
		RBucket<Resource> bucket = _redisson.getBucket(key);
		Resource resource = bucket.get();
		return resource;
	}
	
	public void doWork(String masterId)
	{
		int randomNumber = _randomWorkDelay.nextInt(_upperBound - _lowerBound) + _lowerBound;
		try
		{
		    System.out.println(String.format("    %s: Thread sleep for %fs",masterId,randomNumber/(float)1000));
			Thread.sleep(randomNumber);
		}
		catch(InterruptedException ex)
		{
		    Thread.currentThread().interrupt();
		}
	}
	
	public void loadResources()
	{
		for(int i=0 ; i < _workPoolSize ; i++ ) {
	    	Resource resource = new Resource(i+1);
	    	updateResource(Integer.toString(resource.getId()), resource);
		}
	}
}
