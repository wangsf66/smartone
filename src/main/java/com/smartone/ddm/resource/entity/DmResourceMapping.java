package com.smartone.ddm.resource.entity;

import com.ibs.code.entity.BasicEntity;
/**
 * 
 * @author wangShuFang
 */
@SuppressWarnings("serial")
public class DmResourceMapping extends BasicEntity{
	private String resourceId;
	private String resourceName;
	private String mappingContent;
	//0 table资源  1 sql资源
	private Integer resourceType = 0;
	
	private int isFailure = 0;
	
	public int getIsFailure() {
		return isFailure;
	}
	public void setIsFailure(int isFailure) {
		this.isFailure = isFailure;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getMappingContent() {
		return mappingContent;
	}
	public void setMappingContent(String mappingContent) {
		this.mappingContent = mappingContent;
	}
	public Integer getResourceType() {
		return resourceType;
	}
	public void setResourceType(Integer resourceType) {
		this.resourceType = resourceType;
	}
	public DmResourceMapping(String resourceId, String resourceName, String mappingContent, Integer resourceType) {
		super();
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.mappingContent = mappingContent;
		this.resourceType = resourceType;
	}
	public DmResourceMapping() {
		super();
		// TODO Auto-generated constructor stub
	}
}
