package com.smartone.ddm.resource.entity;

import com.douglei.api.doc.annotation.ApiEntity;
import com.douglei.api.doc.annotation.ApiEntityParam;
import com.ibs.code.entity.BasicEntity;
/**
 * 
 * @author wangShuFang
 */
@SuppressWarnings("serial")
@ApiEntity
public class DmResource extends BasicEntity{
	 @ApiEntityParam
	 private String descName;
	 @ApiEntityParam
     private String resourceName;
     private String oldResourceName;
     private int createModel = 1;
     private int isBuildModel = 0; 
     @ApiEntityParam 
     private String content;
     //0 table资源  1 sql资源
     @ApiEntityParam
     private int resourceType;
     @ApiEntityParam
     private String moduleId;
     
     private int supportDelete = 0;
 	
 	 private int supportCover = 0;
 	 
 	 private boolean isSelectSql = false;
 	 
 	 //private int isStopUsing = 0;
 	 
 	 private int isEverBuildModel;
 	 
 	
	public int getIsEverBuildModel() {
		return isEverBuildModel;
	}
	public void setIsEverBuildModel(int isEverBuildModel) {
		this.isEverBuildModel = isEverBuildModel;
	}
	private String serviceInfoId;
 	 
 	
	public String getServiceInfoId() {
		return serviceInfoId;
	}
	public void setServiceInfoId(String serviceInfoId) {
		this.serviceInfoId = serviceInfoId;
	}
//	public int getIsStopUsing() {
//		return isStopUsing;
//	}
//	public void setIsStopUsing(int isStopUsing) {
//		this.isStopUsing = isStopUsing;
//	}
	public boolean isSelectSql() {
		return isSelectSql;
	}
	public void setSelectSql(boolean isSelectSql) {
		this.isSelectSql = isSelectSql;
	}
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
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
	public String getOldResourceName() {
		return oldResourceName;
	}
	public void setOldResourceName(String oldResourceName) {
		this.oldResourceName = oldResourceName;
	}
	public int getCreateModel() {
		return createModel;
	}
	public void setCreateModel(int createModel) {
		this.createModel = createModel;
	}
	public int getIsBuildModel() {
		return isBuildModel;
	}
	public void setIsBuildModel(int isBuildModel) {
		this.isBuildModel = isBuildModel;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}
	
	public int getSupportDelete() {
		return supportDelete;
	}
	public int getResourceType() {
		return resourceType;
	}
	public void setSupportDelete(int supportDelete) {
		this.supportDelete = supportDelete;
	}
	public int getSupportCover() {
		return supportCover;
	}
	public void setSupportCover(int supportCover) {
		this.supportCover = supportCover;
	}
}
