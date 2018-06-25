package com.bluenimble.platform.api.impls.readers;

import java.io.IOException;
import java.io.InputStream;

import com.bluenimble.platform.IOUtils;
import com.bluenimble.platform.api.ApiContentTypes;
import com.bluenimble.platform.api.ApiRequestBodyReader;

public class StreamApiRequestBodyReader implements ApiRequestBodyReader {

	private static final long serialVersionUID = -9161966870378744014L;

	@Override
	public Object read (InputStream payload) throws IOException {
		try {
			return IOUtils.toByteArray (payload);
		} catch (Exception ex) {
			throw new IOException (ex.getMessage (), ex);
		}
	}

	@Override
	public String [] mediaTypes () {
		return new String [] { ApiContentTypes.Stream };
	}

}