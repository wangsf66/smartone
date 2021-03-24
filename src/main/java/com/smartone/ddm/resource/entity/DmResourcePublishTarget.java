package com.smartone.ddm.resource.entity;

import com.ibs.code.entity.BasicEntity;

public class DmResourcePublishTarget extends BasicEntity{
	private String resourceId;
	//服务大分类
	private String objectId;
	
	private String resourceType;
	
	private int serviceType;
	
	private int state;
	
	private Integer orderCode;
	
	
	
	
	public Integer getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(Integer orderCode) {
		this.orderCode = orderCode;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public int getServiceType() {
		return serviceType;
	}

	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}

	public String getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	public DmResourcePublishTarget() {
		super();
	}

	public DmResourcePublishTarget(String resourceId, String objectId, String resourceType, int serviceType,
			int state) {
		super();
		this.resourceId = resourceId;
		this.objectId = objectId;
		this.resourceType = resourceType;
		this.serviceType = serviceType;
		this.state = state;
	}

	public DmResourcePublishTarget(String resourceId, String objectId, int state) {
		super();
		this.resourceId = resourceId;
		this.objectId = objectId;
		this.state = state;
	}
	
	
}
