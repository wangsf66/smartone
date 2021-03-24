package com.smartone.ddm.resource.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartone.ddm.util.ProcedureSqlType;

public class ResourceAdapters {
	private String name;
	private String type;
	private String content;
	private String resourceId;
	private ProcedureSqlType sqlType;
	private int isUpdate;
	private Map<ProcedureSqlType,Object> sqlMap;
	

	public ResourceAdapters(Resource resource) {
		this.name = resource.getName();
		this.type = resource.getType().getName();
		this.content = resource.getContent();
		this.resourceId = resource.getResourceId();
		this.sqlType = resource.getSqlType();
		this.isUpdate = resource.getIsUpdate();
	}
	
	public ResourceAdapters(List<Resource> resources) {
    	this.name = resources.get(0).getName();
		this.type = resources.get(0).getType().getName();
		this.resourceId = resources.get(0).getResourceId();
		this.isUpdate = resources.get(0).getIsUpdate();
		if(resources.size()==1) {
			this.content = resources.get(0).getContent();
		}else {
			sqlMap = new HashMap<ProcedureSqlType,Object>();
			for(Resource resource:resources) {
				if (resource.getSqlType().equals(ProcedureSqlType.CREATE)) {
					sqlMap.put(resource.getSqlType(), resource.getContent());
				}else if(resource.getSqlType().equals(ProcedureSqlType.CALL)) {
					sqlMap.put(resource.getSqlType(), resource.getContent());
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public String resource() {
		return resourceId;
	}

	public ProcedureSqlType getSqlType() {
		return sqlType;
	}

	public ResourceAdapters() {
		super();
	}

	public Map<ProcedureSqlType, Object> getSqlMap() {
		return sqlMap;
	}

	public void setSqlMap(Map<ProcedureSqlType, Object> sqlMap) {
		this.sqlMap = sqlMap;
	}

	public int getIsUpdate() {
		return isUpdate;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
}
