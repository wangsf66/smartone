package com.smartone.ddm.resource.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourceDataAdapters {
	private String resourceName;
	private List<Map<String,Object>> tableDataList = new ArrayList<Map<String,Object>>();
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public List<Map<String, Object>> getTableDataList() {
		return tableDataList;
	}
	public void setTableDataList(List<Map<String, Object>> tableDataList) {
		this.tableDataList = tableDataList;
	}
	
	public ResourceDataAdapters(String resourceName, List<Map<String, Object>> tableDataList) {
		super();
		this.resourceName = resourceName;
		this.tableDataList = tableDataList;
	}
	public ResourceDataAdapters() {
		super();
		// TODO Auto-generated constructor stub
	}
}
