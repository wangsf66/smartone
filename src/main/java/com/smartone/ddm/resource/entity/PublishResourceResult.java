package com.smartone.ddm.resource.entity;

public class PublishResourceResult{
	private String id;
	private int resourceType;
	private String descName;
    private String resourceName;
    private String moduleId;
    private String objectId;
    private int isChecked;
    private int isSuccess;
    private String isCheckedDesc;
    private String isSuccessDesc;
     
 	public int getIsSuccess() {
		return isSuccess;
	}
	public void setIsSuccess(int isSuccess) {
		this.isSuccess = isSuccess;
	}
	public String getIsCheckedDesc() {
		return isCheckedDesc;
	}
	public void setIsCheckedDesc(String isCheckedDesc) {
		this.isCheckedDesc = isCheckedDesc;
	}
	public String getIsSuccessDesc() {
		return isSuccessDesc;
	}
	public void setIsSuccessDesc(String isSuccessDesc) {
		this.isSuccessDesc = isSuccessDesc;
	}
	public int getIsChecked() {
 		return isChecked;
 	}
 	public void setIsChecked(int isChecked) {
 		this.isChecked = isChecked;
 	}
	
	public String getDescName() {
		return descName;
	}

	public void setDescName(String descName) {
		this.descName = descName;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PublishResourceResult() {
		super();
	}
}
