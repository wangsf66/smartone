package com.smartone.ddm.resource.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibs.code.entity.BasicEntity;

public class DmPublishState extends BasicEntity{
	private String serviceId;
	private int isSuccess;
	private String resourceId;
	private String errorMessage;
	
	private Map<String,Object> tableDataMap = new HashMap<String,Object>();
	
	public Map<String, Object> getTableDataMap() {
		return tableDataMap;
	}
	public void setTableDataMap(Map<String, Object> tableDataMap) {
		this.tableDataMap = tableDataMap;
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
	public int getIsSuccess() {
		return isSuccess;
	}
	public void setIsSuccess(int isSuccess) {
		this.isSuccess = isSuccess;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public DmPublishState(String serviceId, int isSuccess, String resourceId, String errorMessage) {
		super();
		this.serviceId = serviceId;
		this.isSuccess = isSuccess;
		this.resourceId = resourceId;
		this.errorMessage = errorMessage;
	}
	public DmPublishState() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
