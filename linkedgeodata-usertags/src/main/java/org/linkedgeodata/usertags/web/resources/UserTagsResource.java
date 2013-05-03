package org.linkedgeodata.usertags.web.resources;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.linkedgeodata.usertags.core.OsmEntityType;
import org.linkedgeodata.usertags.core.OsmOAuthClient;
import org.linkedgeodata.usertags.core.OsmUtils;
import org.linkedgeodata.usertags.core.Tag;
import org.linkedgeodata.usertags.core.TxWrapper;
import org.linkedgeodata.usertags.core.UserId;
import org.linkedgeodata.usertags.core.UserIdOsm;
import org.linkedgeodata.usertags.core.UserTagsStore;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


@Component
@Path("/api/0.1")
public class UserTagsResource {

	//@Resource(name="userTags.oAuthConsumer")
	private OAuthConsumer oAuthConsumer = new DefaultOAuthConsumer("UNESf7cJrwH1o8f3Yx3LML3U27DHLu91YXf5gJMj",
			"AZEpmpA8KK63xdp7p4c38kvaKKGmH088pEYWSW6g");
	
	private OAuthProvider oAuthProvider = OsmUtils.osmProviderDevelop;
	
	//@Resource(name="userTags.oAuthCallbackUrl")
	private String oAuthCallbackUrl;
	
	@Resource(name="osm.userTags.store")
	private UserTagsStore store;
	
	@Resource(name="osm.userTags.web.baseUrl")
	private String baseUrl;
	
	
	@PostConstruct
	private void init() {
		this.oAuthCallbackUrl = baseUrl + "/oauth/callback";
	}
	
	public static UserId requireUserId(@Context HttpServletRequest req) {
		if(true) { return new UserIdOsm(1); }
		
		HttpSession session = req.getSession();
		
		UserId result = requireUserId(session);
		
		return result;
	}
	
	public static UserId requireUserId(HttpSession session) {
		UserId userId = (UserId) session.getAttribute("userId");
		
		if(userId == null) {
			throw new RuntimeException("Not logged in");
		}
		
		return userId;
	}
	
	
	@POST
	@Path("/login/osm/oauth")
	@Produces(MediaType.APPLICATION_JSON)
	public String loginOsmOAuthInitiate(@Context HttpServletRequest req)
			throws SQLException, OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException
	{
		HttpSession session = req.getSession();
		
		UserId userId = (UserId)session.getAttribute("userId");
		if(userId != null) {
			throw new RuntimeException("Already logged in");
		}
		
		OsmOAuthClient oAuthClient = new OsmOAuthClient(
				oAuthConsumer,
				oAuthProvider,
				oAuthCallbackUrl);

		session.setAttribute("oAuthClient", oAuthClient);
		
		String authUrl = oAuthClient.initiate();

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", true);
		response.put("authUrl", authUrl);
		
		return toJsonString(response);
	}
	
	public static String toJsonString(Object o) {
		Gson gson = new Gson();
		String result = gson.toJson(o);
		return result;
	}

	
	
	@POST
	@Path("/login/oauth/callback")
	@Produces(MediaType.APPLICATION_JSON)
	public String loginOsmOAuthProgress(@Context HttpServletRequest req, @FormParam("oauth_verifier") String code, @FormParam("oauth_token") String token)
			throws SQLException, OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException, IOException
	{
		HttpSession session = req.getSession();

		OsmOAuthClient oAuthClient = (OsmOAuthClient)session.getAttribute("oAuthClient");
		
		if(oAuthClient == null) {
			throw new RuntimeException("Cannot progress on non-initated oauth login sequence");
		}
		
		oAuthClient.progress(code);
		
		UserId userId = oAuthClient.getUserId();

		session.setAttribute("userId", userId);
		
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", true);
		response.put("userId", userId);
		
		return toJsonString(response);		
	}
	
	@GET
	@Path("/test/{type}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String testEx(@Context HttpServletRequest req, @PathParam("type") String entitiyType, @PathParam("id") final Long osmEntityId)
	{
		return "{success: true, cool: true}";
	}

	@GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public String test(@Context HttpServletRequest req, @PathParam("type") String entitiyType, @PathParam("id") final Long osmEntityId)
	{
		return "{success: true}";
	}

	
	@POST
	@Path("/store/{type}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String store(@Context HttpServletRequest req, @PathParam("type") String entitiyType, @PathParam("id") final Long osmEntityId, @FormParam("tags") String json)
			throws SQLException
	{
		final UserId userId = requireUserId(req);
		
		Type tagListType = new TypeToken<List<Tag>>(){}.getType();
		
		Gson gson = new Gson();
		final List<Tag> tags = gson.fromJson(json, tagListType);
		
		final OsmEntityType osmEntityType = OsmEntityType.valueOf(entitiyType);
		
		//final Store store
		
		store.wrap(new TxWrapper<Void>() {
			@Override
			public Void tx(Connection conn) throws SQLException {
				UserTagsStore.updateTags(conn, userId, osmEntityType, osmEntityId, tags);
				return null;
			}
		});
		
		return "{}";
	}
}
