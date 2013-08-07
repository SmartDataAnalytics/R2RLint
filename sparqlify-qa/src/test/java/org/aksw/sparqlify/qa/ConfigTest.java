/**
 * 
 */
package org.aksw.sparqlify.qa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.qa.main.Config;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class ConfigTest {
	
	Config config;
	String dimensionsConfigFilePath = "src/test/resources/dimensions.properties";
	
	final String dummyKey = "nonsense";
	final String dummyVal = "23";
	final String dimAccKey = "accuracy";
	final String dimAccVal = "yes";
	final String disabledDim = "licensing";
	final String enabledDim = "availability";
	final List<String> configuredDimensions = Arrays.asList("availability", "accuracy");
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		config = new Config(dimensionsConfigFilePath);
	}

	/**
	 * Test method for {@link org.aksw.sparqlify.qa.main.Config#get(java.lang.String)}.
	 */
	@Test
	public void testGet() {
		String dummyRes = config.get(dummyKey);
		assertEquals(dummyVal, dummyRes);
		
		String dimAccRes = config.get(dimAccKey);
		assertEquals(dimAccRes, dimAccVal);
	}

	/**
	 * Test method for {@link org.aksw.sparqlify.qa.main.Config#dimensionEnabled(java.lang.String)}.
	 */
	@Test
	public void testDimensionEnabled() {
		assertTrue(config.dimensionEnabled(enabledDim));
		assertFalse(config.dimensionEnabled(disabledDim));
	}

	/**
	 * Test method for {@link org.aksw.sparqlify.qa.main.Config#getDimensionNames()}.
	 */
	@Test
	public void testGetDimensions() {
		assertEquals(configuredDimensions, config.getDimensionNames());
	}

}
