package com.smartone.ddm.resource.service;

import java.util.List;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.ibs.code.service.BasicService;
import com.smartone.ddm.resource.entity.DmPublishTableData;

/**
 * 
 * @author DougLei
 */
@TransactionComponent
public class DmPublishTableDataService  extends BasicService{
	
	@Transaction
	public void insertMany(List<DmPublishTableData> list) {
		DmPublishTableData oldTableData  = null;
		for(DmPublishTableData dmPublishTableData:list) {
			oldTableData = SessionContext.getSqlSession().uniqueQuery(DmPublishTableData.class,"select * from DM_PUBLISH_TABLE_DATA where SERVICE_ID = '"+dmPublishTableData.getServiceId()+"' and RESOURCE_ID='"+dmPublishTableData.getResourceId()+"' AND DATA_ID='"+dmPublishTableData.getDataId()+"'");
		    if(oldTableData==null) {
		    	tableSessionSave(dmPublishTableData);
		    }
		}
	}
	
	@Transaction
	public void updateMany(List<DmPublishTableData> list) {
		tableSessionUpdate(list);
	}
	

	@Transaction
	public void delete(String ids) {
		deleteByIds("DM_PUBLISH_TABLE_DATA","ID",ids);
	}
}


