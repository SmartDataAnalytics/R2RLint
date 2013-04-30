package org.aksw.sml_eval.adaptors;

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.aksw.commons.util.StreamUtils;

public class StringReader
	implements Callable<String>
{
	private InputStream in;
	
	public StringReader(InputStream in) {
		this.in = in;
	}
	
	@Override
	public String call()
			throws Exception
	{
		String result = StreamUtils.toString(in);
		return result;
	}	
}