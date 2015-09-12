package org.adbcj;

public class DbSessionClosedException extends DbException {

	private static final long serialVersionUID = 1L;

	public DbSessionClosedException() {
		super( "This database connection has been closed");
	}

	public DbSessionClosedException(String message) {
		super(message);
	}

	public DbSessionClosedException(Throwable cause) {
		super( cause);
	}

}
