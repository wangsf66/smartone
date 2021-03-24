package com.smartone.ddm.resource.entity;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.douglei.orm.mapping.MappingType;
import com.douglei.orm.mapping.MappingTypeContainer;
import com.smartone.ddm.util.ProcedureSqlType;

public class Resource {
	
	private static final String RESOURCE_FOLDER = System.getProperty("user.home") + File.separatorChar + ".dev-resources" + File.separatorChar;
	
	private List<ProcedureParameter> procedureParameterList;
	
	private MappingType type;
	
	private Boolean isUpdateName;
	
	private String resourceId;
	
	private String content;
	
	private String name;
	
	private int isUpdate;
	
	//资源建模后，修改会有oldName，无修改就为null
	private String oldName;
	
	private String nameSpace;
	
	private ProcedureSqlType sqlType;
	
	private List<DmResourceParam> tableParamsList;
	
	private  List<Map<String, Object>> sqllist;
	
	public int getIsUpdate() {
		return isUpdate;
	}

	public void setIsUpdate(int isUpdate) {
		this.isUpdate = isUpdate;
	}

	/**
	 * 获取资源的文件夹
	 * @return
	 */
	public String getFolder() {
		return RESOURCE_FOLDER;
	}
	
	/**
	 * 获取资源的文件名称, 包含后缀
	 * @return
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	/**
	 * 获取资源内容
	 * @return
	 */
	public String getContent() {
		// TODO Auto-generated method stub
		return content;
	}
	
	
	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public static String getResourceFolder() {
		return RESOURCE_FOLDER;
	}
	
	public MappingType getType() {
		return type;
	}
	public void setType(String typeName) {
		this.type = MappingTypeContainer.getMappingTypeByName(typeName);
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<ProcedureParameter> getProcedureParameterList() {
		return procedureParameterList;
	}

	public void setProcedureParameterList(List<ProcedureParameter> procedureParameterList) {
		this.procedureParameterList = procedureParameterList;
	}
	
	public List<DmResourceParam> getTableParamsList() {
		return tableParamsList;
	}

	public void setTableParamsList(List<DmResourceParam> tableParamsList) {
		this.tableParamsList = tableParamsList;
	}
	
	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public List<Map<String, Object>> getSqllist() {
		return sqllist;
	}

	public void setSqllist(List<Map<String, Object>> sqllist) {
		this.sqllist = sqllist;
	}

	public ProcedureSqlType getSqlType() {
		return sqlType;
	}

	public void setSqlType(ProcedureSqlType sqlType) {
		this.sqlType = sqlType;
	}

	public Boolean getIsUpdateName() {
		return isUpdateName;
	}

	public void setIsUpdateName(Boolean isUpdateName) {
		this.isUpdateName = isUpdateName;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	
}
