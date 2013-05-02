package org.aksw.sml_eval.web.resources;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.aksw.sml_eval.core.OsmOAuthClient;
import org.aksw.sml_eval.core.OsmUtils;
import org.aksw.sml_eval.core.UserId;
import org.aksw.sml_eval.core.UserIdOsm;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


enum OsmEntityType {
	node,
	way,
	relation
}

interface TxWrapper<T> {
	T tx(Connection conn) throws Exception;
}

@Component
@Path("/foo/0.1")
public class OsmUserTags {

	//@Resource(name="userTags.oAuthConsumer")
	private OAuthConsumer oAuthConsumer;
	
	private OAuthProvider oAuthProvider = OsmUtils.osmProviderDevelop;
	
	//@Resource(name="userTags.oAuthCallbackUrl")
	private String oAuthCallbackUrl;
	
	@Resource(name="osm.userTags.store")
	private UserTagsStore store;
	
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
	@Path("/login/osm/oauth/initiate")
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
		
		oAuthClient.initiate();

		return "{success: true}";
	}
	
	@POST
	@Path("/login/osm/oauth/callback")
	@Produces(MediaType.APPLICATION_JSON)
	public String loginOsmOAuthProgress(@Context HttpServletRequest req, String code)
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
		
		return "{success: true}";
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
