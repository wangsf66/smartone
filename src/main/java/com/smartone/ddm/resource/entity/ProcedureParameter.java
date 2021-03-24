package com.smartone.ddm.resource.entity;
/**
 * 
 * @author wangShuFang
 */
public class ProcedureParameter {
	private String parameterName=null;
	private String defaultValue = null;
	private String dataType=null;
	private Integer length=0;
	private Integer precision=0;
	private Integer inout;
	
	public Integer getInout() {
		return inout;
	}
	public void setInout(Integer inout) {
		this.inout = inout;
	}
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	public Integer getPrecision() {
		return precision;
	}
	public void setPrecision(Integer precision) {
		this.precision = precision;
	}
	public ProcedureParameter() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ProcedureParameter(String parameterName, String defaultValue, String dataType, Integer length, Integer precision,
			Integer inout) {
		super();
		this.parameterName = parameterName;
		this.defaultValue = defaultValue;
		this.dataType = dataType;
		this.length = length;
		this.precision = precision;
		this.inout = inout;
	}
}
