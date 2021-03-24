package com.smartone.ddm.resource.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSONObject;
import com.douglei.orm.context.PropagationBehavior;
import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.sql.query.page.PageResult;
import com.ibs.code.service.BasicService;
import com.ibs.components.response.ResponseContext;
import com.ibs.spring.eureka.cloud.feign.APIServer;
import com.ibs.spring.eureka.cloud.feign.RestTemplateWrapper;
import com.smartone.ddm.resource.entity.DmDataPublishTarget;
import com.smartone.ddm.resource.entity.DmPublishDataResult;
import com.smartone.ddm.resource.entity.DmPublishState;
import com.smartone.ddm.resource.entity.DmPublishTableData;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourcePublishTarget;
import com.smartone.ddm.resource.entity.DmService;
import com.smartone.ddm.resource.entity.PublishResource;
import com.smartone.ddm.resource.entity.PublishResourceResult;
import com.smartone.ddm.resource.entity.Resource;
import com.smartone.ddm.resource.entity.ResourceAdapters;

/**
 * 
 * @author DougLei
 */
@TransactionComponent
public class DmServiceService  extends BasicService{
	
	
	@Autowired
	private RestTemplateWrapper restTemplate;
	
	@Autowired
	private ResourceContentService ResourceContentService;
	
	@Transaction
	public void insertMany(List<DmService> list) {
		for(DmService dmService:list) {
			if(doValidate(dmService)) {
			   if(dmService.getPid()==null) {
				   dmService.setWeight(1);
			   }else {
				   dmService.setWeight(5); 
			   }
			   tableSessionSave(dmService);
			}
		}
	 }
	
	@Transaction		
	public void updateMany(List<DmService> list) {
		for(DmService dmService:list) {
			if(doValidate(dmService)) {
				if(dmService.getPid()==null) {
					 dmService.setWeight(1);
				}else {
					 dmService.setWeight(5); 
				}
				tableSessionUpdate(dmService);
			}
		}
	}

	@Transaction
	public void delete(String ids) {
		String idArray[] = ids.split(",");
		List<DmService> serviceList = null;
		DmService dmService  = null;
		for(String id :idArray) {
			dmService = SessionContext.getSqlSession().uniqueQuery(DmService.class,"select * from dm_service where id = '"+id+"'");
			if(dmService.getPid()==null) {
			    serviceList = SessionContext.getSqlSession().query(DmService.class,"select * from dm_service where pid = '"+id+"'");
			    if(serviceList.size()>0) {
				   ResponseContext.addValidation("id为%s的分类下有所属服务，不可被删除", null, id);
				   return;
				}
			    deleteByIds("DM_SERVICE","ID",id);
			}else {
			    deleteByIds("DM_SERVICE","ID",id);
			}
		}
	}
	
	
	public boolean doValidate(DmService dmService) {
		List<DmService> obj = null;
		if(dmService.getPid()==null) {
			//类型
			obj =(List<DmService>)SessionContext.getSqlSession().query(DmService.class,"select * from DM_SERVICE where PID is null and  NAME='"+dmService.getName()+"'");
		}else {
			//服务名
			obj =(List<DmService>)SessionContext.getSqlSession().query(DmService.class,"select * from DM_SERVICE where PID is not null and  NAME='"+dmService.getName()+"'");
		}
		if(obj!=null&&obj.size()>0){
			ResponseContext.addValidation("name为%s的名称不可重复插入", null, dmService.getName());
			return false;
		}
		return true;
	}
	
	@Transaction
	public void queryById(PublishResource publishResource) {
		DmService service = SessionContext.getSqlSession().uniqueQuery(DmService.class,"select * from dm_service where id='"+publishResource.getObjectId()+"'");
        //满足条件资源
		PageResult<PublishResourceResult> pageObject = null;
		List<PublishResourceResult> resourceList  = null;
		if(publishResource.getPage()!=0||publishResource.getRows()!=0) {
			pageObject = SessionContext.getSQLSession().pageQuery(PublishResourceResult.class, publishResource.getPage(), publishResource.getRows(),"selectResourceList", "selectPublishResource", publishResource);
			if(service.getPid()==null) {
				ResponseContext.addData(pageObject);
				return;
			}
		}else {
			resourceList = SessionContext.getSQLSession().query(PublishResourceResult.class,"selectResourceList", "selectPublishResource", publishResource);
			if(service.getPid()==null) {
				ResponseContext.addData(resourceList);
				return;
			}
		}
		//服务分类
		DmService parentService = SessionContext.getSqlSession().uniqueQuery(DmService.class,"select * from dm_service where id='"+service.getPid()+"'");
		publishResource.setObjectId(service.getPid());
		//查询服务所属分类的资源
		PageResult<PublishResourceResult> pageServiceResource = null;
		List<PublishResourceResult> serviceResourceList = null;
		if(publishResource.getPage()!=0||publishResource.getRows()!=0) {
			pageServiceResource = SessionContext.getSQLSession().pageQuery(PublishResourceResult.class, publishResource.getPage(), publishResource.getRows(),"selectResourceList", "selectPublishResource", publishResource);
			if(pageServiceResource.getResultDatas().size()>0) {
				//当子服务权重大于分类的权重时，自服务会继承分类下发的资源，当分类资源中存在子服务中指定不可分配的资源时，该不可分配资源显示状态2，不可分配
				if(service.compare(parentService, service)==1) {
					mergeResource(pageObject.getResultDatas(),pageServiceResource.getResultDatas());
			    }else{
			        //TODO 当子服务权重小于分类的权重时
			    }
			}
			ResponseContext.addData(pageObject);
		}else {
			serviceResourceList = SessionContext.getSQLSession().query(PublishResourceResult.class,"selectResourceList", "selectPublishResource", publishResource);
			if(serviceResourceList.size()>0) {
				//当子服务权重大于分类的权重时，自服务会继承分类下发的资源，当分类资源中存在子服务中指定不可分配的资源时，该不可分配资源显示状态2，不可分配
				if(service.compare(parentService, service)==1) {
					mergeResource(resourceList,serviceResourceList);
			    }else{
			        //TODO 当子服务权重小于分类的权重时
			    }
			}
			ResponseContext.addData(resourceList);
		}
	}
	
	private void mergeResource(List<PublishResourceResult> resourceList,List<PublishResourceResult> serviceResourceList) {
		for(PublishResourceResult serviceResource:resourceList) {
    		for(PublishResourceResult typeResource:serviceResourceList) {
	    		if(typeResource.getId().equals(serviceResource.getId())) {
	    			if(serviceResource.getIsChecked()!=3) {
	    				serviceResource.setIsChecked(typeResource.getIsChecked());
	    			}
	    		}
	    	}
    	}
	}
	
	
	@Transaction
    public void syncMenuData(String id,String state) {
		List<DmPublishDataResult> dataResultList = null;
		if("data".equals(state)) {
			dataResultList = SessionContext.getSQLSession().query(DmPublishDataResult.class,"selectResourceData", "selectPublishResourceData",id);
		}else {
			dataResultList = SessionContext.getSQLSession().query(DmPublishDataResult.class,"selectResourceData", "selectPublishResourceModel",id);
		}
		String dataType =dataResultList.get(0).getDataType();
		Map<String ,Object> objectMap = null;
		DmPublishDataResult dmPublishDataResult = null;
		Map<String,List<Map<String,Object>>> map = new HashMap<String,List<Map<String,Object>>>();
        for(int i=0; i<dataResultList.size(); i++) {
        	dmPublishDataResult = dataResultList.get(i);
        	objectMap = SessionContext.getSqlSession().uniqueQuery("select * from "+dmPublishDataResult.getTargetName()+" where "
    			    + ""+dmPublishDataResult.getDataKey()+" = '"+dmPublishDataResult.getDataId()+"'");
	        if (map.containsKey(dmPublishDataResult.getTargetName())) {
	            map.get(dmPublishDataResult.getTargetName()).add(objectMap);
	        }else {
	        	List<Map<String,Object>> tmpList = new ArrayList<Map<String,Object>>();
	            tmpList.add(objectMap);
	            map.put(dmPublishDataResult.getTargetName(), tmpList);
	        }
	    }
        DmService service = SessionContext.getSqlSession().uniqueQuery(DmService.class,"select * from dm_service where id='"+id+"'");
        publishData(service,map,dataType,state);
       
	}
	
	private void publishResourceData(DmResource resource,DmService service, Map<String, List<Map<String, Object>>> map) {
		ResponseEntity<?> responseEntity = callPublishData(map,service.getName());
		Map<String,Object> objectMap = (HashMap<String,Object>)responseEntity.getBody();
		List<String> successObject = (List<String>) objectMap.get("data");
		DmPublishTableData tableData = null;
		for(String string:successObject) {
			tableData  = SessionContext.getSqlSession().uniqueQuery(DmPublishTableData.class,"select * from DM_PUBLISH_TABLE_DATA where RESOURCE_ID ='"+resource.getId()+"' and SERVICE_ID = '"+service.getId()+"' and DATA_ID = '"+string+"'");
		    if(tableData!=null) {
		    	tableData.setIsSuccess(1);
		    	SessionContext.getTableSession().update(setBasicPropertyValues(tableData,false));
		    }
		}
	}
	
	private void publishData(DmService service, Map<String, List<Map<String, Object>>> map,String dataType,String state) {
		ResponseEntity<?> responseEntity = callPublishData(map,service.getName());
		Map<String,Object> objectMap = (HashMap<String,Object>)responseEntity.getBody();
		List<String> successObject = (List<String>) objectMap.get("data");
		DmDataPublishTarget dmDataPublishTarget = null;
		if(successObject!=null&&successObject.size()>0) {
			for(String string:successObject) {
				if("data".equals(state)) {
					dmDataPublishTarget  = SessionContext.getSqlSession().uniqueQuery(DmDataPublishTarget.class,"select * from DM_DATA_PUBLISH_TARGET where OBJECT_ID = '"+service.getId()+"' and REF_DATA_ID = '"+string+"'  and DATA_TYPE !='module' ");
				}else {
					dmDataPublishTarget  = SessionContext.getSqlSession().uniqueQuery(DmDataPublishTarget.class,"select * from DM_DATA_PUBLISH_TARGET where OBJECT_ID = '"+service.getId()+"' and REF_DATA_ID = '"+string+"' and DATA_TYPE='"+dataType+"' ");
				} 
			    if(dmDataPublishTarget!=null) {
			    	dmDataPublishTarget.setIsSuccess(1);
			    	SessionContext.getTableSession().update(setBasicPropertyValues(dmDataPublishTarget,false));
			    }
			}
		   ResponseContext.addData("数据发布成功");
		}
	}
	
	private ResponseEntity<?> callPublishData(Map<String, List<Map<String, Object>>> map, String name) {
		return restTemplate.exchange(new APIServer() {
			@Override
			public String getName() {
				return "加载资源API";
			}
			
			@Override
			public String getUrl() {
				return "http://"+name+"/mapping/data/load";
			}

			@Override
			public HttpMethod getRequestMethod() {
				return HttpMethod.POST;
			}
		    },JSONObject.toJSONString(map),HashMap.class);
	}


	@Transaction
	public void publishResource(DmResourcePublishTarget dmResourcePublishTarget) {
		DmService service = SessionContext.getSqlSession().uniqueQuery(DmService.class,"select * from dm_service where id='"+dmResourcePublishTarget.getObjectId()+"'");
		List<DmService> serviceList = null;
	    if(service.getPid()==null) {
	         serviceList =  SessionContext.getSqlSession().query(DmService.class,"select * from dm_service where pid='"+service.getId()+"'");
	    }
	    List<ResourceAdapters> resourceAdapters = new ArrayList<ResourceAdapters>();
	    List<Resource> resources = ResourceContentService.getResourceContent(dmResourcePublishTarget.getResourceId());
		if(resources.size()==0) {
			ResponseContext.addValidation("id为%s的资源发布失败, 未查询到任何content信息", null,dmResourcePublishTarget.getResourceId());
			return;
		}else {
			resourceAdapters.add(new ResourceAdapters(resources));
		}
		if(serviceList!=null && serviceList.size()>0) {
        	for(DmService dmService:serviceList) {
        		publishTarget(dmService,resourceAdapters);
        	}
	    }else {
        	publishTarget(service,resourceAdapters);
	    }
	}
	
	@Transaction
	public void initResourcesById(String id) {
	   DmService service = SessionContext.getSqlSession().uniqueQuery(DmService.class,"select * from dm_service where id='"+id+"'");
	   List<DmService> serviceList = null;
       if(service.getPid()==null) {
          serviceList =  SessionContext.getSqlSession().query(DmService.class,"select * from dm_service where pid='"+service.getId()+"'");
       }
       List<ResourceAdapters> resourceAdapters = new ArrayList<ResourceAdapters>();
       if(serviceList!=null && serviceList.size()>0) {
        	for(DmService dmService:serviceList) {
        		//封装发布对象
        		montageObject(id,dmService,resourceAdapters);
        		//发布资源
        		publishTarget(dmService,resourceAdapters);
        	}
       }else {
    	    montageObject(id,service,resourceAdapters);
        	publishTarget(service,resourceAdapters);
       }
	}
	
	
	private void montageObject(String id,DmService dmService,List<ResourceAdapters> resourceAdapters ) {
		List<DmResourcePublishTarget> resourcesPublishList = SessionContext.getSQLSession().query(DmResourcePublishTarget.class,"selectResourceList", "selectChildServiceMontageResources",dmService);
		if(resourcesPublishList.size()==0) {
			ResponseContext.addValidation("id为%s的资源未指定发布目标系统", null, id);
		}else {
			DmResource dmResource = null;
	        List<DmResource> dmResourceList  = new ArrayList<DmResource>();
	        for(DmResourcePublishTarget DmResourcePublishTarget:resourcesPublishList) {
	    	    dmResource = SessionContext.getSqlSession().uniqueQuery(DmResource.class,"select * from dm_resource where id = '"+DmResourcePublishTarget.getResourceId()+"'");
	    	    dmResourceList.add(dmResource);
	        }
	        for(DmResource resource:dmResourceList) {
	        	List<Resource> resources = ResourceContentService.getResourceContent(resource.getId());
	    		if(resources.size()==0) {
	    			ResponseContext.addValidation("id为%s的资源发布失败, 未查询到任何content信息", null, resource.getId());
	    		}else {
	    			resourceAdapters.add(new ResourceAdapters(resources));
	    		}
		    }
		}
	}
	
	public void publishTarget(DmService dmService,List<ResourceAdapters> resourceAdapters){
		 ResponseEntity<?> responseEntity = callCreateResources(resourceAdapters,dmService.getName());
		 Map<String,Object> map = (HashMap<String,Object>)responseEntity.getBody();
		 List<String> successIds = (List<String>) map.get("data");
		 List<Map<String,Object>> validationData = (List<Map<String,Object>>)map.get("validation");
		 DmPublishState dmPublishState  = null;
		 if(validationData!=null&&validationData.size()>0) {
			 for(Map<String,Object> vailMap:validationData) {
				 saveErrorPublishState(dmService,2,vailMap);
			 }
		 }
		 if(successIds!=null&&successIds.size()>0) {
			 for(String resourceId:successIds) {
				 savePublishState(resourceId,dmService,1);
			     //savePublishTableData(resourceId,dmService);
			 }
			 ResponseContext.addData("资源发布成功"); 
		 }
	}

	private void savePublishTableData(String resourceId, DmService dmService) {
		List<DmPublishTableData> tableDataList = SessionContext.getSqlSession().query(DmPublishTableData.class,"select * from DM_PUBLISH_TABLE_DATA where RESOURCE_ID ='"+resourceId+"' and SERVICE_ID = '"+dmService.getId()+"'");
	    DmResource resource =  SessionContext.getSqlSession().uniqueQuery(DmResource.class,"\r\n" + 
	    		"select * from DM_RESOURCE where id='"+resourceId+"'");
	    Map<String,List<Map<String,Object>>> map = new HashMap<String,List<Map<String,Object>>>();
		Map<String ,Object> objectMap = null;
		List<Map<String,Object>> tmpList = new ArrayList<Map<String,Object>>();
	    for(DmPublishTableData tableData:tableDataList) {
	    	objectMap = SessionContext.getSqlSession().uniqueQuery("select * from "+resource.getResourceName()+" where id = '"+tableData.getDataId()+"'");
	    	tmpList.add(objectMap);
	    }
	    map.put(resource.getResourceName(),tmpList);
	    publishResourceData(resource,dmService,map);
	}

	private void savePublishState(String id,DmService dmService,int IsSuccess) {
		DmPublishState dmPublishState = SessionContext.getSqlSession().uniqueQuery(DmPublishState.class, "select * from DM_PUBLISH_STATE where RESOURCE_ID = '"+id+"' and SERVICE_ID = '"+dmService.getId()+"'");
		String msg = "资源发布成功";
		if(dmPublishState==null) {
		    dmPublishState = new DmPublishState(dmService.getId(),IsSuccess,id,msg);
			SessionContext.getTableSession().save(setBasicPropertyValues(dmPublishState,true));
	    }else {
	   		dmPublishState.setIsSuccess(IsSuccess);
	   		dmPublishState.setErrorMessage(msg);
	   		SessionContext.getTableSession().update(setBasicPropertyValues(dmPublishState,false));
	    }
	}
	
	private DmPublishState saveErrorPublishState(DmService dmService,int IsSuccess,Map<String,Object> vailMap) {
		Map<String,Object> dataMap = (Map<String, Object>) vailMap.get("data");
		DmPublishState dmPublishState = SessionContext.getSqlSession().uniqueQuery(DmPublishState.class, "select * from DM_PUBLISH_STATE where RESOURCE_ID = '"+(String)dataMap.get("id")+"' and SERVICE_ID = '"+dmService.getId()+"'");
		String msg = (String) vailMap.get("message");
		if(dmPublishState==null) {
		    dmPublishState = new DmPublishState(dmService.getId(),IsSuccess,(String)dataMap.get("id"),msg);
			SessionContext.getTableSession().save(setBasicPropertyValues(dmPublishState,true));
	    }else {
	   		dmPublishState.setIsSuccess(IsSuccess);
	   		dmPublishState.setErrorMessage(msg);
	   		SessionContext.getTableSession().update(setBasicPropertyValues(dmPublishState,false));
	    }
		return dmPublishState;
	}
	
   @Transaction(propagationBehavior=PropagationBehavior.REQUIRED_NEW)
   private ResponseEntity<?> callCreateResources(List<ResourceAdapters> publishResourceAdapters, String serviceName) {
		return restTemplate.exchange(new APIServer() {
			@Override
			public String getName() {
				return "加载资源API";
			}
			
			@Override
			public String getUrl() {
				return "http://"+serviceName+"/mapping/load";
			}

			@Override
			public HttpMethod getRequestMethod() {
				return HttpMethod.POST;
			}
		    }, JSONObject.toJSONString(publishResourceAdapters),HashMap.class);
	  }

}


