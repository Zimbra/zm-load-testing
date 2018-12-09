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

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public class LockInterface {
	private final RedissonClient _redisson;
	
	public LockInterface(RedisInitializer redisInitializer)
	{
		_redisson = redisInitializer.getRedissonClient();
	}
	
	public RLock getLock(String masterId, String lockId)
	{
		String lockKey = lockId + ":LOCK";
		System.out.println(String.format("    %s: LockInterface before calling for lock %s ",masterId,lockKey));
		RLock fairLock = _redisson.getFairLock(lockKey);
		fairLock.lock();
		System.out.println(String.format("    %s: LockInterface after calling for lock %s ",masterId,lockKey));
		return fairLock;
	}
	
	public void releaseLock(RLock fairLock)
	{
		fairLock.unlock();
	}
}
