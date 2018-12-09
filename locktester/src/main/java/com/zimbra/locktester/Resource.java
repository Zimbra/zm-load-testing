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

public class Resource {
	private int _id;
	private int _resource1;
	private int _incrementOperationsCounter;
	private int _decrementOperationsCounter;
	
	Resource()
	{}
		
	Resource(int id) {
		_id = id;
		_resource1 = 0;
		_incrementOperationsCounter = 0;
		_decrementOperationsCounter = 0;
	}
	
	public void incrementResource()
	{
		++_resource1;
		++_incrementOperationsCounter;
	}
	
	public void decrementResource()
	{
		--_resource1;
		++_decrementOperationsCounter;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void displayState(String masterId)
	{
		System.out.println(String.format("    %s: Resource Id is: %d ",masterId,_id));
		System.out.println(String.format("    %s: Total increment operations: %d ",masterId,_incrementOperationsCounter));
		System.out.println(String.format("    %s: Total decrement operations: %d",masterId,_decrementOperationsCounter));
		System.out.println(String.format("    %s: Resource values: %d",masterId,_resource1));
		System.out.println(String.format("    %s: Operations differences values: %d",masterId,(_incrementOperationsCounter - _decrementOperationsCounter) ));
	}
}
