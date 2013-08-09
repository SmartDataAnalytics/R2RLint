package org.aksw.sparqlify.qa.metrics;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class HttpUri implements NodeMetric {
	
	private String parentDim;
	private String name;
	private float threshold = 0;
	private MeasureDataSink sink;
	private Pinpointer pinpointer;
	
	
	// TODO: host names up to 255 characters
	
	/*
	 * this pattern was created based on
	 *  - the BNF from http://www.w3.org/Addressing/URL/5_BNF.html
	 *  - the post of Diego Perini from https://gist.github.com/dperini/729294
	 *  - information from http://tools.ietf.org/html/rfc1123#page-13
	 *  - information from http://tools.ietf.org/html/draft-fielding-url-syntax-09#appendix-A 
	 */
	private final String httpUrlPattern = "^" +
			// protocol: http:// or https://
			"(?:(?:https?)://)" +
			// user info, e.g. user@ or user:passwd@
			"(?:\\S+(?::\\S*)?@)?" +
			// host part, e.g. localhost, aksw.org, 127.0.0.1
			"(?:" +
			// IP address based host names, like 193.239.40.138
			
				// exclude host names based on local IP addresses because they
				// cannot be resolved in the WWW
				// 10.x.x.x
				"(?!10(?:\\.\\d{1,3}){3})" +
				// 127.x.x.x
				"(?!127(?:\\.\\d{1,3}){3})" +
				// 169.254.x.x
				"(?!169\\.254(?:\\.\\d{1,3}){2})" +
				// 172.16.0.0/12 (172.16.0.0 to 172.31.255.255)
				"(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\d{1,3}){2})" +
				// 192.168.x.x
				"(?!192\\.168(?:\\.\\d{1,3}){2})" +
				
				// all remaining and valid IP addresses:
					// first octet:
					//  1-99            1xx          2xx up to 223
					"(?:[1-9]\\d?|" + "1\\d\\d|" + "2[01]\\d|22[0-3])" +
					// second and third octet
					//       0-99            1xx          2xx up to 255
					"(?:\\.(?:\\d{1,2}|" + "1\\d\\d|" + "2[0-4]\\d|25[0-4])){2}" +
					// fourth octet
					// omitting network (x.x.x.0) and broadcast (x.x.x.255)
					// addresses
					//       1-99            1xx            2xx up to 254
					"(?:\\.(?:[1-9]\\d?|" + "1\\d\\d|" + "2[0-4]\\d|25[0-4]))" +
			"|" +
			// domain name based host names like aksw.org or
			// mail.informatik.uni-leipzig.de
				
				// TODO: add support for internationalized domain names
				
				// domain name
				// restrictions: only one hyphen *between* two chars; a char can
				// be a letter or digit
				"(?:(?:(?:[a-zA-Z0-9]-?)*(?:[a-zA-Z0-9])+\\.)+)" +
				// TLD identifier
				"(?:[a-z]{2,})" +
			")" +
			// port number
			"(?::\\d{2,5})?" +
			
			// path
			//
			// according to
			// http://tools.ietf.org/html/draft-fielding-url-syntax-09#appendix-A :
			
			// path          = [ "/" ] path_segments
			// path_segments = segment *( "/" segment )
			// segment       = *pchar *( ";" param )
			// param         = *pchar
			// pchar         = unreserved | escaped | ":" | "@" | "&" | "=" | "+"
			// unreserved    = alpha | digit | mark
			// escaped       = "%" hex hex
			// alpha         = lowalpha | upalpha
			// mark          = "$" | "-" | "_" | "." | "!" | "~" |
			//				   "*" | "'" | "(" | ")" | ","

			// opaque URLs not considered here
			"(?:(?:/([a-zA-Z\\d_~',\\Q$-.!*()\\E]|%[a-fA-F\\d]{2})*)*)" +
			
			// query
			//
			// http://tools.ietf.org/html/draft-fielding-url-syntax-09#appendix-A:
			//
			// rel_path      = [ path_segments ] [ "?" query ]
			// query         = *urlc
			// urlc          = reserved | unreserved | escaped
		    // reserved      = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+"
			// unreserved    = alpha | digit | mark
		    // escaped       = "%" hex hex
			
		    // http://tools.ietf.org/html/rfc3986#page-23:
		    //
		    // query      = *( pchar / "/" / "?" )
		    // pchar      = unreserved / pct-encoded / sub-delims / ":" / "@"
		    // unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
		    // sub-delims = "!" / "$" / "&" / "'" / "(" / ")"
            //                 / "*" / "+" / "," / ";" / "="
			"(?:\\?" +
				"(?:" +
					// field
					"(?:([a-zA-Z\\d;/:_~',\\Q-?@$+*.!()\\E]|%[a-fA-F\\d]{2}))+" +
					// = 
					"=" +
					// value &
					"(?:([a-zA-Z\\d;/:_~',\\Q-?@$+*.!()\\E]|%[a-fA-F\\d]{2}))+[&;]" +
				")*" + 
					
				"(?:" +
					// field
					"(?:([a-zA-Z\\d;/:_~',\\Q-?@$+*.!()\\E]|%[a-fA-F\\d]{2}))+" +
					// =
					"=" +
					// value
					"(?:([a-zA-Z\\d;/:_~',\\Q-?@$+*.!()\\E]|%[a-fA-F\\d]{2}))+" +
				")" +
			")?" +
			
			// fragment
			"(?:#(?:([a-zA-Z\\d;/:_~',=\\Q-?@$+*.!()\\E]|%[a-fA-F\\d]{2}))*)?" +
			"$";
	
	@Override
	public void setParentDimension(String parentDimension) {
		parentDim = parentDimension;

	}

	
	@Override
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	
	@Override
	public String getParentDimension() {
		return parentDim;
	}

	@Override
	public void registerMeasureDataSink(MeasureDataSink sink) {
		this.sink = sink;
	}

	@Override
	public void registerDbConnection(Connection conn) {
		// pass
	}

	@Override
	public void registerPinpointer(Pinpointer pinpointer) {
		this.pinpointer = pinpointer;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void assessNodes(Triple triple) {
		Node subj = triple.getSubject();
		if (subj.isURI() && !subj.getURI().matches(httpUrlPattern)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeBack("subject" , triple, viewQuads);
		}
		
		Node pred = triple.getPredicate();
		if (!pred.getURI().matches(httpUrlPattern)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeBack("predicate", triple, viewQuads);
		}
		
		Node obj = triple.getObject();
		if (obj.isURI() && !obj.getURI().matches(httpUrlPattern)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeBack("object", triple, viewQuads);
		}
	}

	
	private void writeBack(String position, Triple triple, Set<ViewQuad<ViewDefinition>> viewQuads){
		// FIXME: just a debug dummy
		String reason = "";
		for (ViewQuad<ViewDefinition> viewQuad : viewQuads) {
			reason += viewQuad.getQuad() + "\n" +
					"of the following view definition:\n" +
					viewQuad.getView() + "\n";
		}
		
		reason += "\n\n";
		MeasureDatum datum = new MeasureDatum(
				parentDim, name, 0, position + " position of " + triple.toString(), ""); //reason);
		sink.write(datum);
	}

}
