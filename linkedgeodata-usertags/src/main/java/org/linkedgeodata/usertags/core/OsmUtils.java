package org.linkedgeodata.usertags.core;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class OsmUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(OsmUtils.class);
	
	public static final OAuthProvider osmProviderDevelop = new DefaultOAuthProvider(
			"http://api06.dev.openstreetmap.org/oauth/request_token",
			"http://api06.dev.openstreetmap.org/oauth/access_token",
			"http://api06.dev.openstreetmap.org/oauth/authorize");

	public static final OAuthProvider osmProviderProduction = new DefaultOAuthProvider(
			"http://www.openstreetmap.org/oauth/request_token",
			"http://www.openstreetmap.org/oauth/access_token",
			"http://www.openstreetmap.org/oauth/authorize");
	
	
	public static void authenticate(OAuthConsumer consumer, OAuthProvider provider, String callbackUrl)
			throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException
	{
	}
	
	public static void cont(String code) {
	}
	
	
	public static <T> T unmarshallXml(Class<T> clazz, InputStream in)
			throws JAXBException, UnsupportedEncodingException {
		
		String className = clazz.getPackage().getName();
		JAXBContext context = JAXBContext.newInstance(className);	
		Unmarshaller unmarshaller = context.createUnmarshaller();

		Reader reader = new IgnoreIllegalCharactersXmlReader(in);
	
		//Reader reader = new InputStreamReader(inputStream,"UTF-8");
		 
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");
		
		Object result = unmarshaller.unmarshal(is); //file);
		return (T)result;
	}

}
