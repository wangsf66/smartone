package com.smartone.ddm.busimodel.entity;

import com.douglei.api.doc.annotation.ApiEntity;
import com.douglei.api.doc.annotation.ApiEntityParam;
import com.ibs.code.entity.BasicEntity;
/**
 * 
 * @author wangShuFang
 */
@SuppressWarnings("serial")
@ApiEntity
public class DmBusiModel extends BasicEntity{
    @ApiEntityParam
	private String  name;
	private Integer isBuildModel=0;
    @ApiEntityParam
	private String resourceName;
	private String remark;
	@ApiEntityParam
    private String moduleId;

	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getIsBuildModel() {
		return isBuildModel;
	}
	public void setIsBuildModel(Integer isBuildModel) {
		this.isBuildModel = isBuildModel;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
