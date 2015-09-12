/*
	This file is part of ADBCJ.

	ADBCJ is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ADBCJ is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ADBCJ.  If not, see <http://www.gnu.org/licenses/>.

	Copyright 2008  Mike Heath
 */
package org.adbcj.mysql.codec.packets;

import org.adbcj.mysql.codec.MysqlException;

public class ErrorResponse extends ServerPacket {

	private final int errorNumber;
	private final String sqlState;
	private final String message;

	public ErrorResponse(int length, int packetNumber, int errorNumber, String sqlState, String message) {
		super(length, packetNumber);
		this.errorNumber = errorNumber;
		this.sqlState = sqlState;
		this.message = message;
	}

	public int getErrorNumber() {
		return errorNumber;
	}

	public String getSqlState() {
		return sqlState;
	}

	public String getMessage() {
		return message;
	}

    public MysqlException toException(){
        return new MysqlException(getSqlState() + " " + getMessage());
    }

}
