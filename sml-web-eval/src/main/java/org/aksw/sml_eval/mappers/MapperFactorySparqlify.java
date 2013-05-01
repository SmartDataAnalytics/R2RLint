package org.aksw.sml_eval.mappers;

import java.io.File;

import org.aksw.commons.util.jdbc.DataSourceConfig;

public class MapperFactorySparqlify
	implements MapperFactory
{
	private File exec;
	
	public MapperFactorySparqlify(File exec) {
		this.exec = exec;
	}
	
	@Override
	public Adapter createMapper(DataSourceConfig dsc) {
		Adapter result = new AdapterCliSparqlify(exec, dsc);
		return result;
	}

}
