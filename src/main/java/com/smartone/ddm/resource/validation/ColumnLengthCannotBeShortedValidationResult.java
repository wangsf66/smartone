package com.smartone.ddm.resource.validation;

import com.douglei.orm.mapping.validator.ValidateFailResult;

public  class ColumnLengthCannotBeShortedValidationResult extends ValidateFailResult{

	public ColumnLengthCannotBeShortedValidationResult(String fieldName, String originMessage, String code,
			Object... params) {
		super(fieldName, originMessage, code, params);
		// TODO Auto-generated constructor stub
	}
	
}
