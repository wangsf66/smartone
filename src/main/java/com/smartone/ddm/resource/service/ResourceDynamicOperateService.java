package com.smartone.ddm.resource.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.SessionFactoryContainer;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.mapping.MappingProperty;
import com.douglei.orm.mapping.MappingTypeNameConstants;
import com.douglei.orm.mapping.handler.MappingHandler;
import com.douglei.orm.sessionfactory.sessions.session.sql.ExecutableSqlEntity;
import com.douglei.orm.sessionfactory.sessions.session.sql.purpose.QueryPurposeEntity;
import com.ibs.code.result.DataValidationResult;
import com.ibs.code.service.BasicService;
import com.ibs.components.filters.param.parser.ParamParserFilter;
import com.ibs.components.filters.param.parser.RequestParameter;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.query.builtin.conditions.MethodContext;
import com.smartone.ddm.resource.entity.DmResourceMapping;
import com.smartone.ddm.util.ProcedurePrefixUtil;
/**
 * 
 * @author wangShuFang
 */
@TransactionComponent
public class ResourceDynamicOperateService extends BasicService {
	
	public static final String procedurePrefix = "CALL_";

	@Transaction
	public void dynamicQuery(HttpServletRequest request, String resourceName,boolean isBuilt) {
		DmResourceMapping dmResourceMapping = SessionContext.getTableSession().uniqueQuery(DmResourceMapping.class, "select * from DM_RESOURCE_MAPPING where RESOURCE_NAME = '"+resourceName+"'");
			if(dmResourceMapping!=null&&dmResourceMapping.getIsFailure()==1) {
				ResponseContext.addValidationFull(resourceName, "resourceName", "资源已失效，不可被调用","smartone.dynamic.resource.tableResourceIsFailure");
		        return;
			}
		RequestParameter res = (RequestParameter)request.getAttribute(ParamParserFilter.REQUEST_BODY_KEY);
		MethodContext methodContext= null;
		List<String> removeParamsList = null;
		MappingHandler mappingHandler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
		MappingProperty  mappingProperty = null;
		try {
			if(isBuilt) {	
				methodContext = new MethodContext(res.getBuiltin(),res.getResource(),res.getParentResource(),resourceName);
			}else if(mappingHandler.exists(resourceName)) {
				mappingProperty = mappingHandler.getMappingProperty(resourceName);
				if(MappingTypeNameConstants.TABLE.equals(mappingProperty.getType())){
					methodContext = new MethodContext(res.getBuiltin(),res.getResource(),res.getParentResource(),resourceName);
				}else{
					ExecutableSqlEntity es = mappingHandler.getExecutableSqlEntity(QueryPurposeEntity.getSingleton(), resourceName, null,res.getResource());
					JSONObject extend = JSONObject.parseObject(mappingProperty.getExtend());
					JSONArray params = extend.getJSONArray("params");
					if(params!=null && params.size()>0) {
						for(int i=0;i<params.size();i++) {
							for (Map.Entry<String, Object> entry : res.getResource().entrySet()) {
								if(params.get(i).equals(entry.getKey())) {
									if(removeParamsList==null) {
										removeParamsList = new ArrayList<String>();
										removeParamsList.add(entry.getKey());
									}else {
									    removeParamsList.add(entry.getKey());
									}
								}               
							}
						}
					}
				  if(removeParamsList!=null) {
					  for(String str:removeParamsList) {
						  res.getResource().remove(str);
					  } 
				  }
				  methodContext = new MethodContext(res.getBuiltin(),res.getResource(),res.getParentResource(),es);
				}
			}else {
				ResponseContext.addValidationFull(resourceName, "resurceName", "该资源不存在","smartone.dynamic.resource.notExists");
			}
			ResponseContext.addData(methodContext.getDatas());
		} catch (Exception e) {
			e.printStackTrace();
			ResponseContext.addError(null,e);
			SessionContext.executeRollback();
		}
	}
	
	 @Transaction
	 public void insertMany(String resourceName, List<Map<String, Object>> maplist) {
		 DmResourceMapping dmResourceMapping = SessionContext.getTableSession().uniqueQuery(DmResourceMapping.class, "select * from DM_RESOURCE_MAPPING where RESOURCE_NAME = '"+resourceName+"'");
			if(dmResourceMapping!=null&&dmResourceMapping.getIsFailure()==1) {
				ResponseContext.addValidationFull(resourceName, "resourceName", "资源已失效，不可被调用","smartone.dynamic.resource.tableResourceIsFailure");
				return;
			}
		 if(validateByValidator(maplist) == DataValidationResult.SUCCESS) {
			 MappingHandler mappingHandler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
			 for(Map<String, Object> map:maplist) {
				 if(map.get("ID")==null) {
					map.put("ID", UUID.randomUUID().toString());
				 }
				 Boolean isExists = mappingHandler.exists(resourceName);
				 if(!isExists) {
					 ResponseContext.addValidationFull(resourceName, "resurceName", "该资源不存在","smartone.dynamic.resource.notExists");
					 return;
				 }
				 MappingProperty property = mappingHandler.getMappingProperty(resourceName);
				 if(!(MappingTypeNameConstants.TABLE.equals(property.getType()))) {
					 ResponseContext.addValidationFull(resourceName, "resurceName", "接口调用错误，SQL语句请调用operate方法","smartone.dynamic.resource.sqlErroeInterface");
					 return;
				 }
				 tableSessionSave(resourceName,map); 	
			 }
		 }
	 }

	@Transaction
	public void updateMany(String resourceName, List<Map<String, Object>> maplist) {
		DmResourceMapping dmResourceMapping = SessionContext.getTableSession().uniqueQuery(DmResourceMapping.class, "select * from DM_RESOURCE_MAPPING where RESOURCE_NAME = '"+resourceName+"'");
		if(dmResourceMapping!=null&&dmResourceMapping.getIsFailure()==1) {
			ResponseContext.addValidationFull(resourceName, "resourceName", "资源已失效，不可被调用","smartone.dynamic.resource.tableResourceIsFailure");
			return;
		}
		if(validateByValidator(maplist) == DataValidationResult.SUCCESS) {
			 MappingHandler mappingHandler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
			 for(Map<String, Object> map :maplist) {
				   for (Map.Entry<String, Object> entry : map.entrySet()) {
					   if("isNull".equals(entry.getValue())) {
						   map.put(entry.getKey(),null);
					   }
				    }
			 }
			 Boolean isExists = mappingHandler.exists(resourceName);
			 if(!isExists) {
				 ResponseContext.addValidationFull(resourceName, "resourceName", "该资源不存在","smartone.dynamic.resource.notExists");
				 return;
			 }
			 MappingProperty property = mappingHandler.getMappingProperty(resourceName);
			 if(!(MappingTypeNameConstants.TABLE.equals(property.getType()))) {
				 ResponseContext.addValidationFull(resourceName, "resourceName", "接口调用错误，SQL语句请调用operate方法","smartone.dynamic.resource.sqlErroeInterface");
				 return;
			 }
			 tableSessionUpdate(resourceName,maplist, true);
		 }
     }
	
	@Transaction
	public void deleteObject(String resourceName, String ids) {
		DmResourceMapping dmResourceMapping = SessionContext.getTableSession().uniqueQuery(DmResourceMapping.class, "select * from DM_RESOURCE_MAPPING where RESOURCE_NAME = '"+resourceName+"'");
		if(dmResourceMapping!=null&&dmResourceMapping.getIsFailure()==1) {
			ResponseContext.addValidationFull(resourceName, "resourceName", "资源已失效，不可被调用","smartone.dynamic.resource.tableResourceIsFailure");
			return;
		}
		Boolean isExists =SessionFactoryContainer.getSingleton().get().getMappingHandler().exists(resourceName);
		if(isExists) {
			deleteByIds(resourceName,"ID",ids);
		}
	}
	
	@Transaction
	public void operateMany(String resourceName, List<Map<String, Object>> maplist, int sqlType) {
		for(Map<String, Object> map :maplist) {
		   for (Map.Entry<String, Object> entry : map.entrySet()) {
			   if("isNull".equals(entry.getValue())) {
				   map.put(entry.getKey(),null);
			   }
		    }
		}
		if(sqlType==1) {
			sqlSessionExecuteProcedure(ProcedurePrefixUtil.CALL+resourceName, null, maplist);   
		}else {
			sqlSessionExecuteUpdate(resourceName, null, maplist);
		}		
	}
}
