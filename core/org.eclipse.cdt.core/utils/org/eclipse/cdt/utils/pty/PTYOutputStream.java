package org.eclipse.cdt.utils.pty;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.OutputStream;

public class PTYOutputStream extends OutputStream {
	private int fd;

	/**
	 * Fome a Unix valid file descriptor set a Reader.
	 * @param desc file descriptor.
	 */
	public PTYOutputStream(int fd) {
		this.fd = fd;
	}

	/**
	 * @see OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (
			(off < 0)
				|| (off > b.length)
				|| (len < 0)
				|| ((off + len) > b.length)
				|| ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		byte[] tmpBuf = new byte[len];
		System.arraycopy(b, off, tmpBuf, off, len);
		write0(fd, tmpBuf, len);
	}
	/**
	 * Implementation of read for the InputStream.
	 *
	 * @exception IOException on error.
	 */
	public void write(int b) throws IOException {
		byte[] buf = new byte[1];
		buf[0] = (byte) b;
		write(buf, 0, 1);
	}

	/**
	 * Close the Reader
	 * @exception IOException on error.
	 */
	public void close() throws IOException {
		if (fd == -1)
			return;
		int status = close0(fd);
		if (status == -1)
			throw new IOException("close error"); //$NON-NLS-1$
		fd = -1;
	}

	private native int write0(int fd, byte[] b, int len) throws IOException;
	private native int close0(int fd) throws IOException;

	static {
		System.loadLibrary("pty"); //$NON-NLS-1$
	}

}
