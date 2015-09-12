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
package org.adbcj.mysql.codec;

import org.adbcj.support.CancellationToken;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public abstract class ClientRequest {
    private final CancellationToken cancelSupport;

    protected ClientRequest(CancellationToken cancelSupport) {
        this.cancelSupport = cancelSupport;
    }


    public abstract int getLength() throws UnsupportedEncodingException;

	/**
	 * The packet number is sent as a byte so only the least significant byte will be used.
	 *
	 * @return
	 */
	public int getPacketNumber() {
		return 0;
	}

    protected abstract boolean hasPayload();

    public abstract void writeToOutputStream(OutputStream out) throws IOException;


    public boolean startWriteOrCancel(){
        return cancelSupport.tryStartOrIsCancel();
    }

    public boolean wasCancelled(){
        return cancelSupport.isCancelled();
    }

    public boolean tryCancel(){
        return cancelSupport.cancel();
    }
}
