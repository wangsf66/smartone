package com.smartone.ddm.resource.service;

import java.util.ArrayList;
import java.util.List;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.mapping.MappingTypeNameConstants;
import com.ibs.code.service.BasicService;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourcePrepose;
import com.smartone.ddm.resource.entity.DmResourcePublishTarget;
import com.smartone.ddm.resource.entity.DmService;
import com.smartone.ddm.util.ResourceTypeUtil;
import com.smartone.ex.mapping.ExMappingTypeConstants;

/**
 * 
 * @author DougLei
 */
@TransactionComponent
public class DmResourcePublishTargetService  extends BasicService{
	
	@Transaction
	public void insertIsLocking(DmResourcePublishTarget resourcePublish) {
		resourcePublish.setState(3);
		insert(resourcePublish);
	}
	
	@Transaction
	public void insertIschecked(DmResourcePublishTarget resourcePublish) {
		resourcePublish.setState(1);
		insert(resourcePublish);
	 }

	private void insert(DmResourcePublishTarget resourcePublish) {
		String resourceIDS[] = null; 
		List<DmResourcePublishTarget> resourceList = new ArrayList<DmResourcePublishTarget>();
		resourceIDS = resourcePublish.getResourceId().split(",");
		DmService dmService = SessionContext.getSqlSession().uniqueQuery(DmService.class, "select * from DM_SERVICE where id='"+resourcePublish.getObjectId()+"'");
		for(String id:resourceIDS) {
			//保存关系信息
			savePublishTarget(resourcePublish,dmService,id,resourceList);
		}
		//递归保存排序存储过程资源
		if(resourceList.size()>0) {
			queryPreposeResource(resourceList,resourcePublish);
		}
	}
	
	
	private void savePublishTarget(DmResourcePublishTarget resourcePublish,DmService dmService,String id,List<DmResourcePublishTarget> resourceList) {
		//对已存在关系信息做跟新操作，其他做保存操作，存储过程资源放入统一的list，后期做递归保存
		DmResource dmResource = null;
		DmResourcePublishTarget dmResourcePublishTarget = SessionContext.getSqlSession().uniqueQuery(DmResourcePublishTarget.class,"select * from DM_RESOURCE_PUBLISH_TARGET where OBJECT_ID='"+resourcePublish.getObjectId()+"' AND RESOURCE_ID = '"+id+"'");
		if(dmResourcePublishTarget!=null) {
		   dmResourcePublishTarget.setState(resourcePublish.getState());
		   tableSessionUpdate(dmResourcePublishTarget);
		}else {
		    dmResourcePublishTarget = new  DmResourcePublishTarget(id,resourcePublish.getObjectId(),resourcePublish.getState());
		    if(dmService.getPid()==null) {
				dmResourcePublishTarget.setServiceType(0);
		    }else {
				dmResourcePublishTarget.setServiceType(1);
			}
			dmResource = SessionContext.getSqlSession().uniqueQuery(DmResource.class, "select * from DM_RESOURCE where id='"+id+"'");
			if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_TABLE) {
				dmResourcePublishTarget.setResourceType( MappingTypeNameConstants.TABLE);
			}else if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_SQL) {
				dmResourcePublishTarget.setResourceType( MappingTypeNameConstants.SQL);
			}else if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_PROCEDURE) {
				dmResourcePublishTarget.setResourceType( MappingTypeNameConstants.PROCEDURE);
			}else if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_BUSIMODEL) {
				dmResourcePublishTarget.setResourceType(ExMappingTypeConstants.BUSIMODEL);
			}
			if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_PROCEDURE) {
				resourceList.add(dmResourcePublishTarget);
			}else {
				tableSessionSave(dmResourcePublishTarget);
			}
		}
	}

	//TODO 前置资源表中查找前queryPreposeResource置资源(递归)
	private void queryPreposeResource(List<DmResourcePublishTarget> resourceList,DmResourcePublishTarget resourcePublish) {
		DmResourcePublishTarget dmResourcePublishTarget = null;
		//查询所有存储过程的前置条件
	    List<DmResourcePrepose> preposeResourceList  = SessionContext.getSQLSession().query(DmResourcePrepose.class,"selectResourceList", "selectPreposeResource",getIds(resourceList));
        List<DmResourcePublishTarget> list = new ArrayList<DmResourcePublishTarget>();
        List<DmResourcePrepose> rpList = new ArrayList<DmResourcePrepose>();
        for(DmResourcePublishTarget publishTarget:resourceList) {
			list.add(publishTarget);
		}
        boolean isExist = true;
	    if(preposeResourceList.size()>0&&isExist==true) {
	    	//添加放置递归循环的判断
	    	for(DmResourcePrepose resourcePrepose:preposeResourceList) {
		    	dmResourcePublishTarget = new DmResourcePublishTarget(resourcePrepose.getPreposeResourceId(),resourcePublish.getObjectId(), MappingTypeNameConstants.PROCEDURE,
		    			resourcePublish.getServiceType(),resourcePublish.getState());
		    	if(rpList.contains(resourcePrepose)) {
	    			isExist = false;
	    		}else {
	    			rpList.add(resourcePrepose); 
	    			list.add(dmResourcePublishTarget); 
	    		}
	    	}
	    	recursiveQueryPreposeResource(preposeResourceList,resourcePublish,list,isExist,rpList);
	    }
	    int orderCode = list.size();
		for(DmResourcePublishTarget publishTarget:list) {
			publishTarget.setOrderCode(orderCode--);
		}
		tableSessionSave(list);
	}
	
	private void recursiveQueryPreposeResource(List<DmResourcePrepose> resourceList,DmResourcePublishTarget resourcePublish,List<DmResourcePublishTarget> list,boolean isExist,List<DmResourcePrepose> rpList) {
		DmResourcePublishTarget dmResourcePublishTarget = null;
		List<DmResourcePrepose> preposeResourceList  = SessionContext.getSQLSession().query(DmResourcePrepose.class,"selectResourceList", "selectPreposeResource",getPreposeIds(resourceList));
	    if(preposeResourceList.size()>0&&isExist==true) {
	    	for(DmResourcePrepose resourcePrepose:preposeResourceList) {
		    	dmResourcePublishTarget = new DmResourcePublishTarget(resourcePrepose.getPreposeResourceId(),resourcePublish.getObjectId(), MappingTypeNameConstants.PROCEDURE,
		    			resourcePublish.getServiceType(),resourcePublish.getState());
		    	if(rpList.contains(resourcePrepose)) {
	    			isExist = false;
	    		}else {
	    			rpList.add(resourcePrepose); 
	    			list.add(dmResourcePublishTarget); 
	    		}
	    	}
	    	recursiveQueryPreposeResource(preposeResourceList,resourcePublish,list,isExist,rpList);
	    }
	}
	
	private String[] getIds(List<DmResourcePublishTarget> resourceList) {
		String[] str = new String[resourceList.size()];
		for(int i=0;i<resourceList.size();i++) {
			str[i] = resourceList.get(i).getResourceId();
		}	
		return str;
   }
	
   private String[] getPreposeIds(List<DmResourcePrepose> resourceList) {
		String[] str = new String[resourceList.size()];
		for(int i=0;i<resourceList.size();i++) {
			str[i] = resourceList.get(i).getPreposeResourceId();
		}	
		return str;
   }
  
   @Transaction
   public void test(String id) { 
	   List<DmResourcePrepose> preposeResourceList  = SessionContext.getSqlSession().query(DmResourcePrepose.class,"\r\n" + 
	   		"  select * from DM_RESOURCE_PREPOSE where RESOURCE_ID='"+id+"'");
	   List<DmResourcePrepose> list = new ArrayList<DmResourcePrepose>();
       boolean isExist = true;
	   if(preposeResourceList.size()>0&&isExist==true) {
	    	//添加放置递归循环的判断
		   for(DmResourcePrepose resourcePrepose:preposeResourceList) {
	    		if(list.contains(resourcePrepose)) {
	    			isExist = false;
	    		}else {
	    			list.add(resourcePrepose); 
	    		}
	    	}
		   recursiveQueryPreposeResource(preposeResourceList,list,isExist);
	   }
	   ResponseContext.addData(list);
   }
   
   private void recursiveQueryPreposeResource(List<DmResourcePrepose> resourceList,List<DmResourcePrepose> list,boolean isExist ) {
		List<DmResourcePrepose> preposeResourceList  = SessionContext.getSQLSession().query(DmResourcePrepose.class,"selectResourceList", "selectPreposeResource",getPreposeIds(resourceList));
	    if(preposeResourceList.size()>0&&isExist==true) {
	    	for(DmResourcePrepose resourcePrepose:preposeResourceList) {
	    		if(list.contains(resourcePrepose)) {
	    			isExist = false;
	    		}else {
	    			list.add(resourcePrepose); 
	    		}
	    	}
	    	recursiveQueryPreposeResource(preposeResourceList,list,isExist);
	    }
	}
}
