package com.smartone.ddm.resource.validation;

import com.douglei.orm.mapping.validator.ValidateFailResult;

public class TableNothingValidationResult  extends ValidateFailResult{
	
	
	
	public TableNothingValidationResult(String fieldName, String originMessage, String code, Object... params) {
		super(fieldName, originMessage, code, params);
		// TODO Auto-generated constructor stub
	}

//	@Override
//	public String getOriginMessage() {
//		return  "关联的表信息不存在";
//	}
//	
//	@Override
//	public String getCode() {
//		return  "smartone.value.violation.TableNothing";
//	}
}
