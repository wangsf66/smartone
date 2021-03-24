package com.smartone.ex.mapping.busimodel.metadata;

import com.douglei.orm.mapping.metadata.AbstractMetadata;

public class SqlMetadata extends AbstractMetadata{

	/**
	 * 
	 */
	private static final long serialVersionUID = -739652021319713997L;
	private String type;
    private String sqlType;

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public SqlMetadata(String name, String type, String oldName, String sqlType) {
		super.name = name;
		super.oldName = oldName;
		this.type = type;
		this.sqlType = sqlType;
	}
	
	public String getSqlType() {
		return sqlType;
	}
	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}
}
