package org.linkedgeodata.usertags.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.JAXBException;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import org.aksw.commons.util.StreamUtils;
import org.linkedgeodata.usertags.xml.Osm;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;



public class Playground {

	public static Osm testXml() throws UnsupportedEncodingException, JAXBException, IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource res = resolver.getResource("/osmUserDetailsTest.xml");

		Osm osm = OsmUtils.unmarshallXml(Osm.class, res.getInputStream());
		return osm;
	}
	
	public static void main(String[] args) throws Exception {

		Osm osm = testXml();
		System.out.println(osm.getUser().getDisplayName());
		
	}
	
	
	public static void mainO(String[] args) throws Exception {

		// Production
//		OAuthConsumer consumer = new DefaultOAuthConsumer("dwSU4C5KCpkpdnH5mFfLUPc1r5SVd0kshSR71LeA",
//				"X7S78LKLfowMEbfD1yelesrVHB62EkFJy7FKtmOx");

		// Testing
		OAuthConsumer consumer = new DefaultOAuthConsumer("UNESf7cJrwH1o8f3Yx3LML3U27DHLu91YXf5gJMj",
				"AZEpmpA8KK63xdp7p4c38kvaKKGmH088pEYWSW6g");

//		OAuthProvider provider = new DefaultOAuthProvider(
//				"http://www.openstreetmap.org/oauth/request_token",
//				"http://www.openstreetmap.org/oauth/access_token",
//				"http://www.openstreetmap.org/oauth/authorize");

		OAuthProvider provider = new DefaultOAuthProvider(
		"http://api06.dev.openstreetmap.org/oauth/request_token",
		"http://api06.dev.openstreetmap.org/oauth/access_token",
		"http://api06.dev.openstreetmap.org/oauth/authorize");

		
		// OAuthProvider provider = new CommonsHttpOAuthProvider(
		// "https://fireeagle.yahooapis.com/oauth/request_token",
		// "https://fireeagle.yahooapis.com/oauth/access_token",
		// "https://fireeagle.yahoo.net/oauth/authorize");

		System.out.println("Fetching request token from server...");

		// we do not support callbacks, thus pass OOB
		String authUrl = provider.retrieveRequestToken(consumer,
				OAuth.OUT_OF_BAND); //"http://example.org");

		System.out.println("Request token: " + consumer.getToken());
		System.out.println("Token secret: " + consumer.getTokenSecret());

		System.out.println("Now visit:\n" + authUrl
				+ "\n... and grant this app authorization");
		System.out
				.println("Enter the verification code and hit ENTER when you're done");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = br.readLine();

		System.out.println("Fetching access token from server...");

		provider.retrieveAccessToken(consumer, code);

		System.out.println("Access token: " + consumer.getToken());
		System.out.println("Token secret: " + consumer.getTokenSecret());

		//HttpRequestBase request = new HttpPost("http://openstreetmap.org/api/0.6/user/details");
//		StringEntity body = new StringEntity("city=hamburg&label="
//				+ URLEncoder.encode("Send via Signpost!", "UTF-8"));
//		body.setContentType("application/x-www-form-urlencoded");
//		request.setEntity(body);

		//URL url = new URL("http://openstreetmap.org/api/0.6/permissions");
		//URL url = new URL("http://openstreetmap.org/api/0.6/user/preferences");
		//URL url = new URL("http://api06.dev.openstreetmap.org/api/0.6/user/preferences");
		URL url = new URL("http://api06.dev.openstreetmap.org/api/0.6/user/details");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
		consumer.sign(request);

		System.out.println("Sending update request to server...");

		request.connect();

        System.out.println("Response: " + request.getResponseCode() + " "
                + request.getResponseMessage());

        String str = StreamUtils.toString(request.getInputStream());
        System.out.println(str);
        
//		HttpClient httpClient = new DefaultHttpClient();
//		HttpResponse response = httpClient.execute(request);
//
//		System.out.println("Response: "
//				+ response.getStatusLine().getStatusCode() + " "
//				+ response.getStatusLine().getReasonPhrase());
	}

}
