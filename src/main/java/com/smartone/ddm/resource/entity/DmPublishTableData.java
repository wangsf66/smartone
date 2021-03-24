package com.smartone.ddm.resource.entity;

import com.ibs.code.entity.BasicEntity;

public class DmPublishTableData extends BasicEntity{
	private String serviceId;
	private String resourceId;
	private String dataId;
	private int isSuccess;

	public int getIsSuccess() {
		return isSuccess;
	}
	public void setIsSuccess(int isSuccess) {
		this.isSuccess = isSuccess;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getDataId() {
		return dataId;
	}
	public void setDataId(String dataId) {
		this.dataId = dataId;
	}
	
	public DmPublishTableData() {
		super();
		// TODO Auto-generated constructor stub
	}

}
