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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import org.redisson.api.RLock;

public class LockTester implements Runnable{
	private final boolean _increment;
	private final LockInterface _lockInterface;
	private final ResourceManipulator _resourceManipulator;
	private final Random _randomWork;
	private final int _workPoolSize;
	
    public LockTester(RedisInitializer redisInitializer, boolean increment, ResourceManipulator resourceManipulator, Properties prop) {
    	_increment = increment;
    	_resourceManipulator = resourceManipulator;
    	_lockInterface = new LockInterface(redisInitializer);
    	_randomWork = new Random();
    	_workPoolSize = Integer.parseInt(prop.getProperty("work_pool_size", "10"));
	}

	//@Override
    public void run() {
    	while(true){
    		String myName = Thread.currentThread().getName();
    		System.out.println(String.format("%s:LockTester.run starts",myName));
    		
        	String workKey = Integer.toString(_randomWork.nextInt(_workPoolSize) + 1);
        	RLock rLock = _lockInterface.getLock(myName,workKey);
        	
        	Resource resource = _resourceManipulator.getResource(workKey);
        	_resourceManipulator.doWork(myName);
        	if(true == _increment) {
        		resource.incrementResource();
        	}
        	else {
        		resource.decrementResource();
        	}
        	resource.displayState(myName);
        	_resourceManipulator.updateResource(workKey, resource);
        	
        	_lockInterface.releaseLock(rLock);
    		System.out.println(String.format("%s:LockTester.run ends",myName));
    	}    	
    }
 
    public static void main(String[] args) {
    	System.out.println("Inside main");

    	FileReader reader = null;
    	Properties prop;
    	   	
		try {
			reader = new FileReader("conf/config.prop");
	    	prop = new Properties();  
	    	try {
				prop.load(reader);
				
				RedisInitializer redisInitializer = new RedisInitializer(prop);
		    	ResourceManipulator resourceManipulator = new ResourceManipulator(redisInitializer,prop);
		    	resourceManipulator.loadResources();
		    	
		    	boolean increment = false;
		    	
		    	int threadCount = Integer.parseInt(prop.getProperty("thread_count","40"));
		    	int milliSleep = Integer.parseInt(prop.getProperty("sleep_before_next_spawn_in_millisec","100"));
		    	for (int i=0 ; i < threadCount ; i++) {
		    		if(0 == i%2) {
		    			increment = true;
		    		}
		    		else {
		    			increment = false;
		    		}
		    			
		    		LockTester lockTester = new LockTester(redisInitializer,increment,resourceManipulator,prop);
		            Thread t1 = new Thread(lockTester);
		            t1.start();
		            try {
		            	//sleep for milliseconds before spawning a new thread
						Thread.sleep(milliSleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    	}				
				
			} catch (IOException e1) {
				System.out.println(String.format("LockTester could not read file",e1));
				System.exit(1);
			}  
		} catch (FileNotFoundException e2) {
			System.out.println(String.format("LockTester could not load file",e2));
			System.exit(1);
		}
		finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					System.out.println("LockTester could not close file reader" + e);
				}
		}
    }
}
