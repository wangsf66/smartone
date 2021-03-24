package com.smartone.ddm.resource.service;

import java.util.List;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.ibs.code.service.BasicService;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.resource.entity.DmPublishState;

/**
 * 
 * @author DougLei
 */
@TransactionComponent
public class DmPublishStateService  extends BasicService{
	
	@Transaction
	public void query(String serviceId) {
    	List<DmPublishState> publishStateList = SessionContext.getSqlSession().query(DmPublishState.class,"select * from DM_PUBLISH_STATE where SERVICE_ID ='"+serviceId+"'");
		ResponseContext.addData(publishStateList);
	}
}


