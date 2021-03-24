package com.smartone.ddm.util;

import java.util.Date;

import com.douglei.orm.configuration.environment.mapping.SqlMappingParameterDefaultValueHandler;
import com.ibs.components.filters.request.header.RequestHeaderContext;
/**
 * 
 * @author wangShuFang
 */
public class SqlRescourceDefaultValue extends SqlMappingParameterDefaultValueHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 522532705762406660L;

	public Object getDefaultValue(String value) {
		if("_accountId".equals(value)) {
			return RequestHeaderContext.getTokenEntity().getAccountId();
		}else if("_currentTime".equals(value)) {
			return new Date();
		}else if("_projectId".equals(value)) {
			return RequestHeaderContext.getTokenEntity().getProjectId();
		}else if("_customerId".equals(value)) {
			return RequestHeaderContext.getTokenEntity().getCustomerId();
		}
		return value;
	}
} 
