package org.aksw.sml_eval.mappers;

import java.io.File;

import org.aksw.commons.util.jdbc.DataSourceConfig;

public class MapperFactorySparqlMap
	implements MapperFactory
{
	private File exec;
	
	public MapperFactorySparqlMap(File exec) {
		this.exec = exec;
	}
	
	@Override
	public Adapter createMapper(DataSourceConfig dsc) {
		Adapter result = new AdapterCliSparqlMap(exec, dsc);
		return result;
	}

}
