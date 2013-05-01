package org.aksw.sml_eval.mappers;

import java.io.File;

import org.aksw.commons.util.jdbc.DataSourceConfig;

public interface MapperFactory {
	Adapter createMapper(DataSourceConfig dsc);
}
