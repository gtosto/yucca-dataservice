package org.csi.yucca.dataservice.ingest.binary;

/*
 * Copyright 1994-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.fs.FileSystem;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * A <code>SequenceInputStream</code> represents the logical concatenation of
 * other input streams. It starts out with an ordered collection of input
 * streams and reads from the first one until end of file is reached, whereupon
 * it reads from the second one, and so on, until end of file is reached on the
 * last of the contained input streams.
 * 
 * @author Author van Hoff
 * @since JDK1.0
 */
public class SequenceHDFSReader extends Reader {
	Enumeration<? extends org.apache.hadoop.fs.Path> path;
	FileSystem fs;
	Reader in;
	StringReader buf;
	CSVReader csvIn;
	/**
	 * Initializes a newly created <code>SequenceInputStream</code> by
	 * remembering the argument, which must be an <code>Enumeration</code> that
	 * produces objects whose run-time type is <code>InputStream</code>. The
	 * input streams that are produced by the enumeration will be read, in
	 * order, to provide the bytes to be read from this
	 * <code>SequenceInputStream</code>. After each input stream from the
	 * enumeration is exhausted, it is closed by calling its <code>close</code>
	 * method.
	 * 
	 * @param e
	 *            an enumeration of input streams.
	 * @see java.util.Enumeration
	 */
	public SequenceHDFSReader(FileSystem fs,
			Enumeration<? extends org.apache.hadoop.fs.Path> p) {
		this.path = p;
		this.fs = fs;
		try {
			firstPath();
		} catch (IOException ex) {
			// This should never happen
			throw new Error("panic");
		}
	}

	/**
	 * Continues reading in the next stream if an EOF is reached.
	 */
	final void firstPath() throws IOException {
        if (csvIn != null) {
        	csvIn.close();
        }

        if (path.hasMoreElements()) {
        	org.apache.hadoop.fs.Path p = (org.apache.hadoop.fs.Path) path.nextElement();
            if (p == null)
                throw new NullPointerException();
            
            csvIn = new CSVReader(new InputStreamReader(fs.open(p)),',','"');
            nextLine();
        }
        else csvIn = null;

    }

	/**
	 * Continues reading in the next stream if an EOF is reached.
	 */
	final void nextPath() throws IOException {
		if (csvIn != null) {
			csvIn.close();
		}

		if (path.hasMoreElements()) {
			org.apache.hadoop.fs.Path p = (org.apache.hadoop.fs.Path) path
					.nextElement();
			if (p == null)
				throw new NullPointerException();

			csvIn = new CSVReader(new InputStreamReader(fs.open(p)), ',', '"', 1);
			nextLine();

			
			
		} else
			csvIn = null;

	}
	
	/**
	 * Continues reading in the next line if an EOF is reached.
	 */
	final void nextLine() throws IOException {
		if (csvIn!=null)
		{
			String[] fields = csvIn.readNext();
			if (fields==null) {
				nextPath();
			}
			else {
				StringWriter sw = new StringWriter();
				CSVWriter csvw =new CSVWriter(sw,';',CSVWriter.DEFAULT_QUOTE_CHARACTER,"\n" );
				csvw.writeNext(fields);
				csvw.flush();
				buf = new StringReader(sw.toString());
				csvw.close();
			}
		}
		else
		{
			buf = null;
		}

	}

	public void close() throws IOException {
		do {
			nextPath();
		} while (in != null);
	}

	@Override
	public int read(char[] c, int off, int len) throws IOException {
		if (buf == null) {
			return -1;
		} else if (c == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > c.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		int n = buf.read(c, off, len);
		if (n <= 0) {
			nextLine();
			return read(c, off, len);
		}
		return n;
	}
}
