package org.aksw.sparqlify.qa.metrics.availability;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.qa.sinks.MeasureDataSink;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


class Handler200 implements HttpHandler {

	@Override
	public void handle(HttpExchange e) throws IOException {
		String response =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<empty/>";
        e.sendResponseHeaders(301, response.length());
        OutputStream os = e.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}
}


class Handler301 implements HttpHandler {

	@Override
	public void handle(HttpExchange e) throws IOException {
		
		e.getResponseHeaders().add("Location", "http://" +
				DereferenceableForwardLinksTest.hostName + ":" +
				DereferenceableForwardLinksTest.port +
				DereferenceableForwardLinksTest.okPath);
		e.sendResponseHeaders(301, 0);
        OutputStream out = e.getResponseBody();
        out.close();
	}
}


class Handler404 implements HttpHandler {

	@Override
	public void handle(HttpExchange e) throws IOException {

		e.sendResponseHeaders(404, 0);
        OutputStream out = e.getResponseBody();
        out.close();
	}
}


class Handler500 implements HttpHandler {

	@Override
	public void handle(HttpExchange e) throws IOException {

		e.sendResponseHeaders(500, 0);
        OutputStream out = e.getResponseBody();
        out.close();
	}
}


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_bool_beans.xml"})
public class DereferenceableForwardLinksTest {

	@Autowired
	MeasureDataSink sink;
	@Autowired
	DereferenceableForwardLinks metric;
	@Autowired
	Pinpointer pinpointer;
	
	final static int port = 8080;
	int portNotServed = 8081;
	final static String hostName = "localhost";
	final static String okPath = "/ok";
	final static String redirectPath = "/redirect";
	final static String notFoundPath = "/not_found";
	final static String serverErrPath = "/serv_err";
	HttpServer server;


	@Before
	public void setUp() throws Exception {
		pinpointer.registerViewDefs(new ArrayList<ViewDefinition>());
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext(okPath, new Handler200());
		server.createContext(redirectPath, new Handler301());
		server.createContext(notFoundPath, new Handler404());
		server.createContext(serverErrPath, new Handler500());
		server.setExecutor(null);
		server.start();
	}


	@After
	public void tearDown() throws Exception {
		server.stop(0);
	}


	/*
	 * URI resolvable (subject)
	 */
	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://" + hostName + ":" + port + okPath);
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI resolvable (predicate)
	 */
	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://" + hostName + ":" + port + okPath);
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI resolvable (object)
	 */
	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://" + hostName + ":" + port + okPath);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI redirect (subject)
	 */
	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://" + hostName + ":" + port + redirectPath);
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI redirect (predicate)
	 */
	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://" + hostName + ":" + port + redirectPath);
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI redirect (object)
	 */
	@Test
	public synchronized void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://" + hostName + ":" + port + redirectPath);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI not found (subject)
	 */
	@Test
	public synchronized void test07() throws NotImplementedException, SQLException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://" + hostName + ":" + port + notFoundPath);
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI not found (predicate)
	 */
	@Test
	public synchronized void test08() throws NotImplementedException, SQLException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://" + hostName + ":" + port + notFoundPath);
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertTrue(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI not found (object)
	 */
	@Test
	public synchronized void test09() throws NotImplementedException, SQLException {
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://" + hostName + ":" + port + notFoundPath);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertTrue(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * server error URI (subject)
	 */
	@Test
	public synchronized void test10() throws NotImplementedException, SQLException {
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://" + hostName + ":" + port + serverErrPath);
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * server error URI (predicate)
	 */
	@Test
	public synchronized void test11() throws NotImplementedException, SQLException {
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://" + hostName + ":" + port + serverErrPath);
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertTrue(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * server error URI (object)
	 */
	@Test
	public synchronized void test12() throws NotImplementedException, SQLException {
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://" + hostName + ":" + port + serverErrPath);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertTrue(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI not served (subject)
	 */
	@Test
	public synchronized void test13() throws NotImplementedException, SQLException {
		String metricName = "test13";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://" + hostName + ":" + portNotServed + okPath);
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI not served (predicate)
	 */
	@Test
	public synchronized void test14() throws NotImplementedException, SQLException {
		String metricName = "test14";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://" + hostName + ":" + portNotServed + okPath);
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.SUBJECT));
		assertTrue(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.PREDICATE));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName,
				TriplePosition.OBJECT));
	}


	/*
	 * URI redirect (subject)
	 */
	@Test
	public synchronized void test15() throws NotImplementedException, SQLException {
		String metricName = "test15";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setPrefix("http://ex.org/");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://" + hostName + ":" + portNotServed + okPath);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(((BooleanTestingSink) sink).nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(((BooleanTestingSink) sink).nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
