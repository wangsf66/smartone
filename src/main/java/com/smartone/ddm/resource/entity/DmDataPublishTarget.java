package com.smartone.ddm.resource.entity;

import com.ibs.code.entity.BasicEntity;

public class DmDataPublishTarget extends BasicEntity{
	
	private String refDataId;
	//服务大分类
	private String objectId;
	
	private String dataType;
	
	private int isSuccess;
	
	private int state;
	
	private String pid;
	
	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getRefDataId() {
		return refDataId;
	}

	public void setRefDataId(String refDataId) {
		this.refDataId = refDataId;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public int getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(int isSuccess) {
		this.isSuccess = isSuccess;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
