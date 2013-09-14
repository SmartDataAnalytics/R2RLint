package org.aksw.sparqlify.qa.metrics.accuracy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

public class ValidLanguageTagTest {

	BooleanTestingSink sink;
	Pinpointer pinpointer;


	@Before
	public void setUp() throws Exception {
		sink = new BooleanTestingSink();
		pinpointer = new Pinpointer(new ArrayList<ViewDefinition>());
	}


	/*
	 * valid language tags (taken from
	 * http://www.rfc-editor.org/rfc/bcp/bcp47.txt)
	 */
	
	/*
	 * de (German)
	 */
	@Test
	public void test01() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "de", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * fr (French)
	 */
	@Test
	public void test02() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "fr", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * ja (Japanese)
	 */
	@Test
	public void test03() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "ja", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * i-enochian (example of a grandfathered tag)
	 */
	@Test
	public void test04() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "i-enochian", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * zh-Hant (Chinese written using the Traditional Chinese script)
	 */
	@Test
	public void test05() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "zh-Hant", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * zh-Hans (Chinese written using the Simplified Chinese script)
	 */
	@Test
	public void test06() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "zh-Hans", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * sr-Cyrl (Serbian written using the Cyrillic script)
	 */
	@Test
	public void test07() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "sr-Cyrl", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * sr-Latn (Serbian written using the Latin script)
	 */
	@Test
	public void test08() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "sr-Latn", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * FIXME: extended language subtags are currently not supported by
	 * org.openjena.riot.LangTag 
	 */
	
	/*
	 * zh-cmn-Hans-CN (Chinese, Mandarin, Simplified script, as used in
	 * China)
	 */
	@Test
	public void test09() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "zh-cmn-Hans-CN", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * cmn-Hans-CN (Mandarin Chinese, Simplified script, as used in China)
	 */
	@Test
	public void test10() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "cmn-Hans-CN", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * FIXME: extended language subtags are currently not supported by
	 * org.openjena.riot.LangTag 
	 */

	/*
	 * zh-yue-HK (Chinese, Cantonese, as used in Hong Kong SAR)
	 */
	@Test
	public void test11() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "zh-yue-HK", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * yue-HK (Cantonese Chinese, as used in Hong Kong SAR)
	 */
	@Test
	public void test12() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "yue-HK", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * zh-Hans-CN (Chinese written using the Simplified script as used in
	 * mainland China)
	 */
	@Test
	public void test13() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test13";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "zh-Hans-CN", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * sr-Latn-RS (Serbian written using the Latin script as used in Serbia)
	 */
	@Test
	public void test14() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test14";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "sr-Latn-RS", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * sl-rozaj (Resian dialect of Slovenian)
	 */
	@Test
	public void test15() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test15";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "sl-rozaj", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * sl-rozaj-biske (San Giorgio dialect of Resian dialect of Slovenian)
	 * 
	 * FIXME: should not fail
	 */
	@Test
	public void test16() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test16";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "sl-rozaj-biske", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * sl-nedis (Nadiza dialect of Slovenian)
	 */
	@Test
	public void test17() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test17";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "sl-nedis", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * de-CH-1901 (German as used in Switzerland using the 1901 variant
	 * [orthography])
	 * 
	 * FIXME: should not fail
	 */
	@Test
	public void test18() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test18";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "de-CH-1901", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * sl-IT-nedis (Slovenian as used in Italy, Nadiza dialect)
	 */
	@Test
	public void test19() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test19";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "sl-IT-nedis", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * hy-Latn-IT-arevela (Eastern Armenian written in Latin script, as used
	 * in Italy)
	 */
	@Test
	public void test20() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test20";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "hy-Latn-IT-arevela", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * de-DE (German for Germany)
	 */
	@Test
	public void test21() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test21";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "de-DE", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * en-US (English as used in the United States)
	 */
	@Test
	public void test22() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test22";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "en-US", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * es-419 (Spanish appropriate for the Latin America and Caribbean
	 * region using the UN region code)
	 */
	@Test
	public void test23() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test23";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "es-419", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * de-CH-x-phonebk (private use subtags)
	 */
	@Test
	public void test24() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test24";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "de-CH-x-phonebk", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * az-Arab-x-AZE-derbend (private use subtags)
	 * 
	 * FIXME: should not fail
	 */
	@Test
	public void test25() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test25";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "az-Arab-x-AZE-derbend", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * x-whatever (private use using the singleton 'x')
	 */
	@Test
	public void test26() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test26";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "x-whatever", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * qaa-Qaaa-QM-x-southern (all private tags)
	 */
	@Test
	public void test27() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test27";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "qaa-Qaaa-QM-x-southern", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * de-Qaaa (German, with a private script)
	 */
	@Test
	public void test28() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test28";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "de-Qaaa", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * sr-Latn-QM (Serbian, Latin script, private region)
	 */
	@Test
	public void test29() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test29";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "sr-Latn-QM", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * sr-Qaaa-RS (Serbian, private script, for Serbia)
	 */
	@Test
	public void test30() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test30";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "sr-Qaaa-RS", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * en-US-u-islamcal (tags that use extensions)
	 */
	@Test
	public void test31() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test31";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "en-US-u-islamcal", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * zh-CN-a-myext-x-private (tags that use extensions)
	 */
	@Test
	public void test32() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test32";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "zh-CN-a-myext-x-private", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * en-a-myext-b-another (tags that use extensions)
	 */
	@Test
	public void test33() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test33";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "en-a-myext-b-another", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * de-419-DE (invalid: two region tags)
	 */
	@Test
	public void test34() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test34";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "de-419-DE", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * a-DE (invalid: use of a single-character subtag in primary position;
	 * note that there are a few grandfathered tags that start with "i-" that
	 * are valid)
	 */
	@Test
	public void test35() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test35";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "a-DE", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * ar-a-aaa-b-bbb-a-ccc (invalid: two extensions with same single-letter
	 * prefix)
	 * 
	 * FIXME: should not fail
	 */
	@Test
	public void test36() throws NotImplementedException {
		ValidLanguageTag metric = new ValidLanguageTag();
		String metricName = "test36";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createLiteral("foo", "ar-a-aaa-b-bbb-a-ccc", false);
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
