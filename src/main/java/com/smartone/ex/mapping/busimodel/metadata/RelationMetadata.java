package com.smartone.ex.mapping.busimodel.metadata;

import java.util.List;

import com.douglei.orm.mapping.metadata.AbstractMetadata;
import com.douglei.tools.StringUtil;

public class RelationMetadata extends AbstractMetadata{
	private String refBusiModelName;// 关联的业务模型名称
	private String parentName;// 父级名称
	private String idPropName;// 资源的主键属性名
	private String refResourceType;// 关联的资源类型
	private String refResourceKeyName;// 关联的资源的key名
	private String refParentResourcePropId;// 关联外键的属性名
	private String isCascadeDelete;//是否级联删除
	private String orderCode;// 排序值
	private String refResourceName;// 关联资源名称
	private List<RelationMetadata> childrenList;// 子节点
	private List<SqlMetadata> sqlList; //sql语句集合
	
	public RelationMetadata(String refBusiModelName, String parentName, String idPropName, String refResourceType,
			String refResourceKeyName, String refParentResourcePropId, String isCascadeDelete, String orderCode,
			String refResourceName) {
		super();
		this.refBusiModelName = refBusiModelName;
		this.parentName = parentName;
		this.idPropName = StringUtil.isEmpty(idPropName)?"ID":idPropName;
		this.refResourceType = refResourceType;
		this.refResourceKeyName = StringUtil.isEmpty(idPropName)?null:refResourceKeyName;
		this.refParentResourcePropId = refParentResourcePropId;
		this.isCascadeDelete = isCascadeDelete;
		this.orderCode = orderCode;
		this.refResourceName = refResourceName;
	}

	
	public RelationMetadata() {
		super();
	}
	
	public RelationMetadata(String name, String oldName) {
		super.name = name;
		super.oldName = oldName;
	}


	

	public String getRefBusiModelName() {
		return refBusiModelName;
	}
	public void setRefBusiModelName(String refBusiModelName) {
		this.refBusiModelName = refBusiModelName;
	}
	public String getParentName() {
		return parentName;
	}
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	public String getIdPropName() {
		return idPropName;
	}
	public void setIdPropName(String idPropName) {
		this.idPropName = idPropName;
	}
	public String getRefResourceType() {
		return refResourceType;
	}
	public void setRefResourceType(String refResourceType) {
		this.refResourceType = refResourceType;
	}
	public String getRefResourceKeyName() {
		return refResourceKeyName;
	}
	public void setRefResourceKeyName(String refResourceKeyName) {
		this.refResourceKeyName = refResourceKeyName;
	}
	public String getRefParentResourcePropId() {
		return refParentResourcePropId;
	}
	public void setRefParentResourcePropId(String refParentResourcePropId) {
		this.refParentResourcePropId = refParentResourcePropId;
	}
	public String getIsCascadeDelete() {
		return isCascadeDelete;
	}
	public void setIsCascadeDelete(String isCascadeDelete) {
		this.isCascadeDelete = isCascadeDelete;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public String getRefResourceName() {
		return refResourceName;
	}
	public void setRefResourceName(String refResourceName) {
		this.refResourceName = refResourceName;
	}
	public List<RelationMetadata> getChildrenList() {
		return childrenList;
	}
	public void setChildrenList(List<RelationMetadata> childrenList) {
		this.childrenList = childrenList;
	}
	public List<SqlMetadata> getSqlList() {
		return sqlList;
	}
	public void setSqlList(List<SqlMetadata> sqlList) {
		this.sqlList = sqlList;
	}
}
