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
public class DmResourceParam extends BasicEntity {
    @ApiEntityParam
    private String paramName;
    private String oldParamName;
    @ApiEntityParam
    private String descName;
    @ApiEntityParam
    private String datatype; 
    @ApiEntityParam
    private Integer length; 
    @ApiEntityParam
    private String defaultValue = "";
    @ApiEntityParam
    private Integer precision = 0;
    @ApiEntityParam
    private Integer isNullable = 1;
    @ApiEntityParam
    private Integer isUnique = 0; 
    @ApiEntityParam
    private Integer isValidate = 0; 
    @ApiEntityParam
    private Integer orderCode;
    @ApiEntityParam
    private String resourceId;
    //0 table资源  1 sql资源

    //0 参数集  1 结果集
    private Integer paramType = 0;
    
    private int inout;
    
	@Override
	public boolean equals(Object obj) {
		DmResourceParam object = (DmResourceParam)obj;
		return paramName.equals(object.getParamName());
	}
	
	public int getInout() {
		return inout;
	}
	public void setInout(int inout) {
		this.inout = inout;
	}
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	public String getOldParamName() {
		return oldParamName;
	}
	public void setOldParamName(String oldParamName) {
		this.oldParamName = oldParamName;
	}
	public String getDescName() {
		return descName;
	}
	public void setDescName(String descName) {
		this.descName = descName;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public Integer getPrecision() {
		return precision;
	}
	public void setPrecision(Integer precision) {
		this.precision = precision;
	}
	public Integer getIsNullable() {
		return isNullable;
	}
	public void setIsNullable(Integer isNullable) {
		this.isNullable = isNullable;
	}
	public Integer getIsUnique() {
		return isUnique;
	}
	public void setIsUnique(Integer isUnique) {
		this.isUnique = isUnique;
	}
	public Integer getIsValidate() {
		return isValidate;
	}
	public void setIsValidate(Integer isValidate) {
		this.isValidate = isValidate;
	}
	public Integer getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(Integer orderCode) {
		this.orderCode = orderCode;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public Integer getParamType() {
		return paramType;
	}
	public void setParamType(Integer paramType) {
		this.paramType = paramType;
	}
	
	
	public DmResourceParam() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DmResourceParam( String resourceId,String paramName,Integer paramType,Integer orderCode,String datatype) {
		this.paramName = paramName;
		this.datatype = datatype;
		this.orderCode = orderCode;
		this.resourceId = resourceId;
		this.paramType = paramType;
	}
	public DmResourceParam(String resourceId,String paramName,Integer paramType,Integer orderCode) {
		this.paramName = paramName;
		this.orderCode = orderCode;
		this.resourceId = resourceId;
		this.paramType = paramType;
	}

	public DmResourceParam(String parameterName, String dataType, Integer length, String defaultValue,
			Integer precision, int sort, String resourceId, int paramType, Integer inout) {
		this.paramName = parameterName;
		this.datatype = dataType;
		this.length = length;
		this.defaultValue = defaultValue;
		this.precision = precision;
		this.orderCode = sort;
		this.resourceId = resourceId;
		this.paramType = paramType;
		this.inout = inout;
	}
}
