package org.aksw.sparqlify.qa.metrics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * Most of the test data is taken from from this great article
 * about Regex-based URL validation: http://mathiasbynens.be/demo/url-regex .
 * Thank you Mathias!
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 */
public class HttpUriTest {
	@Test
	public void test01() {
		assertTrue("http://foo.com/blah_blah".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test02() {
		assertTrue("http://foo.com/blah_blah/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test03() {
		assertTrue("http://foo.com/blah_blah_(wikipedia)".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test04() {
		assertTrue("http://foo.com/blah_blah_(wikipedia)_(again)".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test05() {
		assertTrue("http://www.example.com/wpstyle/?p=364".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test06() {
		assertTrue("https://www.example.com/foo/?bar=baz&inga=42&quux".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test07() {
		assertTrue("https://www.example.com/foo/?bar&inga=42&quux".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test08() {
		assertTrue("https://www.example.com/foo/?bar=baz&inga".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test09() {
		assertTrue("https://www.example.com/foo/?bar&inga".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test10() {
		assertFalse("http://✪df.ws/123".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test11() {
		assertTrue("http://userid:password@example.com:8080".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test12() {
		assertTrue("http://userid:password@example.com:8080/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test13() {
		assertTrue("http://userid@example.com".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test14() {
		assertTrue("http://userid@example.com/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test15() {
		assertTrue("http://userid@example.com:8080".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test16() {
		assertTrue("http://userid@example.com:8080/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test17() {
		assertTrue("http://userid:password@example.com".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test18() {
		assertTrue("http://userid:password@example.com/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test19() {
		assertTrue("http://142.42.1.1/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test20() {
		assertTrue("http://142.42.1.1:8080/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test21() {
		assertFalse("http://➡.ws/䨹".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test22() {
		assertFalse("http://⌘.ws".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test23() {
		assertTrue("http://foo.com/blah_(wikipedia)#cite-1".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test24() {
		assertTrue("http://foo.com/blah_(wikipedia)_blah#cite-1".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test25() {
		assertTrue("http://foo.com/(something)?after=parens".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test26() {
		assertTrue("http://code.google.com/events/#&product=browser".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test27() {
		assertTrue("http://j.mp".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test28() {
		// false because of the ftp protocol prefix
		assertFalse("ftp://foo.bar/baz".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test29() {
		assertTrue("http://foo.bar/?q=Test%20URL-encoded%20stuff".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test30() {
		assertFalse("http://ﻢﺛﺎﻟ.ﺈﺨﺘﺑﺍﺭ".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test31() {
		assertTrue("http://-.~_!$&'()*+,;=:%40:80%2f::::::@example.com".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test32() {
		assertTrue("http://1337.net".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test33() {
		assertTrue("http://a.b-c.de".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test34() {
		assertTrue("http://223.255.255.254".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test35() {
		assertFalse("http://".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test36() {
		assertFalse("http://.".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test37() {
		assertFalse("http://..".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test38() {
		assertFalse("http://../".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test39() {
		assertFalse("http://?".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test40() {
		assertFalse("http://??".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test41() {
		assertFalse("http://??/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test42() {
		assertFalse("http://#".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test43() {
		assertFalse("http://##".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test44() {
		assertFalse("http://##/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test45() {
		assertFalse("http://foo.bar?q=Spaces should be encoded".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test46() {
		assertFalse("//".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test47() {
		assertFalse("//a".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test48() {
		assertFalse("///a".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test49() {
		assertFalse("///".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test50() {
		assertFalse("http:///a".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test51() {
		assertFalse("foo.com".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test52() {
		assertFalse("h://test".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test53() {
		assertFalse("http:// shouldfail.com".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test54() {
		assertFalse(":// should fail".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test55() {
		assertFalse("http://foo.bar/foo(bar)baz quux".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test56() {
		assertFalse("ftps://foo.bar/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test57() {
		assertFalse("http://-error-.invalid/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test58() {
		assertFalse("http://a.b--c.de/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test59() {
		assertFalse("http://-a.b.co".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test60() {
		assertFalse("http://a.b-.co".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test61() {
		assertFalse("http://0.0.0.0".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test62() {
		assertFalse("http://0.0.0.0".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test63() {
		assertFalse("http://10.1.1.0".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test64() {
		assertFalse("http://10.1.1.255".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test65() {
		assertFalse("http://224.1.1.1".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test66() {
		assertFalse("http://1.1.1.1.1".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test67() {
		assertFalse("http://123.123.123".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test68() {
		assertFalse("http://3628126748".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test69() {
		assertFalse("http://.www.foo.bar/".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test70() {
		assertFalse("http://www.foo.bar./".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test71() {
		assertFalse("http://.www.foo.bar./".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test72() {
		assertFalse("http://10.1.1.1".matches(HttpUri.httpUrlPattern));
	}
	@Test
	public void test73() {
		assertFalse("http://10.1.1.254".matches(HttpUri.httpUrlPattern));
	}
}
