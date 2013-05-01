package org.aksw.sml_eval.web.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.util.jdbc.DataSourceConfig;
import org.aksw.sml_eval.core.TaskBundle;
import org.aksw.sml_eval.core.TaskRepo;
import org.aksw.sml_eval.mappers.Adapter;
import org.aksw.sml_eval.mappers.MapperFactory;

public class MapperRepo {
	//private TaskRepo taskRepo;
	
	private Map<String, Map<String, Adapter>> toolToTaskToMapper;

	public MapperRepo(Map<String, Map<String, Adapter>> toolToTaskToMapper) {
		this.toolToTaskToMapper = toolToTaskToMapper;
	}
	

	public static Map<String, Map<String, Adapter>> createMap(TaskRepo taskRepo, Map<String, MapperFactory> toolToFactory) {
		
		Map<String, Map<String, Adapter>> result = new HashMap<String, Map<String, Adapter>>();
		
		for(Entry<String, MapperFactory> entry : toolToFactory.entrySet()) {

			String toolName = entry.getKey();
			MapperFactory mapperFactory = entry.getValue();
		
			Map<String, Adapter> taskToMapper = new HashMap<String, Adapter>();
			result.put(toolName, taskToMapper);
			
			
			for(TaskBundle task : taskRepo.getTasks().values()) {
				
				String taskName = task.getTaskName();

				DataSourceConfig dsc = task.getDataSourceConfig();
				Adapter mapper = mapperFactory.createMapper(dsc);
				
				taskToMapper.put(taskName, mapper);
			}
		}
		
		return result;
	}
	
	public static MapperRepo create(TaskRepo taskRepo, Map<String, MapperFactory> toolToFactory) {
		Map<String, Map<String, Adapter>> toolToTaskToMapper = createMap(taskRepo, toolToFactory);
		
		MapperRepo result = new MapperRepo(toolToTaskToMapper);
		
		return result;
	}
	
	public Adapter getMapper(String mappingToolName, String taskId) {

		Map<String, Adapter> taskToMapper = toolToTaskToMapper.get(mappingToolName);
		
		if(taskToMapper == null) {
			throw new RuntimeException("No mapper found for '" + mappingToolName + "'");
		}
		
		Adapter result = taskToMapper.get(taskId);
		if(result == null) {
			throw new RuntimeException("No task found for '" + taskId + "'");
		}
		return result;
	}
}
