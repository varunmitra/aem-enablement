package com.adobe.training.core.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.stream.JsonReader;

@Component(immediate = true)
public class ParseJson {

	Logger logger = LoggerFactory.getLogger(ParseJson.class);

	public String loadJson(String SOURCE_URL) {

		// TODO Auto-generated method stub
		String value = new String();
		try {
			URL url = new URL(SOURCE_URL);
			InputStreamReader in = new InputStreamReader(url.openStream());
			JsonReader reader = new JsonReader(in);
			reader.beginObject();
			if ("quoteResponse".equals(reader.nextName()))
				reader.beginObject();
			if ("result".equals(reader.nextName())) {
				reader.beginArray();
				reader.beginObject();

			}
			while (reader.hasNext()) {
				String name = reader.nextName();
				if ("regularMarketPreviousClose".equals(name)) {
					value = reader.nextString();
					break;
				} else
					reader.skipValue();
			}
			reader.close();
		} catch (MalformedURLException e) {
			logger.error("Malformed URL Exception " + e);
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IO Exception " + e);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Exception " + e);
		}
		
		return value;

	}

}
