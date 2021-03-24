package com.smartone.ddm.resource.service;

import java.util.List;

import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.ibs.code.service.BasicService;
import com.smartone.ddm.resource.entity.DmResourcePrepose;

/**
 * 
 * @author DougLei
 */
@TransactionComponent
public class DmResourcePreposeService  extends BasicService{
	
	@Transaction
	public void insertMany(List<DmResourcePrepose> list) {
		tableSessionSave(list);
	 }
	
	@Transaction
	public void updateMany(List<DmResourcePrepose> list) {
		tableSessionUpdate(list);
	}
	

	@Transaction
	public void delete(String ids) {
		deleteByIds("DM_RESOURCE_PREPOSE","ID",ids);
	}
}
