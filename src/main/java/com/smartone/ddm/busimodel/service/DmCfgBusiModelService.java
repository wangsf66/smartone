package com.smartone.ddm.busimodel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.SessionFactoryContainer;
import com.douglei.orm.context.SimpleSessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.mapping.Mapping;
import com.douglei.orm.mapping.MappingProperty;
import com.douglei.orm.mapping.handler.MappingHandleException;
import com.douglei.orm.mapping.handler.MappingHandler;
import com.douglei.orm.mapping.handler.entity.AddOrCoverMappingEntity;
import com.douglei.orm.mapping.handler.entity.DeleteMappingEntity;
import com.douglei.orm.sessionfactory.sessions.Session;
import com.douglei.orm.sessionfactory.sessions.session.sql.ExecutableSqlEntity;
import com.douglei.orm.sessionfactory.sessions.session.sql.purpose.QueryPurposeEntity;
import com.ibs.code.result.DataValidationResult;
import com.ibs.code.service.BasicService;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.busimodel.entity.DmBusiModelRelation;
import com.smartone.ddm.busimodel.entity.DmSqlBusiStructure;
import com.smartone.ddm.query.builtin.conditions.MethodContext;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourceMapping;
import com.smartone.ddm.resource.entity.Resource;
import com.smartone.ddm.resource.service.ResourceContentService;
import com.smartone.ddm.util.ProcedurePrefixUtil;
import com.smartone.ddm.util.ResourceTypeUtil;
import com.smartone.ddm.util.StrUtil;
import com.smartone.ex.mapping.ExMappingTypeConstants;
import com.smartone.ex.mapping.busimodel.metadata.BusiModelMetadata;
import com.smartone.ex.mapping.busimodel.metadata.RelationMetadata;
import com.smartone.ex.mapping.busimodel.metadata.SqlMetadata;
/**
 * 
 * @author wangShuFang
 */
public class DmCfgBusiModelService extends BasicService {
	
	public static final String STATE = "$state$";
	public static final String DELETE = "delete";
	public static final String UPDATE = "update";
	public static final String INSERT = "insert";
	public static final String SELE_DELE = "sele_dele";
	public static final String SELECT = "select";
	
	
	@Autowired
	private ResourceContentService ResourceContentService;


	@Transaction
	public void insertMany(List<DmResource> list) {
		for(DmResource dmResource:list) {
			if (doValidate(dmResource)) {
			   dmResource.setResourceType(ResourceTypeUtil.RESOURCE_TYPE_BUSIMODEL);
			}
		}
		tableSessionSave(list);
    }
	
	@Transaction
	public void updateMany(List<DmResource> list) {
		for(DmResource dmResource:list) {
			dmResource.setResourceType(ResourceTypeUtil.RESOURCE_TYPE_BUSIMODEL);
			updateCfgBusiModel(dmResource);
		}
	}
	
	public void updateCfgBusiModel(DmResource dmResource){
		DmResource oldDmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE WHERE ID='"+dmResource.getId()+"'");
		dmResource.setIsEverBuildModel(oldDmResource.getIsEverBuildModel());
		if(oldDmResource.getIsBuildModel()==1) {
			if(oldDmResource!=null) {
				if(oldDmResource.getResourceName().equals(dmResource.getResourceName())) {
					dmResource.setIsBuildModel(oldDmResource.getIsBuildModel());
					dmResource.setIsEverBuildModel(oldDmResource.getIsEverBuildModel());
					tableSessionUpdate(dmResource);
				}else {
					if(oldDmResource.getIsEverBuildModel()==1&&oldDmResource.getOldResourceName()==null) {
						dmResource.setOldResourceName(oldDmResource.getResourceName());
					}
			    	if(doValidate(dmResource)) {
			    		dmResource.setIsBuildModel(0);
			    		tableSessionUpdate(dmResource);
					}
			    	try {
						cancelmodel(oldDmResource);
					} catch (MappingHandleException e) {
						e.printStackTrace();
					}
				 }
			 }
		}else {
			if(doValidate(dmResource)) {
	    		dmResource.setIsBuildModel(0);
	    		tableSessionUpdate(dmResource);
			}	
		}
    }

	@Transaction
	public void delete(String ids) {
		List<DmResource> list= SessionContext.getSqlSession().query(DmResource.class,
				"select * from DM_RESOURCE where ID in (" +StrUtil.stringToSql(ids)+ ")");
		List<DmBusiModelRelation> relationList = null;
		List<DmSqlBusiStructure> structureList = null;
		if(list!=null&&list.size()>0) {
			for (DmResource dmResource : list) {
				if (dmResource.getIsBuildModel() == 1) {
					ResponseContext.addValidationFull(dmResource, "id", "该业务对象已建模，请取消建模后再做删除操作", "smartone.dynamic.business.CancelModel");
				    return;
				}
				relationList = SessionContext.getSqlSession().query(DmBusiModelRelation.class,"select * from DM_CFG_BUSI_MODEL_RES_RELATIONS where REF_BUSI_MODEL_ID = '" + dmResource.getId() + "'");
				if(relationList!=null&&relationList.size()>0) {
					for(DmBusiModelRelation relation:relationList) {
						if(relation.getRefResourceType()<=ResourceTypeUtil.RESOURCE_TYPE_SQL||relation.getRefResourceType()>ResourceTypeUtil.RESOURCE_TYPE_TABLE) {
							structureList = SessionContext.getSqlSession().query(DmSqlBusiStructure.class,"select * from DM_SQL_BUSI_STRUCTURE where PARENT_ID = '"+relation.getId()+"'");   
						       if(structureList!=null&&structureList.size()>0) {
						    	   for(DmSqlBusiStructure structure:structureList) {
						    		   SessionContext.getSqlSession().executeUpdate("delete from DM_SQL_BUSI_STRUCTURE where ID='"+structure.getId()+"'");
						    	   }
						       }
						   }
						SessionContext.getSqlSession().executeUpdate("delete from DM_CFG_BUSI_MODEL_RES_RELATIONS where ID='"+relation.getId()+"'");
					}
				}
				SessionContext.getSqlSession().executeUpdate("delete from DM_RESOURCE where ID='"+dmResource.getId()+"'");
			}
		}
		Map<String,Object> idsMap = new HashMap<String,Object>();
		idsMap.put("ids", ids);
		ResponseContext.addData(idsMap);  
	}
	
	public void query(Map<String, Object> map ,String resourceName,String isRecursive) {
		Session session = SimpleSessionContext.getSession();
		MappingHandler mappingHandler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
		Boolean isExists = mappingHandler.exists(resourceName);
		Mapping mapping = null;
		BusiModelMetadata bm = null;
		MappingProperty  mappingProperty = null;
		try {
			if(!isExists) {
				 ResponseContext.addValidationFull(resourceName, "resourceName", "该资源不存在","smartone.dynamic.resource.notExists");
				 return;
			 }
			mapping =  mappingHandler.getMapping(resourceName,ExMappingTypeConstants.BUSIMODEL, true);
		    bm = (BusiModelMetadata)mapping.getMetadata();
		    mappingProperty = mappingHandler.getMappingProperty(resourceName);
		    Object mapList = null;
		    if(isRecursive!=null&&"false".equals(isRecursive)) {
		    	mapList= queryNodeReportForm(bm.getRelations(), map, session,mappingProperty);
		    }else {
		    	mapList= queryNode(bm.getRelations(), map, session,mappingProperty);
		    }
			session.commit();
			ResponseContext.addData(mapList);
		} catch (Exception e) {
			e.printStackTrace();
			session.rollback();
			ResponseContext.addError(map, e);
		}finally {
			session.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	private Object queryNodeReportForm(List<RelationMetadata> treeList, Map<String, Object> map, Session session,MappingProperty  mappingProperty) {
		Map<String,List<Map<String,Object>>> dsbsResultMap = new HashMap<String,List<Map<String,Object>>>();
		List<Map<String,Object>> dsbsResultList = null;
		MethodContext methodContext  = null;
		//存放条件的中间map，使用前先进性清空操作
		Map<String, Object> tempMap = new HashMap<String, Object>();
		//内置条件map。如排序
		Map<String, Object> builtinTempMap = new HashMap<String, Object>();
		MappingHandler mappingHandler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
		List<SqlMetadata> dsbsList = null;
		for(RelationMetadata relation : treeList) { 
			if ((ResourceTypeUtil.RESOURCE_TYPE_TABLE+"").equals(relation.getRefResourceType())) {
				//查询table
				methodContext = new MethodContext(returnMap(tempMap,map,relation.getRefResourceKeyName()),returnSortMap(builtinTempMap,map,relation.getRefResourceKeyName()),relation.getRefResourceName());
				dsbsResultList = (List<Map<String,Object>>)methodContext.getBusiDatas(session);
				dsbsResultMap.put(relation.getRefResourceKeyName(),dsbsResultList);
				if(dsbsResultList!=null&&dsbsResultList.size()>0) {
					querychildReportForm(dsbsResultList,session,relation,map); 
			    }
			}else{
				dsbsList = relation.getSqlList();
				if(dsbsList!=null&&dsbsList.size()>0) {
				for(SqlMetadata sqlMetadata:dsbsList) {
					if(sqlMetadata.getType().equals(SELECT)) {
						//解析查询语句以及参数
						ExecutableSqlEntity es = mappingHandler.getExecutableSqlEntity(QueryPurposeEntity.getSingleton(), sqlMetadata.getName(), null,returnMap(tempMap,map));
						JSONObject extend = JSONObject.parseObject(mappingProperty.getExtend());
						JSONArray params = null;
						if(extend!=null) {
							 params = extend.getJSONArray("params");
						}
						returnParams(params,map);
						methodContext = new MethodContext(returnMap(tempMap,map,relation.getRefResourceKeyName()),returnSortMap(builtinTempMap,map,relation.getRefResourceKeyName()),es);
						dsbsResultList = (List<Map<String,Object>>)methodContext.getBusiDatas(session);
						dsbsResultMap.put(relation.getRefResourceKeyName(), dsbsResultList);
						if (dsbsResultList!=null&&dsbsResultList.size()>0) {
							querychildReportForm(dsbsResultList,session,relation,map);
					    }
					 }
				   }
				}
			}
		}
	    return dsbsResultMap;
	}
	
	@SuppressWarnings("unchecked")
	private void querychildReportForm(List<Map<String, Object>> dsbsResultList, Session session,
			RelationMetadata relation,Map<String, Object> map) {
		MethodContext methodContext  = null;
		List<Map<String,Object>> dsbsChildList = null;
		Map<String, Object> tempMap = new HashMap<String, Object>();
		Map<String, Object> builtinTempMap = new HashMap<String, Object>();
		Map<String, Object> childMap = null;
		Object obj = null;
		List<SqlMetadata> dsbsList = null;
		MappingHandler mappingHandler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
		if(relation.getChildrenList()!=null&&relation.getChildrenList().size()>0) {
			for(RelationMetadata childRelation : relation.getChildrenList()) {
				//如果map为空，就初始化一个 
				obj = map.get(childRelation.getRefResourceKeyName());
				if(obj==null) {
					obj = new HashMap<String, Object>();
					childMap =(Map<String, Object>)obj;
				}else {
					childMap =(Map<String, Object>)obj;
				}
				for(Map<String, Object> childmap:dsbsResultList) {
					    //将上一层的隐形条件放到下一层中
						childMap.put(childRelation.getRefParentResourcePropId(),childmap.get(childRelation.getIdPropName()));
					if((ResourceTypeUtil.RESOURCE_TYPE_TABLE+"").equals(childRelation.getRefResourceType())) {
						methodContext = new MethodContext(returnMap(tempMap,childMap,childRelation.getRefResourceKeyName()),returnSortMap(builtinTempMap,map,childRelation.getRefResourceKeyName()),childRelation.getRefResourceName());
						dsbsChildList = (List<Map<String,Object>>)methodContext.getBusiDatas(session);
						childmap.put(childRelation.getRefResourceKeyName(),dsbsChildList);
					}else {
						dsbsList = childRelation.getSqlList();
						if(dsbsList!=null&&dsbsList.size()>0){
						for(SqlMetadata sqlMetadata:dsbsList) {
							if(sqlMetadata.getType().equals(SELECT)) {
								ExecutableSqlEntity es = mappingHandler.getExecutableSqlEntity(QueryPurposeEntity.getSingleton(), sqlMetadata.getName(), null,returnMap(tempMap,childMap));
								methodContext = new MethodContext(returnMap(tempMap,childMap,childRelation.getRefResourceKeyName()),returnSortMap(builtinTempMap,map,childRelation.getRefResourceKeyName()),es);
								dsbsChildList = (List<Map<String,Object>>)methodContext.getBusiDatas(session);
								childmap.put(childRelation.getRefResourceKeyName(), dsbsChildList);
							 }
						  }
					    }
					}
					if(childRelation.getChildrenList()!=null&&dsbsChildList.size()>0) {
						querychild(dsbsChildList,session,childRelation,childMap);
					}
				} 
			}   
		}
	}
	
	private Map<String, Object> returnMap(Map<String, Object> tempMap,Map<String, Object> map,String keyName){
		tempMap.clear();
		if(map!=null&&map.size()>0) {
			for(Map.Entry<String, Object> entry : map.entrySet()){
	            String key = entry.getKey();
	            Object value = entry.getValue();
	            if(!(value instanceof java.util.LinkedHashMap)&& value!=null && key!=null) {
	            	if(key.contains(".")) {
	            		if(key.split("\\.")[0].equals(keyName)) {
	            			if(!key.split("\\.")[1].startsWith("_")) {
	            				tempMap.put(key.split("\\.")[1], value);
	            			}
		 	            }
	            	}else {
	            		if(!key.startsWith("_")) {
	            			tempMap.put(key,value);
	            		}
	            	}
	            }
	        }
		}
	    return tempMap;
	}
	
	//报表业务数据的排序
	private Map<String, Object> returnSortMap(Map<String, Object> builtinTempMap,Map<String, Object> map,String keyName){
		builtinTempMap.clear();
		if(map!=null&&map.size()>0) {
			for(Map.Entry<String, Object> entry : map.entrySet()){
	            String key = entry.getKey();
	            Object value = entry.getValue();
	            if(!(value instanceof java.util.LinkedHashMap)&& value!=null && key!=null) {
	            	if(key.contains(".")) {
	            		if(key.split("\\.")[0].equals(keyName)) {
	            			if(key.split("\\.")[1].startsWith("_")) {
	            				builtinTempMap.put(key.split("\\.")[1], value);
	            			}
		 	            }
	            	}else {
	            		if(key.startsWith("_")) {
	            			builtinTempMap.put(key,value);
	            		}
	            	}
	            }
	        }
		}
	    return builtinTempMap;
	} 
	
	 
	
	//查询数据
	@SuppressWarnings("unchecked")
	private Object queryNode(List<RelationMetadata> treeList, Map<String, Object> map, Session session,MappingProperty  mappingProperty) {
		List<Map<String,Object>> dsbsResultList = null;
		MethodContext methodContext  = null;
		//存放条件的中间map，使用前先进性清空操作
		Map<String, Object> tempMap = new HashMap<String, Object>();
		MappingHandler mappingHandler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
		List<SqlMetadata> dsbsList = null;
		for(RelationMetadata relation : treeList) { 
			if ((ResourceTypeUtil.RESOURCE_TYPE_TABLE+"").equals(relation.getRefResourceType())) {
				//查询table
				methodContext = new MethodContext(returnMap(tempMap,map),relation.getRefResourceName());
				dsbsResultList = (List<Map<String,Object>>)methodContext.getBusiDatas(session);
				if(dsbsResultList!=null&&dsbsResultList.size()>0) {
			    	querychild(dsbsResultList,session,relation,map); 
			    }
			}else{
				dsbsList = relation.getSqlList();
				if(dsbsList!=null&&dsbsList.size()>0) {
				for(SqlMetadata sqlMetadata:dsbsList) {
					if(sqlMetadata.getType().equals(SELECT)) {
						//解析查询语句以及参数
						ExecutableSqlEntity es = mappingHandler.getExecutableSqlEntity(QueryPurposeEntity.getSingleton(), sqlMetadata.getName(), null,returnMap(tempMap,map));
						JSONObject extend = JSONObject.parseObject(mappingProperty.getExtend());
						JSONArray params = null;
						if(extend!=null) {
							 params = extend.getJSONArray("params");
						}
						returnParams(params,map);
						methodContext = new MethodContext(returnMap(tempMap,map),es);
						dsbsResultList = (List<Map<String,Object>>)methodContext.getBusiDatas(session);
						if (dsbsResultList!=null&&dsbsResultList.size()>0) {
							querychild(dsbsResultList,session,relation,map);
					    }
					 }
				   }
				}
			}
		}
		return dsbsResultList;
	}
	 
	
	@SuppressWarnings("unchecked")
	private void querychild(List<Map<String, Object>> dsbsResultList, Session session,RelationMetadata relation,Map<String, Object> map) {
		MethodContext methodContext  = null;
		List<Map<String,Object>> dsbsChildList = null;
		Map<String, Object> tempMap = new HashMap<String, Object>();
		Map<String, Object> childMap = null;
		Object obj = null;
		List<SqlMetadata> dsbsList = null;
		MappingHandler mappingHandler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
		if(relation.getChildrenList()!=null&&relation.getChildrenList().size()>0) {
			for(RelationMetadata childRelation : relation.getChildrenList()) {
				//如果map为空，就初始化一个 
				obj = map.get(childRelation.getRefResourceKeyName());
				if(obj==null) {
					obj = new HashMap<String, Object>();
					childMap =(Map<String, Object>)obj;
				}else {
					childMap =(Map<String, Object>)obj;
				}
				for(Map<String, Object> childmap:dsbsResultList) {
					    //将上一层的隐形条件放到下一层中
						childMap.put(childRelation.getRefParentResourcePropId(),childmap.get(childRelation.getIdPropName()));
					if((ResourceTypeUtil.RESOURCE_TYPE_TABLE+"").equals(childRelation.getRefResourceType())) {
						methodContext = new MethodContext(returnMap(tempMap,childMap),childRelation.getRefResourceName());
						dsbsChildList = (List<Map<String,Object>>)methodContext.getBusiDatas(session);
						childmap.put(childRelation.getRefResourceKeyName(),dsbsChildList);
					}else {
						dsbsList = childRelation.getSqlList();
						if(dsbsList!=null&&dsbsList.size()>0){
						for(SqlMetadata sqlMetadata:dsbsList) {
							if(sqlMetadata.getType().equals(SELECT)) {
								ExecutableSqlEntity es = mappingHandler.getExecutableSqlEntity(QueryPurposeEntity.getSingleton(), sqlMetadata.getName(), null,returnMap(tempMap,childMap));
								methodContext = new MethodContext(returnMap(tempMap,childMap),es);
								dsbsChildList = (List<Map<String,Object>>)methodContext.getBusiDatas(session);
								childmap.put(childRelation.getRefResourceKeyName(), dsbsChildList);
							 }
						   }
					    }
					}
					//递归查询
					if(childRelation.getChildrenList()!=null&&dsbsChildList.size()>0) {
						querychild(dsbsChildList,session,childRelation,childMap);
					}
				} 
			}   
		}
	}
	

	
	
	private void returnParams(JSONArray params, Map<String, Object> map) {
		 if(params!=null && params.size()>0) {
			   for(int i=0;i<params.size();i++) {
				   for (Map.Entry<String, Object> entry : map.entrySet()) {
					   if(params.get(i).equals(entry.getKey())) {
						   map.remove(entry.getKey());
					   }
					   if(i==params.size()-1) {
						  break; 
					   }
				   }
		       }
		   }	  
	}

	//在条件map中，有属性和数组类型的值，将属性作为条件进行查询，用中间map接受
	private Map<String, Object> returnMap(Map<String, Object> tempMap,Map<String, Object> map){
		tempMap.clear();
		if(map!=null&&map.size()>0) {
			for(Map.Entry<String, Object> entry : map.entrySet()){
	            String key = entry.getKey();
	            Object value = entry.getValue();
	            if(!(value instanceof java.util.LinkedHashMap)&& value!=null && key!=null) {
	            	  tempMap.put(key, value);
	            }
	        }
		}
	    return tempMap;
	}
	
	public void batchBusinessOperate(String nameSpace, List<Map<String, Object>> mapList,boolean isBatch) {
    	Session session = SimpleSessionContext.getSession();
    	MappingHandler mappingHandler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
    	boolean isExists = mappingHandler.exists(nameSpace);
    	BusiModelMetadata bm = null;
		Mapping mapping = null;
		try {
			if(!isExists) {
				ResponseContext.addValidationFull(nameSpace, "nameSpace", "该资源不存在","smartone.dynamic.resource.notExists");
				return;
			}
			mapping =  mappingHandler.getMapping(nameSpace,ExMappingTypeConstants.BUSIMODEL,true);
			bm = (BusiModelMetadata)mapping.getMetadata();
			// 递归验证数据
			for(Map<String, Object> map :mapList) {
				validationNode(bm.getRelations(), map, session);
			}
			// 递归保存数据
			for(Map<String, Object> map :mapList) {
				operateNode(bm.getRelations(), map, session,mappingHandler);
			}
			session.commit();
			if(isBatch) {
				ResponseContext.addData(mapList);
			}else {
				ResponseContext.addData(mapList.get(0));
			}
		} catch (Exception e) {
			e.printStackTrace();
			session.rollback();
			ResponseContext.addError(mapList, e);
		} finally {
			session.close();
		}
	}
	
	//验证数据
	private void validationNode(List<RelationMetadata> treeList, Map<String, Object> map, Session session) {
		for (RelationMetadata rm : treeList) {
			if(validateByValidator(map) == DataValidationResult.SUCCESS) {
				if(map.get(STATE).equals(DELETE)&&"1".equals(rm.getIsCascadeDelete())) {
					List<RelationMetadata> list = rm.getChildrenList();
					if(list.size()>0) {
						for(RelationMetadata dm :list) {
							map.remove(dm.getRefResourceKeyName());
						}
					}
				}else {
					if (rm.getChildrenList() != null ) {
						validationChild(rm, map, session);
					}
				}
			}
		}
	}
	
	//验证子数据
    @SuppressWarnings("unchecked")
	private void validationChild(RelationMetadata dcbmrr, Map<String, Object> map, Session session) {
		for (RelationMetadata relationMetadata : dcbmrr.getChildrenList()) {
			List<Map<String, Object>> childMapList = null;
			Object obj = map.get(relationMetadata.getRefResourceKeyName());
			if(obj  instanceof  JSONArray ) {
				childMapList = (List<Map<String, Object>>)obj;
			}else{
				childMapList = new ArrayList<Map<String, Object>>();
			    Map<String, Object> childmap = (Map<String, Object>)obj;
			    childMapList.add(childmap);
			}
			if(childMapList!=null && childMapList.size()>0) {
				for (Map<String, Object> childmap : childMapList) {
					if(validateByValidator(childmap) == DataValidationResult.SUCCESS) {
						if(childmap.get(STATE).equals(DELETE)&&"1".equals(relationMetadata.getIsCascadeDelete())) {
							List<RelationMetadata> list = relationMetadata.getChildrenList();
							if(list!=null&&list.size()>0) {
								for(RelationMetadata dm :list) {
									childmap.remove(dm.getRefResourceKeyName());
								}
							}
						}else {
							if (relationMetadata.getChildrenList() != null && childmap.get(relationMetadata.getRefResourceKeyName())!=null ) {
								validationChild(relationMetadata, childmap, session);
							}	
						}
					}
	   			}
			}
		}
	}

	// 操作结点信息
	private void operateNode(List<RelationMetadata> treeList, Map<String, Object> map, Session session,MappingHandler mappingHandler) {
		List<SqlMetadata> dsbsList = null;
		for (RelationMetadata relation : treeList) {
			if ((ResourceTypeUtil.RESOURCE_TYPE_TABLE+"").equals(relation.getRefResourceType())) {
				if (map.get(STATE).equals(INSERT)) {
					session.getTableSession().save(relation.getRefResourceName(), (Map<String, Object>) setBasicPropertyValues(map, true));
				} else if (map.get(STATE).equals(UPDATE)) {
					session.getTableSession().update(relation.getRefResourceName(), (Map<String, Object>) setBasicPropertyValues(map, false));
				} else if (map.get(STATE).equals(DELETE)) {
					session.getTableSession().delete(relation.getRefResourceName(), map);
				}
			} else {
				Boolean isExists = null;
				MappingProperty  mappingProperty = null;
				JSONObject extend  = null;
				dsbsList =  relation.getSqlList();
				if(dsbsList!=null&&dsbsList.size()>0) {
					for(SqlMetadata sqlMetadata:dsbsList) {
						if(map.get(STATE).equals(sqlMetadata.getType())) {
							isExists = mappingHandler.exists(sqlMetadata.getName());
							if(!isExists) {
								ResponseContext.addValidationFull(sqlMetadata.getName(), "resurceName", "该资源不存在","smartone.dynamic.resource.notExists");
								 return;
							}
							mappingProperty = mappingHandler.getMappingProperty(sqlMetadata.getName());
							extend = JSONObject.parseObject(mappingProperty.getExtend());
							int sqlType = extend.getInteger("type");
							if (sqlType == 1) {
								session.getSQLSession().executeProcedure(ProcedurePrefixUtil.CALL+sqlMetadata.getName(), null, map);
							} else {
								session.getSQLSession().executeUpdate(sqlMetadata.getName(), null, map);
							}
						}
					}
				}
				// 执行存储过程
			}
			if (relation.getChildrenList() != null) {
				if("1".equals(relation.getIsCascadeDelete())&&map.get(STATE).equals(DELETE)) {
					//级联删方法
					deleteSelfChildNode(relation,session,map);
				}else {
					operateChild(relation, map, session,mappingHandler);
				}
			}
		}
	}


	// 递归操作子节点
	@SuppressWarnings("unchecked")
	private void operateChild(RelationMetadata relation, Map<String, Object> map, Session session,MappingHandler mappingHandler) {
		List<SqlMetadata> dsbsList = null;
		for (RelationMetadata childRelation : relation.getChildrenList()) {
			List<Map<String, Object>> childMapList = null;
			Object obj = map.get(childRelation.getRefResourceKeyName());
			if(obj  instanceof  JSONArray ) {
				childMapList = (List<Map<String, Object>>)obj;
			}else{
				childMapList = new ArrayList<Map<String, Object>>();
			    Map<String, Object> childmap = (Map<String, Object>)obj;
			    childMapList.add(childmap);
			}
			if(childMapList!=null&&childMapList.size()>0) {
			for (Map<String, Object> childmap : childMapList) {
				if(childmap.get("ID")==null) {
					childmap.put("ID",UUID.randomUUID().toString());
				}
				if ((ResourceTypeUtil.RESOURCE_TYPE_TABLE+"").equals(childRelation.getRefResourceType())) {
					if (childmap.get(STATE).equals(INSERT)) {
						if(childmap.get(childRelation.getRefParentResourcePropId())==null) {
							childmap.put(childRelation.getRefParentResourcePropId(),
									map.get(relation.getIdPropName()));
						}
						session.getTableSession().save(childRelation.getRefResourceName(),
								(Map<String, Object>) setBasicPropertyValues(childmap, true));
					} else if (childmap.get(STATE).equals(UPDATE)) {
						if(childmap.get(childRelation.getRefParentResourcePropId())==null) {
							childmap.put(childRelation.getRefParentResourcePropId(),
									map.get(relation.getIdPropName()));
						}
						session.getTableSession().update(childRelation.getRefResourceName(),
								(Map<String, Object>) setBasicPropertyValues(childmap, false));
					} else if (childmap.get(STATE).equals(DELETE)) {
						session.getTableSession().delete(childRelation.getRefResourceName(), childmap);
						if ("1".equals(childRelation.getIsCascadeDelete()) && childRelation.getChildrenList()!=null) {
							deleteSelfChildNode(childRelation, session, childmap);
							return;
						}
					}   
				} else {
					Boolean isExists = null;
					MappingProperty  mappingProperty = null;
					JSONObject extend  = null;
					dsbsList = childRelation.getSqlList();
					for (SqlMetadata sqlMetadata : dsbsList) {
						if (sqlMetadata.getType().equals(childmap.get(STATE))) {
							// 执行存储过程
							if(childmap.get(childRelation.getRefParentResourcePropId())==null) {
								childmap.put(childRelation.getRefParentResourcePropId(),map.get(relation.getIdPropName()));
							}
							isExists = mappingHandler.exists(sqlMetadata.getName());
							if(!isExists) {
								ResponseContext.addValidationFull(sqlMetadata.getName(), "resurceName", "该资源不存在","smartone.dynamic.resource.notExists");
								 return;
							}
							mappingProperty = mappingHandler.getMappingProperty(sqlMetadata.getName());
							extend = JSONObject.parseObject(mappingProperty.getExtend());
							int sqlType = extend.getInteger("type");
							if (sqlType == 1) {
								session.getSQLSession().executeProcedure(ProcedurePrefixUtil.CALL+sqlMetadata.getName(), null,childmap);
							} else {
								session.getSQLSession().executeUpdate(sqlMetadata.getName(), null,childmap);
							}
						}
					}
				}

				if (childRelation.getChildrenList() != null ) {
					if ("1".equals(childRelation.getIsCascadeDelete()) && childmap.get(STATE).equals(DELETE)) {
						// 级联删方法
						deleteSelfChildNode(childRelation, session, childmap);
					} else {
						operateChild(childRelation, childmap, session,mappingHandler);
					}
				}
			 }
		  }
		}
	}

	// 递归删除子节点
	private void deleteSelfChildNode(RelationMetadata dcbmrr, Session session, Map<String, Object> map) {
		for (RelationMetadata relationMetadatas : dcbmrr.getChildrenList()) {
			// 当子结构为表时
			if ((ResourceTypeUtil.RESOURCE_TYPE_TABLE+"").equals(relationMetadatas.getRefResourceType())) {
				List<Map<String, Object>> childMapList = (List<Map<String, Object>>) session.getSqlSession()
						.query("select * from " + relationMetadatas.getRefResourceName() + " where "
								+ relationMetadatas.getRefParentResourcePropId() + "='"
								+ map.get("ID").toString() + "'");
				if (relationMetadatas.getChildrenList() != null && childMapList.size() > 0) {
					for (Map<String, Object> childmap : childMapList) {
						deleteSelfChildNode(relationMetadatas, session, childmap);
					}
				}
				for (Map<String, Object> childmap : childMapList) {
					session.getSqlSession()
							.executeUpdate("delete from " + relationMetadatas.getRefResourceName()
									+ " where ID = '" + childmap.get("ID").toString() + "'");
				}
				// 当子结构为sql时
			} else {
				List<Map<String, Object>> mapList = null;
				List<SqlMetadata> dbsList = relationMetadatas.getSqlList();
				if (dbsList != null && dbsList.size() > 0) {
					for (SqlMetadata sqlMetadata : dbsList) {
						if (sqlMetadata.getType().equals(SELE_DELE)) {
							mapList = (List<Map<String, Object>>) session.getSQLSession().query(sqlMetadata.getName(), null,
									map);
							if (mapList != null && mapList.size() > 0) {
								session.getSQLSession().executeUpdate(sqlMetadata.getName(), null, mapList);
	    					    deleteSqlNode(mapList,relationMetadatas,session);
							}
						}
					}
				}
			}
		}
	}
	
	private void deleteSqlNode(List<Map<String, Object>> mapList, RelationMetadata relationMetadatas,
			Session session) {
		if (relationMetadatas.getChildrenList()!= null) {
			for(RelationMetadata dcbrr:relationMetadatas.getChildrenList()) {
				for (Map<String, Object> map : mapList) {
					if((ResourceTypeUtil.RESOURCE_TYPE_TABLE+"").equals(dcbrr.getRefResourceType())) {
						List<Map<String, Object>> childMapList = (List<Map<String, Object>>) session.getSqlSession()
								.query("select * from " + dcbrr.getRefResourceName() + " where "
										+ dcbrr.getRefParentResourcePropId() + "='"
										+ map.get("ID").toString() + "'");
						if (dcbrr.getChildrenList() != null && childMapList.size() > 0) {
							for (Map<String, Object> childmap : childMapList) {
								deleteSelfChildNode(dcbrr, session, childmap);
							}
						}
						for (Map<String, Object> childmap : childMapList) {
							session.getSqlSession()
									.executeUpdate("delete from " + dcbrr.getRefResourceName()
											+ " where ID = '" + childmap.get("ID").toString() + "'");
						}
					}else {
						List<Map<String, Object>> maplist = null;
						List<SqlMetadata> dbsList = dcbrr.getSqlList();
						if (dbsList != null && dbsList.size() > 0) {
							for (SqlMetadata sqlMetadata : dbsList) {
								if (sqlMetadata.getType().equals(SELE_DELE)) {
									// 查询是否有数据
									maplist = (List<Map<String, Object>>) session.getSQLSession().query(sqlMetadata.getName(), null,map);
									if (maplist != null && maplist.size() > 0) {
										session.getSQLSession().executeUpdate(sqlMetadata.getName(), null, maplist);
			    					    deleteSqlNode(maplist,dcbrr,session);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Transaction
	public void createBusiModel(DmResource dmResource) {
		createModel(dmResource);
	}
	
	@Transaction
	public void createModel() {
		List<DmResource> list = SessionContext.getTableSession().query(DmResource.class,"select * from DM_RESOURCE where IS_BUILD_MODEL = 1 and RESOURCE_TYPE = 30 ");
		for(DmResource dmResource:list) {
	    	createModel(dmResource);
	    }
	}
	
	public void createModel(DmResource dmResource) {
		if (dmResource.getIsBuildModel() == 1) {
			ResponseContext.addValidation(dmResource.getId(), "id", "smartone.dynamic.business.createModel.exists");
		    return;
		}
		MappingHandler handler= SessionFactoryContainer.getSingleton().get().getMappingHandler();
		List<Resource> resourceList = ResourceContentService.getResourceContent(dmResource.getId());
		String busiModelXml = resourceList.get(0).getContent();
		try {
			SessionContext.getSqlSession().executeUpdate("DELETE FROM DM_RESOURCE_MAPPING WHERE RESOURCE_ID='"+dmResource.getId()+"'");
			handler.execute(new AddOrCoverMappingEntity(busiModelXml, ExMappingTypeConstants.BUSIMODEL).enableProperty());
			DmResourceMapping busiModelMaping = new DmResourceMapping(dmResource.getId(),resourceList.get(0).getName(),busiModelXml,30);
			SessionContext.getTableSession().save(setBasicPropertyValues(busiModelMaping, true));      
			SessionContext.getSqlSession().executeUpdate("update DM_RESOURCE SET OLD_RESOURCE_NAME = null,IS_BUILD_MODEL=1,IS_EVER_BUILD_MODEL=1  where ID='" + dmResource.getId()+ "'");
			Map<String, Object> idsMap = new HashMap<String, Object>();
			idsMap.put("id", dmResource.getId());
			idsMap.put("msg", "建模成功");
			ResponseContext.addData(idsMap);
		} catch (MappingHandleException e) {
			e.printStackTrace();
			ResponseContext.addError(dmResource, e);
			SessionContext.executeRollback();
		}
	}
	
	@Transaction
	public void cancelBusiModel(DmResource dmResource) {
		cancelModel(dmResource);
	}
	
	private void cancelModel(DmResource dmResource) {
		DmResource cbm = SessionContext.getTableSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE WHERE ID='"+dmResource.getId()+"'");
	    if(cbm.getIsBuildModel()==0) {
	    	ResponseContext.addValidationFull(dmResource, "id", "该业务未建模，不能进行取消建模操作","smartone.dynamic.business.noModel");
	        return;
	    }
		try {
			cancelmodel(cbm);
			cbm.setIsBuildModel(0);
			SessionContext.getSqlSession().executeUpdate("DELETE FROM DM_RESOURCE_MAPPING WHERE RESOURCE_ID='"+dmResource.getId()+"'");
			SessionContext.getTableSession().update(setBasicPropertyValues(cbm, false));   
			Map<String,Object> idsMap = new HashMap<String,Object>();
	    	idsMap.put("id", cbm.getId());
			idsMap.put("msg", "取消建模成功");
			ResponseContext.addData(idsMap);
		} catch (MappingHandleException e) {
			e.printStackTrace();
			SessionContext.executeRollback();
		}
	}
	
	public  void cancelmodel(DmResource dmResource) throws MappingHandleException{
		MappingHandler handler= SessionFactoryContainer.getSingleton().get().getMappingHandler();
		handler.execute(new DeleteMappingEntity(dmResource.getResourceName()));
	}
	
	public boolean doValidate(DmResource dmResource) {
		List<DmResource> obj = null;
		if(dmResource.getId()!=null) {
			obj = (List<DmResource>) SessionContext.getSqlSession().query(DmResource.class,
					"select * from DM_RESOURCE WHERE RESOURCE_NAME='" + dmResource.getResourceName().toUpperCase() + "' and ID!='"+dmResource.getId()+"'");
		}else {
			obj = (List<DmResource>)SessionContext.getSqlSession().query(DmResource.class,
					"select * from DM_DM_RESOURCE WHERE RESOURCE_NAME='" + dmResource.getResourceName().toUpperCase() + "'");
		}
		if (obj != null && obj.size() > 0) {
			ResponseContext.addValidation("资源名不能重复", null, dmResource);
			return false;
		}
		return true;
	}
}

