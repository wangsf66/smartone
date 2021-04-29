package com.smartone.ddm.resource.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.douglei.orm.configuration.environment.EnvironmentContext;
import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.SessionFactoryContainer;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.dialect.DatabaseType;
import com.douglei.orm.mapping.MappingTypeNameConstants;
import com.douglei.orm.mapping.handler.MappingHandleException;
import com.douglei.orm.mapping.handler.MappingHandler;
import com.douglei.orm.mapping.handler.entity.AddOrCoverMappingEntity;
import com.douglei.orm.mapping.handler.entity.DeleteMappingEntity;
import com.ibs.code.service.BasicService;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourceMapping;
import com.smartone.ddm.resource.entity.DmResourceParam;
import com.smartone.ddm.resource.entity.ProcedureParameter;
import com.smartone.ddm.resource.entity.Resource;
import com.smartone.ddm.resource.entity.SqlStatementTypeConstants;
import com.smartone.ddm.util.ParameterDeterminationUtil;
import com.smartone.ddm.util.ProcedurePrefixUtil;
import com.smartone.ddm.util.ProcedureSqlType;
import com.smartone.ddm.util.ResourceTypeUtil;
import com.smartone.ddm.util.StrUtil;
import com.smartone.ex.mapping.ExMappingTypeConstants;

import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.TStatementList;
import gudusoft.gsqlparser.nodes.TParameterDeclaration;
import gudusoft.gsqlparser.stmt.mssql.TMssqlCreateProcedure;
/**
 * 
 * @author wangShuFang
 */
@TransactionComponent
public class DmResourceService extends BasicService{
	

	@Autowired
	private ResourceContentService ResourceContentService;
	
	@Transaction
	public void insertMany(List<DmResource> list) {
		for(DmResource dmResource:list) {
			if(dmResource.getContent()==null) {
				dmResource.setResourceType(ResourceTypeUtil.RESOURCE_TYPE_TABLE);
				insertTableResource(dmResource);
			}else {
				dmResource.setResourceType(ResourceTypeUtil.RESOURCE_TYPE_SQL);
				insertSqlResource(dmResource);
			}
	    }
	}
	
	
	public void insertTableResource(DmResource dmResource) {
		if (doValidate(dmResource)) {
			SessionContext.getTableSession().save(setBasicPropertyValues(dmResource, true));
			ResponseContext.addData(dmResource);
		}
	}
	
	public void insertSqlResource(DmResource dmResource) {
		if(dmResource.getContent().length()>10000) {
			ResponseContext.addValidationFull(dmResource, "content", "sql内容超长", "smartone.dynamic.sql.insert.ContentOverLength");
		    return;
		}
		if (doValidate(dmResource)) {
			TGSqlParser parser = new TGSqlParser(StrUtil.getDialect());
			//TODO 修改返回值 isSelectSql 
			if(sqlParser(dmResource,parser)) {
				dmResource.setIsBuildModel(0);
				//解析参数
				if(dmResource.getResourceType() == ResourceTypeUtil.RESOURCE_TYPE_PROCEDURE) {
					saveProcedureResourceParams(dmResource,parser);
					SessionContext.getTableSession().save(setBasicPropertyValues(dmResource,true));
					ResponseContext.addData(dmResource);
				}else {
					if(saveResourceParams(dmResource,parser)) {
						SessionContext.getTableSession().save(setBasicPropertyValues(dmResource,true));
						ResponseContext.addData(dmResource);
					}
				}
			}
		}
	}
	
	private void saveProcedureResourceParams(DmResource dmResource, TGSqlParser parser) {
		parser.sqltext = dmResource.getContent();
		TCustomSqlStatement sqlStatement  = parser.sqlstatements.get(0);
		switch(sqlStatement.sqlstatementtype){
	    case sstplsql_createprocedure:
	    	//analysisOracleProcedure((TPlsqlCreateProcedure)sqlStatement, dmSql);
	    	break;
	    case sstmssqlcreateprocedure:
	    	analysisSqlServerProcedure((TMssqlCreateProcedure)sqlStatement,dmResource);
	    	break;
	    default:
	    	throw new IllegalArgumentException("目前不支持["+sqlStatement.sqlstatementtype+"]类型的存储过程");
	   }
	}

	private void analysisSqlServerProcedure(TMssqlCreateProcedure procedureSqlStatement, DmResource dmResource) {
		//解析出存储过程名
		String nameSpace = procedureSqlStatement.getProcedureName().toString();
		if(!(nameSpace.equals(dmResource.getResourceName()))) {
			ResponseContext.addValidationFull(dmResource, "nameSpace", "存储过程名称需和资源名称保持一致", "smartone.dynamic.sql.insert.ResourceNameSameNameSpace");
			return;
		}
		SessionContext.getSqlSession().executeUpdate("delete from DM_RESOURCE_PARAMS where RESOURCE_ID='" + dmResource.getId() + "'");
		List<ProcedureParameter> parameterList = new ArrayList<ProcedureParameter>();
		if(procedureSqlStatement.getParameterDeclarations() != null && procedureSqlStatement.getParameterDeclarations().size() > 0){
			TParameterDeclaration param = null;
			int len = procedureSqlStatement.getParameterDeclarations().size();
			ProcedureParameter procedureParameter = null;
			String parameterName;
			String defaultValue = null;
			String dataType;
			String length;
			String precision;
			String[] lengthArr = null;
			for(int i=0;i<len;i++){
				procedureParameter = new ProcedureParameter();
	        	param = procedureSqlStatement.getParameterDeclarations().getParameterDeclarationItem(i);
				//参数名
	        	parameterName = param.getParameterName().toString();
				if(parameterName.startsWith("@")){
					parameterName = parameterName.substring(1); 
					procedureParameter.setParameterName(parameterName);
				}
				//参数默认值
				if(param.getDefaultValue() != null){
					defaultValue = param.getDefaultValue().toString();
					if(defaultValue.startsWith("'")){
						defaultValue = defaultValue.substring(1, defaultValue.length()-1);
						procedureParameter.setDefaultValue(defaultValue);
					}
				}
				//参数类型
				dataType = param.getDataType().toString().toUpperCase();
				String datetype = null;
				if(dataType.indexOf("(") != -1){
					lengthArr = dataType.substring(dataType.indexOf("(")+1, dataType.indexOf(")")).split(",");
					length = lengthArr[0];
					//参数长度
					if(!lengthArr[0].equalsIgnoreCase("max")) {
						procedureParameter.setLength(Integer.parseInt(length));
					}
					//参数精度
					if(lengthArr.length == 2){
						precision = lengthArr[1];
						procedureParameter.setPrecision(Integer.parseInt(precision));
					}else if(lengthArr.length > 2){
						throw new IllegalArgumentException("系统在解析sqlserver存储过程参数时出现异常，请联系后台系统开发人员");
					}
					datetype = dataType.substring(0, dataType.indexOf("("));
				}else {
					datetype = dataType;
				}
				procedureParameter.setDataType(datetype);
				procedureParameter.setInout(param.getMode());
				parameterList.add(procedureParameter);
	        }
		}
		DmResourceParam dmResourceParam = null;
		int sort = 0 ;
		for(ProcedureParameter pc:parameterList) {
			sort++;
			dmResourceParam = new DmResourceParam(pc.getParameterName(),pc.getDataType(),pc.getLength(),pc.getDefaultValue(),pc.getPrecision(),sort,dmResource.getId(),0,pc.getInout());
			SessionContext.getTableSession().save(setBasicPropertyValues(dmResourceParam, true));
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean saveResourceParams(DmResource dmResource,TGSqlParser parser) {
		boolean temp = true;
		SessionContext.getSqlSession().executeUpdate("delete from DM_RESOURCE_PARAMS where RESOURCE_ID='" + dmResource.getId() + "'");
		Object resultList = null;
		Object paramsList = null;
		if (dmResource.isSelectSql()) {
			//1解析sql语句中的结果集
			resultList = ParameterDeterminationUtil.parseResultSet(parser,dmResource);
			//0解析sql语句中的参数集
			paramsList = ParameterDeterminationUtil.parseParamsSet(dmResource);
			if (paramsList != null) {
				List<DmResourceParam> sqlParamsSets = (List<DmResourceParam>) paramsList;
				for (DmResourceParam dsp : sqlParamsSets) {
					SessionContext.getTableSession().save(setBasicPropertyValues(dsp, true));
				}
			}else {
				temp = false;
			}
			if (resultList != null) {
				List<DmResourceParam> sqlResultSets = (List<DmResourceParam>) resultList;
				for (DmResourceParam dsp : sqlResultSets) {
					SessionContext.getTableSession().save(setBasicPropertyValues(dsp, true));
				}
			}else {
				temp = false;
			}
		} else {
			paramsList = ParameterDeterminationUtil.parseParamsSet(dmResource);
			if (paramsList != null) {
				List<DmResourceParam> sqlParamsSets = (List<DmResourceParam>) paramsList;
				for (DmResourceParam dsp : sqlParamsSets) {
					SessionContext.getTableSession().save(setBasicPropertyValues(dsp, true));
				}
			}else {
				temp = false;
			}
		}
		return temp;
	}
	
	
	
	private boolean sqlParser(DmResource dmResource,TGSqlParser parser) {
		 String sqlcontent = dmResource.getContent();
		 parser.sqltext = sqlcontent;
		 if(parser.parse()!=0) {
			 parser.getErrormessage();
			 ResponseContext.addValidationFull(dmResource, "content", "sql错误:%s", "smartone.dynamic.sql.insert.GrammaticalErrors",parser.getErrormessage());
			 return false;
		 }
	    TStatementList list = parser.sqlstatements;
	    //存储过程
		if(SqlStatementTypeConstants.transType(list.get(0).sqlstatementtype)==6){
			//如果是存储过程，就修改RESOURCE_TYPE
			dmResource.setResourceType(ResourceTypeUtil.RESOURCE_TYPE_PROCEDURE); 
		}else if(SqlStatementTypeConstants.transType(list.get(0).sqlstatementtype)==1) {
			dmResource.setSelectSql(true);
		}
		return true;
	 }
	
	
	  @Transaction
	  public void delete(String ids) {
		List<DmResource> list = SessionContext.getSqlSession().query(DmResource.class,
				"select * from DM_RESOURCE where ID in (" + StrUtil.stringToSql(ids) + ")");
		for (DmResource dmResource : list) {
			if (dmResource.getIsBuildModel() == 1) {
				ResponseContext.addValidationFull(dmResource, "id", "资源已建模，请取消建模后再做删除操作","smartone.dynamic.resource.CancelModel");
				return;
			}
			if (dmResource.getResourceType() == ResourceTypeUtil.RESOURCE_TYPE_TABLE) {
				SessionContext.getSqlSession()
						.executeUpdate("delete from DM_RESOURCE where ID = '" + dmResource.getId() + "'");
				SessionContext.getSqlSession().executeUpdate(
						"delete from DM_RESOURCE_PARAMS where RESOURCE_ID = '" + dmResource.getId() + "'");
			    //删除表 
				SessionContext.getSqlSession().executeUpdate("drop table "+dmResource.getResourceName());
			} else {
				SessionContext.getSqlSession()
						.executeUpdate("delete from DM_RESOURCE where ID = '" + dmResource.getId() + "'");
				SessionContext.getSqlSession().executeUpdate(
						"delete from DM_RESOURCE_PARAMS where RESOURCE_ID = '" + dmResource.getId() + "'");
			}
		}
		Map<String,Object> idsMap = new HashMap<String,Object>();
		idsMap.put("ids", ids);
		ResponseContext.addData(idsMap);
	  }
	  
	  
	@Transaction
	public void updateMany(List<DmResource> list) {
		for(DmResource dmResource:list) {
			 if(dmResource.getContent()==null) {
				dmResource.setResourceType(ResourceTypeUtil.RESOURCE_TYPE_TABLE);
				updateTable(dmResource);
			 }else {
				dmResource.setResourceType(ResourceTypeUtil.RESOURCE_TYPE_SQL);
				updateSql(dmResource);
			 }
		 } 
	}
	  
	public void updateSql(DmResource dmResource){
		if(dmResource.getContent().length()>10000) {
			ResponseContext.addValidationFull(dmResource, "content", "sql内容超长", "smartone.dynamic.sql.insert.ContentOverLength");
		    return;
		}
		DmResource OldDmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class,
				"select * from DM_RESOURCE WHERE ID='" + dmResource.getId() + "'");
		if (OldDmResource == null) {
			ResponseContext.addValidationFull(dmResource, "resourceName", "不存在此资源","smartone.dynamic.resource.notExists");
		    return;
		}
		List<DmResource> obj =(List<DmResource>)SessionContext.getSqlSession().query(DmResource.class,"select * from DM_RESOURCE WHERE RESOURCE_NAME='"+dmResource.getResourceName()+"' and ID!='"+dmResource.getId()+"'");
		if(obj.size()>0) {
			ResponseContext.addValidationFull(dmResource, "resourceName", "不可插入重复的资源名","smartone.resource.insert.uniqueResourceName");
		    return;
		}	
		if (!OldDmResource.getResourceName().equals(dmResource.getResourceName())
				|| !OldDmResource.getContent().equals(dmResource.getContent())) {
			//修改时删除sql参数重新解析
			SessionContext.getSqlSession().executeUpdate(
					"delete from DM_RESOURCE_PARAMS where resource_id = '" + OldDmResource.getId() + "'");
			TGSqlParser parser = new TGSqlParser(StrUtil.getDialect());
			// 解析sql语句
			if (sqlParser(dmResource,parser)) {
				// 如果name相同，直接保存，不同时需要在数据库查询新的name对应的数组，数组大小大于0，表示有同名的sql名称，验证错误，数组大小小于0，则数据库中并没有同名的sname，保存
				if (dmResource.getResourceName().equals(OldDmResource.getResourceName())) {
					SessionContext.getTableSession().update(setBasicPropertyValues(dmResource,false));
				} else {
					List<DmResource> list = SessionContext.getTableSession().query(DmResource.class,
							"select * from DM_RESOURCE where RESOURCE_NAME ='" + dmResource.getResourceName()
									+ "'");
					if (list != null && list.size() > 0) {
						ResponseContext.addValidationFull(dmResource, "resourceName", "不可插入重复的资源名",
								"smartone.resource.insert.uniqueResourceName");
						return;
					}
					//TODO 资源存在（资源已见过模且资源表中没有删除该资源）的情况下只要oldname为null就记录oldname
					if(OldDmResource.getIsEverBuildModel()==1&&OldDmResource.getOldResourceName()==null) {
						dmResource.setOldResourceName(OldDmResource.getResourceName());
					}
					SessionContext.getTableSession().update(setBasicPropertyValues(dmResource,false));
				}
			    //解析sql参数
				if(dmResource.getResourceType() == ResourceTypeUtil.RESOURCE_TYPE_PROCEDURE) {
					saveProcedureResourceParams(dmResource,parser);
					saveResource(OldDmResource,dmResource);
				}else {
					if(saveResourceParams(dmResource,parser)) {
						saveResource(OldDmResource,dmResource);
					}
				}
			}
		} else {
			dmResource.setIsBuildModel(OldDmResource.getIsBuildModel());
			dmResource.setResourceType(OldDmResource.getResourceType());
			SessionContext.getTableSession().update(setBasicPropertyValues(dmResource,false));
			ResponseContext.addData(dmResource);
		}
	}
	
	private void saveResource(DmResource OldDmResource,DmResource dmResource) {
		dmResource.setIsEverBuildModel(OldDmResource.getIsEverBuildModel());
		dmResource.setIsBuildModel(0);
		SessionContext.getTableSession().update(setBasicPropertyValues(dmResource,false));
		ResponseContext.addData(dmResource);
		if(OldDmResource.getIsBuildModel()==1) {
			try {
				cancelmodel(OldDmResource);
			} catch (MappingHandleException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateTable(DmResource dmResource) {
		DmResource oldDmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class,
				"select * from DM_RESOURCE WHERE ID='" + dmResource.getId() + "'");
		if(oldDmResource == null) {
			ResponseContext.addValidationFull(dmResource, "resourceName", "不存在此资源","smartone.dynamic.resource.notExists");
		    return;
		}
		if(dmResource.getResourceName().equals(oldDmResource.getResourceName())) {
			dmResource.setIsEverBuildModel(oldDmResource.getIsEverBuildModel());
			dmResource.setIsBuildModel(oldDmResource.getIsBuildModel());
			SessionContext.getTableSession().update(setBasicPropertyValues(dmResource, false));
			ResponseContext.addData(dmResource);
		} else {
			if(oldDmResource.getIsEverBuildModel()==1&&oldDmResource.getOldResourceName()==null) {
				dmResource.setOldResourceName(oldDmResource.getResourceName());
			}
			if (doValidate(dmResource)) {
				dmResource.setIsEverBuildModel(oldDmResource.getIsEverBuildModel());
				dmResource.setIsBuildModel(0);
				SessionContext.getTableSession().update(setBasicPropertyValues(dmResource, false));
				ResponseContext.addData(dmResource);
				if(oldDmResource.getIsBuildModel()==1) {
					try {
						cancelmodel(oldDmResource);
					} catch (MappingHandleException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	//数据修改接口
	@Transaction
	public void createModel() {		List<DmResource> list = SessionContext.getTableSession().query(DmResource.class,"select * from DM_RESOURCE where IS_BUILD_MODEL = 1 and RESOURCE_TYPE = 15 ");
		TGSqlParser parser = new TGSqlParser(StrUtil.getDialect());
		for(DmResource dmResource:list) {
			createSqlModel(dmResource,parser);
	    	//createTableModel(dmResource);
	    }
	}
	
	@Transaction
	public void createModel(DmResource dmresource) {
		DmResource dmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE WHERE ID='"+dmresource.getId()+"'");
		if(dmResource.getIsBuildModel()==1) {
			ResponseContext.addValidationFull(dmResource, "id", " 改资源已被建模，不可重复建模","smartone.dynamic.resource.exists");
		    return;
		}	
	    if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_TABLE) {
			createTableModel(dmResource);
		}else {
			TGSqlParser parser = new TGSqlParser(StrUtil.getDialect());
			createSqlModel(dmResource,parser);
		}	
	}
	
	
	
	
	public String transTypeToString(int a) {
		switch(a){
        case 1:
           return "select";
        case 2:
           return "update";
        case 3:
        	return "insert";
        case 4:
        	return "delete";
        case 6:
        	return "procedure";
        case 5:
        	return "view";
        default:
        	return "declare";
       }
	}
	
	private void createSqlModel(DmResource dmResource, TGSqlParser parser) {
		if (dmResource.getResourceType() == ResourceTypeUtil.RESOURCE_TYPE_PROCEDURE) {
			procedureCreateSqlModel(dmResource);
		}else{
			List<Resource> resourceList = ResourceContentService.getResourceContent(dmResource.getId());
			createSqlModel(dmResource,resourceList);
		}
	}
	
	public void createSqlModel(DmResource dmResource,List<Resource> resourceList) {
		   MappingHandler handler= SessionFactoryContainer.getSingleton().get().getMappingHandler();
		   String xml = resourceList.get(0).getContent();
			try {
				handler.execute(new AddOrCoverMappingEntity(xml, MappingTypeNameConstants.SQL).enableProperty());
				DmResourceMapping dmResourceMapping = null;
				SessionContext.getSqlSession().executeUpdate("delete from DM_RESOURCE_MAPPING where RESOURCE_ID='"+dmResource.getId()+"'");
				if(dmResource.getIsBuildModel()==0) {
					dmResourceMapping = new DmResourceMapping(dmResource.getId(),dmResource.getResourceName(),xml.replaceAll("oldName=\"([^\"]*)\"", ""),dmResource.getResourceType());
					SessionContext.getTableSession().save(setBasicPropertyValues(dmResourceMapping,true));
				}
				SessionContext.getSqlSession().executeUpdate("update DM_RESOURCE SET OLD_RESOURCE_NAME = null,IS_BUILD_MODEL=1,IS_EVER_BUILD_MODEL=1 where ID='" + dmResource.getId()+ "'");
				Map<String,Object> idsMap = new HashMap<String,Object>();
				idsMap.put("id", dmResource.getId());
				idsMap.put("msg", "建模成功");
				ResponseContext.addData(idsMap);
			} catch (MappingHandleException e) {
				e.printStackTrace();
				Exception exc = (Exception)e.getCause();
				if(exc.getMessage()!=null) {
					ResponseContext.addValidationFull(dmResource, "id",exc.getMessage(),null);
				}else {
					ResponseContext.addError(dmResource,e);
				}
				SessionContext.executeRollback();
			}
	}
	
	public void createTableModel(DmResource dmResource) {
		MappingHandler handler= SessionFactoryContainer.getSingleton().get().getMappingHandler();
		List<Resource> resourceList = ResourceContentService.getResourceContent(dmResource.getId());
		try {
			String xml = resourceList.get(0).getContent();
			handler.execute(new AddOrCoverMappingEntity(xml, MappingTypeNameConstants.TABLE).enableProperty());
			int oldIsBuildModel = dmResource.getIsBuildModel();
			DmResourceMapping dmResourceMapping = null;
			SessionContext.getSqlSession().executeUpdate("delete from DM_RESOURCE_MAPPING where RESOURCE_ID='"+dmResource.getId()+"'");
			if (oldIsBuildModel == 0) {
				dmResourceMapping = new DmResourceMapping(dmResource.getId(), dmResource.getResourceName(),
						xml.replaceAll("oldName=\"([^\"]*)\"", ""), dmResource.getResourceType());
				dmResourceMapping.setIsFailure(0);
				SessionContext.getTableSession().save(setBasicPropertyValues(dmResourceMapping, true));
			}
			SessionContext.getSqlSession().executeUpdate("update DM_RESOURCE_PARAMS SET OLD_PARAM_NAME  = null  where RESOURCE_ID='" + dmResource.getId() + "'");
			SessionContext.getSqlSession().executeUpdate("update DM_RESOURCE SET OLD_RESOURCE_NAME = null,IS_BUILD_MODEL=1,IS_EVER_BUILD_MODEL=1 where ID='" + dmResource.getId()+ "'");
			Map<String, Object> idsMap = new HashMap<String, Object>();
			idsMap.put("id", dmResource.getId());
			idsMap.put("msg", "建模成功");
			ResponseContext.addData(idsMap);
		} catch (MappingHandleException e) {
			e.printStackTrace();
			Exception exc = (Exception)e.getCause();
			if(exc.getMessage()!=null) {
				ResponseContext.addValidationFull(dmResource, "id",exc.getMessage(),null);
			}else {
				ResponseContext.addError(dmResource,e);
			}
			SessionContext.executeRollback();
		}
	}
	
	public void procedureCreateSqlModel(DmResource dmResource) {
		MappingHandler handler= SessionFactoryContainer.getSingleton().get().getMappingHandler();
		List<Resource> resourceList = ResourceContentService.getResourceContent(dmResource.getId());
		String procedureXml = null;
		String createprocedureXml = null;
		for(Resource resource:resourceList) {
			if(ProcedureSqlType.CALL.equals(resource.getSqlType())) {
				procedureXml = resource.getContent();
			}
			if(ProcedureSqlType.CREATE.equals(resource.getSqlType())) {
				createprocedureXml = resource.getContent();
			}
		}
		try {
			handler.execute(new AddOrCoverMappingEntity(procedureXml, MappingTypeNameConstants.SQL).enableProperty(), 
					new AddOrCoverMappingEntity(createprocedureXml, MappingTypeNameConstants.PROCEDURE).enableProperty());
			SessionContext.getSqlSession().executeUpdate("DELETE FROM DM_RESOURCE_MAPPING WHERE RESOURCE_ID='"+dmResource.getId()+"'");
			DmResourceMapping dmResourceMapping = null;
			DmResourceMapping createDmResourceMapping = null;
			if(dmResource.getIsBuildModel()==0) {
				dmResourceMapping = new DmResourceMapping(dmResource.getId(),ProcedurePrefixUtil.CALL+dmResource.getResourceName(),procedureXml.replaceAll("oldName=\"([^\"]*)\"", ""),20);
				SessionContext.getTableSession().save(setBasicPropertyValues(dmResourceMapping,true));
				createDmResourceMapping = new DmResourceMapping(dmResource.getId(),dmResource.getResourceName(),createprocedureXml.replaceAll("oldName=\"([^\"]*)\"", ""),15);
				SessionContext.getTableSession().save(setBasicPropertyValues(createDmResourceMapping,true));
			}
			SessionContext.getSqlSession().executeUpdate("update DM_RESOURCE SET OLD_RESOURCE_NAME = null,IS_BUILD_MODEL=1,IS_EVER_BUILD_MODEL=1 where ID='" + dmResource.getId()+ "'");
			//dmResource.setResourceName(resourceList.get(0).getNameSpace());
			Map<String,Object> idsMap = new HashMap<String,Object>();
			idsMap.put("id", dmResource.getId());
			idsMap.put("msg", "建模成功");
			ResponseContext.addData(idsMap);
		} catch (MappingHandleException e) {
			dmResource.setIsBuildModel(0);
			SessionContext.getTableSession().update(setBasicPropertyValues(dmResource,false));
			e.printStackTrace();
			Exception exc = (Exception)e.getCause();
			if(exc.getMessage()!=null) {
				ResponseContext.addValidationFull(dmResource, "id",exc.getMessage(),null);
			}else {
				ResponseContext.addError(dmResource,e);
			}
			SessionContext.executeRollback();
		}
	}
	

   
    @Transaction 
	public void cancelModel(DmResource dmResource) {
		DmResource dr= SessionContext.getTableSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE WHERE ID='"+dmResource.getId()+"'");
	    if(dr!=null&&dr.getIsBuildModel()==0) {
	    	ResponseContext.addValidationFull(dmResource, "id", "该资源未建模，不能进行取消建模操作","smartone.dynamic.resource.noModel");
	        return;
	    }
	    try {
	    cancelmodel(dr); 
    	dr.setIsBuildModel(0);
		SessionContext.getTableSession().update(setBasicPropertyValues(dr,false));
    	Map<String,Object> idsMap = new HashMap<String,Object>();
    	idsMap.put("id", dr.getId());
		idsMap.put("msg", "取消建模成功");
		ResponseContext.addData(idsMap);
	    }catch (MappingHandleException e) {
	    	Exception exc = (Exception)e.getCause();
			if(exc.getMessage()!=null) {
				ResponseContext.addValidationFull(dmResource, "id",exc.getMessage(),null);
			}else {
				ResponseContext.addError(dmResource,e);
			}
			SessionContext.executeRollback();
		}
	}
	
	public void cancelmodel(DmResource dmResource) throws MappingHandleException {
		MappingHandler handler= SessionFactoryContainer.getSingleton().get().getMappingHandler();
		List<DmResource> dmBusiResourceList = null;
		if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_TABLE) {
			//修改mapping为失效，table不可以从容器中移除
			SessionContext.getSqlSession().executeUpdate("update DM_RESOURCE_MAPPING set IS_FAILURE=1 where RESOURCE_ID = '"+dmResource.getId()+"'");
			dmBusiResourceList = SessionContext.getTableSession().query(DmResource.class,"select * from DM_RESOURCE where id  in(select REF_BUSI_MODEL_ID from DM_CFG_BUSI_MODEL_RES_RELATIONS where REF_RESOURCE_ID='"+dmResource.getId()+"')");
    	}else if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_PROCEDURE){
    		SessionContext.getSqlSession().executeUpdate("delete from DM_RESOURCE_MAPPING where RESOURCE_ID='"+dmResource.getId()+"'");
    		handler.execute(new DeleteMappingEntity(dmResource.getResourceName()).enableProperty(),new DeleteMappingEntity(ProcedurePrefixUtil.CALL+dmResource.getResourceName()).enableProperty());
    		dmBusiResourceList = SessionContext.getTableSession().query(DmResource.class,"select * from DM_RESOURCE where id in(select REF_BUSI_MODEL_ID from DM_CFG_BUSI_MODEL_RES_RELATIONS where id in(select PARENT_ID from DM_SQL_BUSI_STRUCTURE where RESOURCE_ID ='"+dmResource.getId()+"'))\r\n" + "");
    	}else if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_SQL){
    		SessionContext.getSqlSession().executeUpdate("delete from DM_RESOURCE_MAPPING where RESOURCE_ID='"+dmResource.getId()+"'");
    		handler.execute(new DeleteMappingEntity(dmResource.getResourceName()).enableProperty());
    		dmBusiResourceList = SessionContext.getTableSession().query(DmResource.class,"select * from DM_RESOURCE where id in(select REF_BUSI_MODEL_ID from DM_CFG_BUSI_MODEL_RES_RELATIONS where id in(select PARENT_ID from DM_SQL_BUSI_STRUCTURE where RESOURCE_ID ='"+dmResource.getId()+"'))\r\n" + "");
    	}
		if(dmBusiResourceList!=null&&dmBusiResourceList.size()>0) {
			for(DmResource resource:dmBusiResourceList) {
				resource.setIsBuildModel(0);
				SessionContext.getTableSession().update(setBasicPropertyValues(resource,false));
			}
	    }
	}
	
	
	public boolean doValidate(DmResource dmResource) {
        if(StrUtil.isContainChinese(dmResource.getResourceName())) {
        	ResponseContext.addValidationFull(dmResource, "resourceName", "资源名不可以为汉字","smartone.resource.insert.resourceNameIsContainChinese");
			return false;
		}
		List<DmResource> obj =(List<DmResource>)SessionContext.getSqlSession().query(DmResource.class,"select * from DM_RESOURCE WHERE RESOURCE_NAME='"+dmResource.getResourceName().toUpperCase()+"'");
		
		if(obj!=null&&obj.size()>0){
			ResponseContext.addValidationFull(dmResource, "resourceName", "不可插入重复的资源名","smartone.resource.insert.uniqueResourceName");
			return false;
		}
		if(EnvironmentContext.getEnvironment().getDialect().getDatabaseType() == DatabaseType.ORACLE && dmResource.getResourceName().length()>30 ) {
			ResponseContext.addValidationFull(dmResource, "resourceName", "数据库表名超长","smartone.value.violation.oracleTableNameOverLength");
			return false;
		}
		return true;
	}
	
	
	
	
	@Transaction 
	public void getXmlById(String resourceId) {
		List<DmResourceMapping> list = SessionContext.getSqlSession().query(DmResourceMapping.class,"select * from DM_RESOURCE_MAPPING where RESOURCE_ID = '"+resourceId+"'");
		ResponseContext.addData(list);
	}
	
	@Transaction 
	public void createModelByXml(DmResourceMapping dmResourceMapping) {
		MappingHandler handler= SessionFactoryContainer.getSingleton().get().getMappingHandler();
		DmResource resource = SessionContext.getSqlSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE where ID = '"+dmResourceMapping.getResourceId()+"'");
	    if(resource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_TABLE) {
	    	handler.execute(new AddOrCoverMappingEntity(dmResourceMapping.getMappingContent(), MappingTypeNameConstants.TABLE).enableProperty());
	    }else if(resource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_SQL) {
	    	handler.execute(new AddOrCoverMappingEntity(dmResourceMapping.getMappingContent(), MappingTypeNameConstants.SQL).enableProperty());
	    }else if(resource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_BUSIMODEL) {
	    	handler.execute(new AddOrCoverMappingEntity(dmResourceMapping.getMappingContent(),ExMappingTypeConstants.BUSIMODEL).enableProperty());
	    }
	    SessionContext.getTableSession().update(setBasicPropertyValues(dmResourceMapping,false));
	    resource.setIsBuildModel(1);
	    SessionContext.getTableSession().update(setBasicPropertyValues(resource,false));
	    ResponseContext.addData(dmResourceMapping);
	}
}
