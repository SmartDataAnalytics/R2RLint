package org.linkedgeodata.usertags.core;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.aksw.commons.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsmOAuthClient {
	private static final Logger logger = LoggerFactory.getLogger(OsmOAuthClient.class);
	
	private OAuthProvider provider;
	private OAuthConsumer consumer;
	private String callbackUrl;
	
	public OsmOAuthClient(OAuthConsumer consumer, OAuthProvider provider, String callbackUrl) {
		this.provider = provider;
		this.consumer = consumer;
		this.callbackUrl = callbackUrl;
	}
	
	public String initiate()
			throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException
	{
		logger.debug("Fetching request token from server...");

		// we do not support callbacks, thus pass OOB
		String authUrl = provider.retrieveRequestToken(consumer, callbackUrl);

		logger.debug("Request token: " + consumer.getToken());
		logger.debug("Token secret: " + consumer.getTokenSecret());

		return authUrl;
		
//		System.out.println("Now visit:\n" + authUrl
//				+ "\n... and grant this app authorization");
//		System.out
//				.println("Enter the verification code and hit ENTER when you're done");
//
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		String code = br.readLine();

		// Note: The callback received the code - we need to continue from there		
	}

	public void progress(String code)
			throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException
	{
		logger.debug("Fetching access token from server...");

		provider.retrieveAccessToken(consumer, code);

		logger.debug("Access token: " + consumer.getToken());
		logger.debug("Token secret: " + consumer.getTokenSecret());
	}
	
	
	
	public UserId getUserId()
			throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, IOException
	{
		URL url = new URL("http://api06.dev.openstreetmap.org/api/0.6/user/details");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
		consumer.sign(request);

		System.out.println("Sending update request to server...");

		request.connect();

        System.out.println("Response: " + request.getResponseCode() + " "
                + request.getResponseMessage());

        String str = StreamUtils.toString(request.getInputStream());
        System.out.println(str);
        
        return null;
	}	
}
