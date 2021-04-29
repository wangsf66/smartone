package com.smartone.ddm.busimodel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.SessionFactoryContainer;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.mapping.handler.MappingHandleException;
import com.douglei.orm.mapping.handler.MappingHandler;
import com.douglei.orm.mapping.handler.entity.DeleteMappingEntity;
import com.ibs.code.service.BasicService;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.busimodel.entity.DmBusiModelRelation;
import com.smartone.ddm.busimodel.entity.DmSqlBusiStructure;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.util.ResourceTypeUtil;
/**
 * 
 * @author wangShuFang
 */
@TransactionComponent
public class DmCfgBusiModelResRelationsService extends BasicService{
	
	
	@Transaction
	public void insertMany(List<DmBusiModelRelation> list) {
		for(DmBusiModelRelation cfgBusiModelResRelations:list) {
			insertBuis(cfgBusiModelResRelations); 
		}
	}
	
	
	private void insertBuis(DmBusiModelRelation cfgBusiModelResRelations) {
		DmResource dmResource = null;
		if (cfgBusiModelResRelations.getRefResourceType() == 10) {
			dmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class,
					"select * from DM_RESOURCE WHERE ID='" + cfgBusiModelResRelations.getRefResourceId()+"'");
			if (dmResource.getIsBuildModel() == 0) {
				ResponseContext.addValidationFull(cfgBusiModelResRelations, "refResourceId", "该资源未被建模，不可使用",
						"smartone.dynamic.resource.unavailable");
				return;
			} 
			cfgBusiModelResRelations.setRefDescName(dmResource.getDescName());
			cfgBusiModelResRelations.setRefResourceName(dmResource.getResourceName());
			SessionContext.getTableSession().save(setBasicPropertyValues(cfgBusiModelResRelations, true));
		} else {
			List<DmSqlBusiStructure> sqlList = cfgBusiModelResRelations.getSqlList();
			if (sqlList.size() > 0) {
				for (DmSqlBusiStructure dmSqlBusiStructure : sqlList) {
					dmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class,
							"select * from DM_RESOURCE WHERE ID='" + dmSqlBusiStructure.getResourceId() + "'");
					if (dmResource.getIsBuildModel() == 0) {
						ResponseContext.addValidationFull(cfgBusiModelResRelations, "refResourceId", "该资源未被建模，不可使用",
								"smartone.dynamic.resource.unavailable");
						return;
					} 
					dmSqlBusiStructure.setParentId(cfgBusiModelResRelations.getId());
					SessionContext.getTableSession().save(dmSqlBusiStructure);
				}
			}
			SessionContext.getTableSession().save(setBasicPropertyValues(cfgBusiModelResRelations, true));
		}
		DmResource dmresource = SessionContext.getTableSession().uniqueQuery(DmResource.class,
				"select * from DM_RESOURCE WHERE ID='" + cfgBusiModelResRelations.getRefBusiModelId() + "'");
		if (dmresource.getIsBuildModel() == 1) {
			dmresource.setIsBuildModel(0);
			try {
				cancelmodel(dmresource);
			} catch (MappingHandleException e) {
				e.printStackTrace();
			}
		}else{
			dmresource.setIsBuildModel(dmresource.getIsBuildModel());
		}
		SessionContext.getTableSession().update(setBasicPropertyValues(dmresource, false));
		ResponseContext.addData(cfgBusiModelResRelations);
	}
	
	public void cancelmodel(DmResource dmResource) throws MappingHandleException{
		MappingHandler handler= SessionFactoryContainer.getSingleton().get().getMappingHandler();
		handler.execute(new DeleteMappingEntity(dmResource.getResourceName()));
	}
	
	
	
	@Transaction	
	public void updateMany(List<DmBusiModelRelation> list) {
		  for(DmBusiModelRelation cfgBusiModelResRelations:list) {
			     updateBuis(cfgBusiModelResRelations); 
		  }
	}
	
	
	private void updateBuis(DmBusiModelRelation cfgBusiModelResRelations) {
	    DmResource dmResource  = null;
		if(cfgBusiModelResRelations.getRefResourceType()==10) {
			   dmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class, "select * from DM_RESOURCE WHERE ID='"+cfgBusiModelResRelations.getRefResourceId()+"'  and RESOURCE_TYPE="+cfgBusiModelResRelations.getRefResourceType());
		   if(dmResource.getIsBuildModel()==1) {
			   cfgBusiModelResRelations.setRefResourceType(dmResource.getResourceType());
			   cfgBusiModelResRelations.setRefDescName(dmResource.getDescName());
			   cfgBusiModelResRelations.setRefResourceName(dmResource.getResourceName());
			   SessionContext.getTableSession().update(setBasicPropertyValues(cfgBusiModelResRelations, false)); 
		   }else {
			   ResponseContext.addValidationFull(cfgBusiModelResRelations, "refResourceId", "该资源未被建模，不可使用","smartone.dynamic.resource.unavailable");
		   }
		}else {
			if(cfgBusiModelResRelations.getSqlList()==null) {
				SessionContext.getTableSession().update(setBasicPropertyValues(cfgBusiModelResRelations, false)); 
			}else {
				List<DmSqlBusiStructure> sqlList = cfgBusiModelResRelations.getSqlList();
				if(sqlList.size()>0) {
					//先将sql业务关系删除 在添加
					SessionContext.getSqlSession().executeUpdate("delete from DM_SQL_BUSI_STRUCTURE where PARENT_ID='"+cfgBusiModelResRelations.getId()+"'");
					for(DmSqlBusiStructure dmSqlBusiStructure:sqlList) {
			    		dmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class, "select * from DM_RESOURCE WHERE ID='"+dmSqlBusiStructure.getResourceId()+"'");
				    	if(dmResource.getIsBuildModel()==1) {
				    		//dmSqlBusiStructure.setType(dmResource.getResourceType()+"");
				    		dmSqlBusiStructure.setParentId(cfgBusiModelResRelations.getId());
				    		SessionContext.getTableSession().save(dmSqlBusiStructure);
				    	}else {
				    		ResponseContext.addValidationFull(cfgBusiModelResRelations, "refResourceId", "该资源未被建模，不可使用","smartone.dynamic.resource.unavailable");
					    	}
					    }
				    }
				cfgBusiModelResRelations.setRefResourceType(20);
				SessionContext.getTableSession().update(setBasicPropertyValues(cfgBusiModelResRelations, false));
			}  
		}
		DmResource dmresource = SessionContext.getTableSession().uniqueQuery(DmResource.class,
				"select * from DM_RESOURCE WHERE ID='" +cfgBusiModelResRelations.getRefBusiModelId()+ "'");
		if(dmresource.getIsBuildModel()==1) {
			dmresource.setIsBuildModel(0);
			try {
				cancelmodel(dmresource);
			} catch (MappingHandleException e) {
				e.printStackTrace();
			}
		}else{
			dmresource.setIsBuildModel(dmresource.getIsBuildModel());
		}
		SessionContext.getTableSession().update(setBasicPropertyValues(dmresource, false));
		ResponseContext.addData(cfgBusiModelResRelations);
	}

  @Transaction
  public void delete(String ids) {
	 List<DmBusiModelRelation> childlist = null;
	 DmResource dmresource = null;
	 List<DmSqlBusiStructure> DmSqlBusiStructurelist = null;
	 List<DmBusiModelRelation> list = SessionContext.getSqlSession().query(DmBusiModelRelation.class,"select * from DM_CFG_BUSI_MODEL_RES_RELATIONS where ID in ("+getParam(ids)+")");  
	 for(DmBusiModelRelation cfgBusiModelResRelations:list) {
		   childlist = SessionContext.getSqlSession().query(DmBusiModelRelation.class,"select * from DM_CFG_BUSI_MODEL_RES_RELATIONS where PARENT_ID = '"+cfgBusiModelResRelations.getId()+"'"); 
		   if(childlist!=null&&childlist.size()>0) {
			   deleteChildren(childlist);
		   }
		   if(cfgBusiModelResRelations.getRefResourceType()==ResourceTypeUtil.RESOURCE_TYPE_SQL) {
			   DmSqlBusiStructurelist = SessionContext.getSqlSession().query(DmSqlBusiStructure.class,"select * from DM_SQL_BUSI_STRUCTURE where PARENT_ID = '"+cfgBusiModelResRelations.getId()+"'");   
		       if(DmSqlBusiStructurelist!=null&&DmSqlBusiStructurelist.size()>0) {
		    	   for(DmSqlBusiStructure dmSqlBusiStructure:DmSqlBusiStructurelist) {
		    		   SessionContext.getSqlSession().executeUpdate("delete from DM_SQL_BUSI_STRUCTURE where ID='"+dmSqlBusiStructure.getId()+"'");
		    	   }
		       }
		   }
		   SessionContext.getSqlSession().executeUpdate("delete from DM_CFG_BUSI_MODEL_RES_RELATIONS where ID='"+cfgBusiModelResRelations.getId()+"'");
	    }
	   dmresource = SessionContext.getSqlSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE where ID = '"+list.get(0).getRefBusiModelId()+"'");
	   if(dmresource.getIsBuildModel()==1) {
		   dmresource.setIsBuildModel(0);
		   try {
				cancelmodel(dmresource);
			  } catch (MappingHandleException e) {
				e.printStackTrace();
		   }
	   }else{
		   dmresource.setIsBuildModel(dmresource.getIsBuildModel());
	   }
	   SessionContext.getTableSession().update(setBasicPropertyValues(dmresource, false));
	    Map<String,Object> idsMap = new HashMap<String,Object>();
	    idsMap.put("ids", ids);
		ResponseContext.addData(idsMap);  
      }
  
	 
	  private void deleteChildren(List<DmBusiModelRelation> childlist) {
		   List<DmSqlBusiStructure> DmSqlBusiStructurelist = null;
		   List<DmBusiModelRelation> nodelist = null;
		   for(DmBusiModelRelation dbmrr:childlist) {
			   nodelist = SessionContext.getSqlSession().query(DmBusiModelRelation.class,"select * from DM_CFG_BUSI_MODEL_RES_RELATIONS where PARENT_ID = '"+dbmrr.getId()+"'"); 
		       if(nodelist!=null&&nodelist.size()>0) {
		    	   deleteChildren(nodelist);
		       }
		       if(dbmrr.getRefResourceType()==ResourceTypeUtil.RESOURCE_TYPE_SQL) {
				   DmSqlBusiStructurelist = SessionContext.getSqlSession().query(DmSqlBusiStructure.class,"select * from DM_SQL_BUSI_STRUCTURE where PARENT_ID = '"+dbmrr.getId()+"'");   
			       if(DmSqlBusiStructurelist!=null&&DmSqlBusiStructurelist.size()>0) {
			    	   for(DmSqlBusiStructure dmSqlBusiStructure:DmSqlBusiStructurelist) {
			    		   SessionContext.getSqlSession().executeUpdate("delete from DM_SQL_BUSI_STRUCTURE where ID='"+dmSqlBusiStructure.getId()+"'");
			    	   }
			       }
			   }
		       SessionContext.getSqlSession().executeUpdate("delete from DM_CFG_BUSI_MODEL_RES_RELATIONS where ID='"+dbmrr.getId()+"'");
		   }
	  }

	private String getParam(String ids) {
			String id[] = ids.split(",");
			String sql = "";
			for(int i=0;i<id.length;i++) {
				if(i==id.length-1) {
					sql+= "'"+id[i]+"'";
				}else {
					sql+= "'"+id[i]+"' ,";
				}
			}	
			return  sql;
		}
	  
	@Transaction
	public void queryList(String id) {
		DmBusiModelRelation cbmrr = SessionContext.getTableSession().uniqueQuery(DmBusiModelRelation.class, "select * from DM_CFG_BUSI_MODEL_RES_RELATIONS where ID='"+id+"' ");
		List<DmSqlBusiStructure> dsbsList = null;
			if(cbmrr.getRefResourceType()==ResourceTypeUtil.RESOURCE_TYPE_SQL) {
				dsbsList = SessionContext.getTableSession().query(DmSqlBusiStructure.class, "select * from DM_SQL_BUSI_STRUCTURE where PARENT_ID='"+id+"'");
				cbmrr.setSqlList(dsbsList);
			}
			ResponseContext.addData(cbmrr);
	}
}
