package org.aksw.sml_eval.mappers;

import org.aksw.commons.util.jdbc.DataSourceConfig;

public interface Mapper {
	MapResult map(DataSourceConfig dsc, String mappingStr);
}
