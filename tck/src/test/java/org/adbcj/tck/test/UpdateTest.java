/*
 *   Copyright (c) 2007 Mike Heath.  All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.adbcj.tck.test;

import org.adbcj.Connection;
import org.adbcj.Result;
import org.adbcj.ResultSet;
import org.adbcj.Value;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


@Test(timeOut = 5000)
public class UpdateTest extends AbstractWithConnectionManagerTest{


	public void testSimpleUpdates() throws InterruptedException {
		Connection connection = connectionManager.connect().get();
		assertNotNull(connection);

		// Clear out updates table
		Result result = connection.executeUpdate("DELETE FROM updates").get();
		assertNotNull(result);

		// Insert a row
		result = connection.executeUpdate("INSERT INTO updates (id) VALUES (1)").get();
		assertNotNull(result);
		assertEquals(result.getAffectedRows(), 1L);

		// Select the row
		ResultSet rs = connection.executeQuery("SELECT id FROM updates").get();
		assertNotNull(rs);
		assertEquals(rs.size(), 1);
		Value value = rs.get(0).get(0);
		assertEquals(value.getInt(), 1);
		assertEquals(rs.getFields().get(0).getColumnLabel().toLowerCase(), "id");

		// Update nothing
		result = connection.executeUpdate("UPDATE updates SET id=1 WHERE id=2").get();
		assertNotNull(result);
		assertEquals(result.getAffectedRows(), 0L);
		
		// Update inserted row
		result = connection.executeUpdate("UPDATE updates SET id=2").get();
		assertNotNull(result);
		assertEquals(result.getAffectedRows(), 1L);
		
		// Delete inserted row
		result = connection.executeUpdate("DELETE FROM updates WHERE id=2").get();
		assertNotNull(result);
		assertEquals(result.getAffectedRows(), 1L);
	}
	
}
