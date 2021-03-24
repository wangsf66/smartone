package com.smartone.ddm.resource.service;


import java.util.List;
import java.util.Map;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
/**
 * 
 * @author wangShuFang
 */
@TransactionComponent
public class DmResourceMappingService {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Transaction
	public List query() {
		Object obj = SessionContext.getSqlSession().query("select * from DM_RESOURCE_MAPPING");
		return (List<Map<String,Object>>)obj;
	}
	
}
