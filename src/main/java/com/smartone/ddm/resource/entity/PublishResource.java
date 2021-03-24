package com.smartone.ddm.resource.entity;

public class PublishResource{
	 private int resourceType;
	 private String resourceName;
	 private String moduleId;
	 private String objectId;
	 private int rows;
	 private int page;
     
	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public PublishResource(String resourceName, String moduleId, String objectId) {
		super();
		this.resourceName = resourceName;
		this.moduleId = moduleId;
		this.objectId = objectId;
	}
	
	public PublishResource() {
		super();
	}
}
