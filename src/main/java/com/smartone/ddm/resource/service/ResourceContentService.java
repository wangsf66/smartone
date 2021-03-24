package com.smartone.ddm.resource.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.douglei.orm.configuration.environment.EnvironmentContext;
import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.mapping.MappingTypeNameConstants;
import com.smartone.ddm.busimodel.entity.DmBusiModelRelation;
import com.smartone.ddm.busimodel.entity.DmSqlBusiStructure;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourceParam;
import com.smartone.ddm.resource.entity.DmSqlContent;
import com.smartone.ddm.resource.entity.Resource;
import com.smartone.ddm.resource.entity.SqlStatementTypeConstants;
import com.smartone.ddm.util.ProcedurePrefixUtil;
import com.smartone.ddm.util.ProcedureSqlType;
import com.smartone.ddm.util.ResourceTypeUtil;
import com.smartone.ex.mapping.ExMappingTypeConstants;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.ESqlStatementType;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.TStatementList;

@TransactionComponent	
public class ResourceContentService{
	
	@Transaction
	public List<Resource> getResourceContent(String resourceId) {
		List<Resource> resourceList = new ArrayList<Resource>();
		Resource resource = null;
		Resource proResource = null;
		DmResource dmResource = SessionContext.getSqlSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE WHERE ID='"+resourceId+"'");
		if(dmResource!=null) {
			if(ResourceTypeUtil.RESOURCE_TYPE_TABLE==dmResource.getResourceType()) {
				resource = new Resource();
				List<DmResourceParam> paramslist= SessionContext.getSqlSession().query(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS WHERE RESOURCE_ID='"+dmResource.getId()+"'");
				if(paramslist!=null&&paramslist.size()>0) {
					String xml = montageTableXml(dmResource,paramslist);
					resource.setTableParamsList(paramslist);
					resource.setType(MappingTypeNameConstants.TABLE);
					resource.setContent(xml);
					resource.setName(dmResource.getResourceName());
					resource.setOldName(dmResource.getOldResourceName());
					resource.setResourceId(dmResource.getId());
					if(dmResource.getOldResourceName()!=null) {
						resource.setIsUpdateName(true);
					}else {
						for(DmResourceParam dmResourceParam:paramslist){
							if(dmResourceParam.getOldParamName()!=null) {
								resource.setIsUpdateName(true);
								break;
							}
						}
					}
					resourceList.add(resource);
				}
			}else if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_BUSIMODEL) {
				resource = new Resource();
				// 查询业务结构
				List<DmBusiModelRelation> dmBusiModelResRelationsList = SessionContext.getTableSession().query(
						DmBusiModelRelation.class,
						"SELECT * FROM DM_CFG_BUSI_MODEL_RES_RELATIONS where REF_BUSI_MODEL_ID='"
								+ dmResource.getId() + "' order by order_code asc");
				// 获取父节点
				List<DmBusiModelRelation> parentList = new ArrayList<DmBusiModelRelation>();
				if(dmBusiModelResRelationsList!=null&&dmBusiModelResRelationsList.size()>0) {
					for (int i = 0; i < dmBusiModelResRelationsList.size(); i++) {
						if (dmBusiModelResRelationsList.get(i).getParentId() == null) {
							parentList.add(dmBusiModelResRelationsList.get(i));
						}
					}  
				}
				// 解析业务结构
				String busiModelXml = builTree(parentList, dmBusiModelResRelationsList,dmResource);
				resource.setType(ExMappingTypeConstants.BUSIMODEL);
				resource.setContent(busiModelXml);
				resource.setName(dmResource.getResourceName());
				resource.setOldName(dmResource.getOldResourceName());
				resource.setResourceId(dmResource.getId());
				if(dmResource.getOldResourceName()!=null) {
					resource.setIsUpdateName(true);	
				}
				resourceList.add(resource); 
			}else if(dmResource.getResourceType()==ResourceTypeUtil.RESOURCE_TYPE_PROCEDURE) {
				 resource = new Resource();
				 proResource = new Resource();
				 resource.setResourceId(dmResource.getId());
				 proResource.setResourceId(dmResource.getId());
				 analysisSqlServerProcedure(dmResource,resource,proResource);
				 resource.setType(MappingTypeNameConstants.PROCEDURE);
				 resource.setName(dmResource.getResourceName());
				 proResource.setType(MappingTypeNameConstants.PROCEDURE);
				 proResource.setName(dmResource.getResourceName());
				 resource.setOldName(dmResource.getOldResourceName());
				 proResource.setOldName(dmResource.getOldResourceName());
				 if(dmResource.getOldResourceName()!=null) {
						resource.setIsUpdateName(true);
						proResource.setIsUpdateName(true);
					}
				 resourceList.add(resource);
				 resourceList.add(proResource);
			}else {
				resource = new Resource();
				List<DmSqlContent> sqllist = analysisSqllist(dmResource);
				String xml = montageSqlXml(dmResource,sqllist);
				resource.setType(MappingTypeNameConstants.SQL);
				resource.setContent(xml);
				resource.setName(dmResource.getResourceName());
				resource.setOldName(dmResource.getOldResourceName());
				resource.setResourceId(dmResource.getId());
				if(dmResource.getOldResourceName()!=null) {
					resource.setIsUpdateName(true);	
				}
				resourceList.add(resource);
			}
		}
		return resourceList;
	}
	
	// 建立树形结构
	private String builTree(List<DmBusiModelRelation> parentList,
			List<DmBusiModelRelation> dmBusiModelResRelationsList,DmResource dmresource) {
		StringBuilder busiModelXml = new StringBuilder();
		busiModelXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		busiModelXml.append("<mapping-configuration>\n");
		busiModelXml.append("<busimodel name=\""+dmresource.getResourceName()+"\" ");
		if(dmresource.getOldResourceName()!=null) {
			busiModelXml.append(" oldName=\""+dmresource.getOldResourceName()+"\" >");
		}else {
			busiModelXml.append(">");  
		}
		if(parentList!=null&&parentList.size()>0) {
			busiModelXml.append("<relations>\n");
			for (DmBusiModelRelation dmBusiModelResRelations : parentList) {
				busiModelXml.append("<relation refBusiModelName=\""+dmresource.getResourceName()+"\" \n");
				busiModelXml.append("idPropName=\""+dmBusiModelResRelations.getIdPropName()+"\" \n");
				if(dmBusiModelResRelations.getRefResourceKeyName()!=null) {
					busiModelXml.append("refResourceKeyName=\""+dmBusiModelResRelations.getRefResourceKeyName()+"\" \n");
				}
				busiModelXml.append("refResourceType=\""+dmBusiModelResRelations.getRefResourceType()+"\" \n");
				busiModelXml.append("isCascadeDelete=\""+dmBusiModelResRelations.getIsCascadeDelete()+"\"\n");
				busiModelXml.append("orderCode=\""+dmBusiModelResRelations.getOrderCode()+"\"\n");
				if(dmBusiModelResRelations.getRefResourceType()==10) {
				   busiModelXml.append(" refResourceName=\""+dmBusiModelResRelations.getRefResourceName()+"\"> \n");
				}else {
				   busiModelXml.append("> \n");
				   List<DmSqlBusiStructure> sqlList = SessionContext.getSqlSession().query(DmSqlBusiStructure.class, "select * from DM_SQL_BUSI_STRUCTURE  where PARENT_ID='"+dmBusiModelResRelations.getId()+"'");
				   DmResource resource = null;
				   if(sqlList.size()>0) {
					   busiModelXml.append("<sqllist> \n");
					   for(DmSqlBusiStructure dmSqlBusiStructure:sqlList) {
						   resource = SessionContext.getSqlSession().uniqueQuery(DmResource.class, "select * from DM_RESOURCE  where ID='"+dmSqlBusiStructure.getResourceId()+"'");
						   busiModelXml.append("<sql name=\""+resource.getResourceName()+"\" type=\""+dmSqlBusiStructure.getType()+"\" />\n");  
					   } 
					   busiModelXml.append("</sqllist>\n");
				   }
				}
				buildChilTree(dmBusiModelResRelations, dmBusiModelResRelationsList,busiModelXml,dmresource);  
				busiModelXml.append("</relation> \n");
			}
			busiModelXml.append("</relations>\n");
		}
		busiModelXml.append("</busimodel>\n");
		busiModelXml.append("</mapping-configuration>");
		return busiModelXml.toString();
	} 

	// 递归，建立子树形结构
	private String buildChilTree(DmBusiModelRelation dmBusiModelResRelations,
			List<DmBusiModelRelation> dmBusiModelResRelationsList,StringBuilder busiModelXml,DmResource dmresource) {
			busiModelXml.append("<childlist>\n");
			for (DmBusiModelRelation menuNode : dmBusiModelResRelationsList) {
				if (menuNode.getParentId() != null && menuNode.getParentId().equals(dmBusiModelResRelations.getId())) {
					busiModelXml.append("<relation refBusiModelName=\""+dmresource.getResourceName()+"\" \n");
					busiModelXml.append("idPropName=\""+menuNode.getIdPropName()+"\" \n");
					busiModelXml.append("refResourceKeyName=\""+menuNode.getRefResourceKeyName()+"\" \n");
					busiModelXml.append("refParentResourcePropId=\""+menuNode.getRefParentResourcePropId()+"\" \n");
					busiModelXml.append("refResourceType=\""+menuNode.getRefResourceType()+"\" \n");
					busiModelXml.append("isCascadeDelete=\""+menuNode.getIsCascadeDelete()+"\"\n");
					busiModelXml.append("orderCode=\""+menuNode.getOrderCode()+"\"");
					if(menuNode.getRefResourceType()==10) {
					   busiModelXml.append(" refResourceName=\""+menuNode.getRefResourceName()+"\" >\n");
					}else {
					   busiModelXml.append(">\n");
					   List<DmSqlBusiStructure> sqlList = SessionContext.getSqlSession().query(DmSqlBusiStructure.class, "select * from DM_SQL_BUSI_STRUCTURE  where PARENT_ID='"+menuNode.getId()+"'");
					   DmResource resource = null;
					   if(sqlList.size()>0) {
					   busiModelXml.append("<sqllist>\n");
					   for(DmSqlBusiStructure dmSqlBusiStructure:sqlList) {
						   resource = SessionContext.getSqlSession().uniqueQuery(DmResource.class, "select * from DM_RESOURCE  where ID='"+dmSqlBusiStructure.getResourceId()+"'");
						   busiModelXml.append("<sql name=\""+resource.getResourceName()+"\" type=\""+dmSqlBusiStructure.getType()+"\" />\n");  
					   }
					   busiModelXml.append("</sqllist>\n");
					   }
					}
					buildChilTree(menuNode, dmBusiModelResRelationsList,busiModelXml,dmresource);
					busiModelXml.append("</relation> \n");
				}
		    }
			busiModelXml.append("</childlist>\n");
		return busiModelXml.toString();
	}
		
	private EDbVendor getDialect() {
		switch(EnvironmentContext.getEnvironment().getDialect().getDatabaseType()) {
		   case MYSQL:
			   return EDbVendor.dbvmysql;
		   case ORACLE:
			   return EDbVendor.dbvoracle;
		   case SQLSERVER:
			   return EDbVendor.dbvmssql;
		}
		throw new NullPointerException();
	}
	
	private List<DmSqlContent> analysisSqllist(DmResource dmResource) {
    	List<DmSqlContent> sqlList = new ArrayList<DmSqlContent>();
    	TGSqlParser parser = new TGSqlParser(getDialect());
    	parser.sqltext = dmResource.getContent();
	    TStatementList list = parser.sqlstatements;
	    parser.parse();
	    DmSqlContent dmSqlContent  = null;
	    //代码块
		if(transType(list.get(0).sqlstatementtype)==7) {
			dmSqlContent = new DmSqlContent(dmResource.getId(),dmResource.getContent(),EnvironmentContext.getEnvironment().getDialect().getDatabaseType().getName(),7);
			sqlList.add(dmSqlContent);
			//查询
		}else if(transType(list.get(0).sqlstatementtype)==1) {
			dmSqlContent = new DmSqlContent(dmResource.getId(),dmResource.getContent(),EnvironmentContext.getEnvironment().getDialect().getDatabaseType().getName(),1);
			sqlList.add(dmSqlContent);
		}else {
			while(list.hasNext()){
				dmSqlContent = new DmSqlContent(dmResource.getId(),list.next().toString(),EnvironmentContext.getEnvironment().getDialect().getDatabaseType().getName(),transType(list.next().sqlstatementtype));
				sqlList.add(dmSqlContent);
			}
		} 
		return sqlList;
    }
    
    public int transType(ESqlStatementType sqlStatementType) {
		switch(sqlStatementType){
        case sstselect:
           return SqlStatementTypeConstants.SELECT;
        case sstupdate:
           return  SqlStatementTypeConstants.UPDATE;
        case sstinsert:
            return  SqlStatementTypeConstants.INSERT;
        case sstdelete:
            return  SqlStatementTypeConstants.DELETE;
        case sstplsql_createprocedure:
            return  SqlStatementTypeConstants.PROCEDURE;
        case sstmssqlcreateprocedure:
        	return  SqlStatementTypeConstants.PROCEDURE;
        case sstcreateview:
        	return  SqlStatementTypeConstants.VIEW;
        default:
        	return  SqlStatementTypeConstants.DECLARE;
       }
	}
	
	private void analysisSqlServerProcedure(DmResource dmResource,Resource resource,Resource proResource) {
		List<DmResourceParam> paramList = SessionContext.getSqlSession().query(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS where RESOURCE_ID in(select id from DM_RESOURCE where id='"+dmResource.getId()+"') and PARAM_TYPE=0 order by ORDER_CODE");
		resource.setNameSpace(dmResource.getResourceName());
		proResource.setNameSpace(dmResource.getResourceName());
		String sql = joinSql(dmResource.getResourceName(),paramList);
		String procedureXml = montageProcedureXml(dmResource,sql);
		String createprocedureXml = montageCreateProcedureXml(dmResource); 
		resource.setContent(procedureXml);
		proResource.setContent(createprocedureXml);
		resource.setSqlType(ProcedureSqlType.CALL);
		proResource.setSqlType(ProcedureSqlType.CREATE);
	}
	
	    //拼接存储过程执行sql
		private String joinSql(String nameSpace,List<DmResourceParam> paramList) {
			StringBuilder sql = new StringBuilder();
			String str = "";
			sql.append("{call "+nameSpace);
			if(paramList!=null) {
				sql.append("(");
				for(DmResourceParam dmResourceParam:paramList) {
					sql.append(returnsql(dmResourceParam));
				}
				sql.setLength(sql.length()-1);
				sql.append(str);
			  }
			sql.append(")}");
			return sql.toString();
		}
			
		private String returnsql(DmResourceParam dmResourceParam) {
			StringBuilder sql = new StringBuilder();
			sql.append("$");
			//拼接参数名
			if(dmResourceParam.getParamName()!=null) {
				sql.append(dmResourceParam.getParamName()+"|");
			}
			
			if(!("".equals(dmResourceParam.getDefaultValue()))) {
				sql.append("defaultValue="+dmResourceParam.getDefaultValue()+"|");
			}
			//如果参数为内置参数，凭借参数默认值
			if(dmResourceParam.getParamName().equals("CREATE_USER_ID")||dmResourceParam.getParamName().equals("LAST_UPDATE_USER_ID")) {
				  sql.append("defaultValue=_accountId|");
			}
			if(dmResourceParam.getParamName().equals("CREATE_DATE")||dmResourceParam.getParamName().equals("LAST_UPDATE_DATE")) {
				  sql.append("defaultValue=_currentTime|");
			}
			if(dmResourceParam.getParamName().equals("PROJECT_ID")) {
				  sql.append("defaultValue=_projectId|");
			}
			if(dmResourceParam.getParamName().equals("CUSTOMER_ID")) {
				  sql.append("defaultValue=_customerId|");
			}
			
			if(dmResourceParam.getDatatype()!=null) {
				sql.append("dbType="+dmResourceParam.getDatatype().toUpperCase()+"|");
			}else {
				sql.append("dbType=VARCHAR|");
			}
			if(dmResourceParam.getLength()!=0) {
				sql.append("length="+dmResourceParam.getLength()+"|");
			}
			if(dmResourceParam.getPrecision()!=0) {
				sql.append("precision="+dmResourceParam.getPrecision()+"|");
			}
			//参数类型（输入、输出）
			if(dmResourceParam.getInout()==0||dmResourceParam.getInout()==5||dmResourceParam.getInout()==1) {
				sql.append("mode=in|");
			}else if(dmResourceParam.getInout()==4||dmResourceParam.getInout()==2) {
				sql.append("mode=out|");
			}else if(dmResourceParam.getInout()==3){
				sql.append("mode=inout|");
			}
			//能否为null
			if(sql.indexOf("mode")!=-1) {
				String params[] = sql.substring(sql.indexOf("mode")).split("\\|")[0].split("=");
				if(params[1].equals("out")||params[1].equals("inout")) {
					sql.append("nullable=false|");
				}else {
					sql.append("nullable=true|");
				}
			}else {
				sql.append("nullable=true|");
			}
			//去掉最后一个，号
			sql.setLength(sql.length()-1);
			sql.append("$,");
			return sql.toString();
		}
			
	//拼接存储过程xml
	private String montageCreateProcedureXml(DmResource dmResource){
		  StringBuilder xml = new StringBuilder();
		  xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		  xml.append("<mapping-configuration>\n");
		  xml.append("<property  supportCover=\"true\" supportDelete=\"true\" extend=\"{'type':1}\" />");
		  xml.append("<procedure name=\""+dmResource.getResourceName()+"\"");
		  if(dmResource.getOldResourceName()!=null) {
			  xml.append(" oldName=\""+dmResource.getOldResourceName()+"\" > \n");
		  }else {
			  xml.append(" >\n");
		  }
		  xml.append("<![CDATA[ "+dmResource.getContent()+"]]>");
		  xml.append("</procedure>\n");
		  xml.append("</mapping-configuration>");
		  return xml.toString();
	  }
	
	private String montageProcedureXml(DmResource dmResource,String sql){
		  StringBuilder xml = new StringBuilder();
		  xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		  xml.append("<mapping-configuration>\n");
		  xml.append("<property  supportCover=\"true\" supportDelete=\"true\" extend=\"{'type':1,'results':["+montageResults(dmResource)+"],'params':["+montageParams(dmResource)+"]}\" />");
		  xml.append("<sql namespace=\""+ProcedurePrefixUtil.CALL+dmResource.getResourceName()+"\"");
		  if(dmResource.getOldResourceName()!=null) {
			  xml.append(" oldNamespace=\""+ProcedurePrefixUtil.CALL+dmResource.getOldResourceName()+"\" >");
		  }else {
			  xml.append(">");
		  }
		  xml.append("<content name=\""+UUID.randomUUID().toString()+"\" type=\"procedure\">\n");
		  String str = sql.replaceAll("<","&lt;");
		  String stra = str.replaceAll(">","&gt;");
		  xml.append(stra);
		  xml.append("</content>\n");
		  xml.append("</sql>\n");
		  xml.append("</mapping-configuration>");
		  return xml.toString();
	  }
	//<property supportCover="false" supportDelete="false" extendExpr="{\"sqlType\":1, \"results\":[\"\", \"\"], \"params\":[\"\", \"\"]}" />
	//<property supportCover="false" supportDelete="false" extendExpr=/>
	private String montageSqlXml( DmResource dmResource,List<DmSqlContent> sqllist){
		  StringBuilder xml = new StringBuilder();
		  xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		  xml.append("<mapping-configuration>\n");
		  //普通查询
		  if("select".equals(sqllist.get(0).getType())) {
			  xml.append("<property  supportCover=\"true\" supportDelete=\"true\" extend=\"{'type':0,'params':["+montageParams(dmResource)+"],'results':["+montageResults(dmResource)+"]}\"/> \n");
		      //TODO 拼接金石磊新的查询方法 sql-query
		  }else {
			  xml.append("<property  supportCover=\"true\" supportDelete=\"true\" extend=\"{'type':2,'params':["+montageParams(dmResource)+"]}\"/>\n");
		  }
		  xml.append("<sql namespace=\""+dmResource.getResourceName()+"\">\n");
		  String str="";
		  String stra = "";
		  for(DmSqlContent sql:sqllist) {
			  xml.append("<content name=\""+UUID.randomUUID().toString()+"\" type=\""+transTypeToString(Integer.parseInt(sql.getType().toString()))+"\">\n");
			  //遇到<>号使用&gt和&lt
			  str = sql.getContent().replaceAll("<","&lt;");
			  stra = str.replaceAll(">","&gt;");
			  xml.append(stra+"\n");
			  xml.append("</content>\n");
		  }   
		  xml.append("</sql>\n");
		  String param = montageParams(dmResource);
		  if(param.length()>0) {
			  xml.append("<validators>\n");
			  String params[] = param.split(",");
			  for(String paramString:params) {
				  xml.append("<validator name=\""+paramString.replace("'","")+"\"  enableNotNull=\"true\" enableDataType=\"true\"  /> \n"); 
			  }
			  xml.append("</validators>\n");
		  }
		  xml.append("</mapping-configuration>");
		  //替换默认值
		  String resultXml = replaceDefaultParams(xml.toString());
		  return resultXml;
	  }
      
	  //$符号特殊替换方法
      private String replaceDefaultParams(String xml) {
    	  return xml.replaceAll("\\$CREATE_USER_ID\\$", "\\$CREATE_USER_ID|defaultValue=_accountId|nullable=true|dataType=string\\$")
    	  .replaceAll( "\\$LAST_UPDATE_USER_ID\\$", "\\$LAST_UPDATE_USER_ID|defaultValue=_accountId|nullable=true|dataType=string\\$")
    	  .replaceAll( "\\$CREATE_DATE\\$", "\\$CREATE_DATE|defaultValue=_currentTime|nullable=true|dataType=datetime\\$")
    	  .replaceAll( "\\$LAST_UPDATE_DATE\\$", "\\$LAST_UPDATE_DATE|defaultValue=_currentTime|nullable=true|dataType=datetime\\$")
    	  .replaceAll( "\\$PROJECT_ID\\$", "\\$PROJECT_ID|defaultValue=_projectId|nullable=true|dataType=string\\$")
    	  .replaceAll( "\\$CUSTOMER_ID\\$", "\\$CUSTOMER_ID|defaultValue=_customerId|nullable=true|dataType=string\\$");
      }
  
	private String montageParams(DmResource dmResource) {
    	List<DmResourceParam> paramList = SessionContext.getSqlSession().query(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS where RESOURCE_ID in(select id from DM_RESOURCE where id='"+dmResource.getId()+"') and PARAM_TYPE=0  ");
		StringBuilder param = new StringBuilder();
    	if(paramList!=null&&paramList.size()>0) {
			for(int i=0;i<paramList.size();i++) {
				if(i==paramList.size()-1) {
					param.append("'"+paramList.get(i).getParamName()+"'");
				}else {
					param.append("'"+paramList.get(i).getParamName()+"',");
				}
			}
		}
    	return param.toString();
	}
	
	private String montageTableParams(DmResource dmResource) {
    	List<DmResourceParam> paramList = SessionContext.getSqlSession().query(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS where RESOURCE_ID in(select id from DM_RESOURCE where id='"+dmResource.getId()+"')");
		StringBuilder param = new StringBuilder();
    	if(paramList!=null&&paramList.size()>0) {
			for(int i=0;i<paramList.size();i++) {
				if(i==paramList.size()-1) {
					param.append(paramList.get(i).getParamName());
				}else {
					param.append(paramList.get(i).getParamName()+",");
				}
			}
		}
    	return param.toString();
	}

	private String montageResults(DmResource dmResource) {
		List<DmResourceParam> paramList = SessionContext.getSqlSession().query(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS where RESOURCE_ID in(select id from DM_RESOURCE where id='"+dmResource.getId()+"')  and PARAM_TYPE=1  ");
		StringBuilder results = new StringBuilder();
    	if(paramList!=null&&paramList.size()>0) {
			for(int i=0;i<paramList.size();i++) {
				if(i==paramList.size()-1) {
					results.append("'"+paramList.get(i).getParamName()+"'");
				}else {
					results.append("'"+paramList.get(i).getParamName()+"',");
				}
			}
		}
    	return results.toString();
	}

	//createMode的值包括: none, create, drop_create, dynamic_update, 其中dynamic_update不建议在生产环境中使用
	//	<!-- dataType的值包括: string, nstring, char, nchar, number, snumber, datetime, clob, blob, 默认值为string -->
	//	<column name="id" oldName="NANE" property="id" dataType="string" length="30" precision="" nullable="true" unique="" defaultValue="" check="" fkTableName="" fkColumnName="" validate="true" />	 
	private String montageTableXml(DmResource dmResource,List<DmResourceParam> paramslist) {
		  StringBuilder xml = new StringBuilder();
		  xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		  xml.append("<mapping-configuration>\n");
		  xml.append("<property supportCover=\"true\" supportDelete=\"true\" extend=\""+montageTableParams(dmResource)+"\" />");
		  xml.append("<table name=\""+dmResource.getResourceName()+"\"");
		  if(dmResource.getOldResourceName()!=null) {
			  xml.append(" oldName=\""+dmResource.getOldResourceName()+"\"");
		  }
		  montageCreateModel(dmResource,xml);
		  xml.append(">\n");
		  
		  xml.append("<columns>\n");
		  xml.append("<column name=\"id\" property=\"id\" dataType=\"string\"  length=\"36\" />\r\n" + 
					 "<column name=\"create_user_id\" property=\"createUserId\" dataType=\"string\" length=\"36\" nullable=\"false\" />\r\n" + 
					 "<column name=\"create_date\" property=\"createDate\" dataType=\"datetime\" nullable=\"false\" />\r\n" + 
					 "<column name=\"last_update_user_id\" property=\"lastUpdateUserId\" dataType=\"string\" length=\"36\" nullable=\"false\" />\r\n" + 
					 "<column name=\"last_update_date\" property=\"lastUpdateDate\" dataType=\"datetime\" nullable=\"false\" />\r\n" + 
					 "<column name=\"project_id\" property=\"projectId\" dataType=\"string\" length=\"36\" nullable=\"false\" />\r\n" + 
					 "<column name=\"customer_id\" property=\"customerId\" dataType=\"string\" length=\"36\" nullable=\"false\" />\n");
		  for(DmResourceParam dmResourceParams:paramslist) {
			  xml.append("<column name=\""+dmResourceParams.getParamName()+"\"  description=\""+dmResourceParams.getDescName()+"\" dataType=\""+dmResourceParams.getDatatype().toLowerCase() +"\"");
			  //数据库列名的最大长度为8000（varchar）
			  if(dmResourceParams.getLength()>8000) {
				  xml.append(" length=\"8000\" ");
			  }else {
				  xml.append(" length=\""+dmResourceParams.getLength()+"\" ");
			  }
			  if(dmResourceParams.getOldParamName()!=null) {
				  xml.append(" oldName=\""+dmResourceParams.getOldParamName()+"\" ");
			  }
			  montageCondition(dmResourceParams,xml);
			  xml.append("/>\n");
		  }
		  xml.append("</columns>\n");
		  xml.append("<constraints>\n");
		  xml.append("<constraint type=\"PRIMARY_KEY\"> \n");
	      xml.append("<column name=\"ID\" /> \n");
	      xml.append("</constraint> \n");
		  for(DmResourceParam dmResourceParams:paramslist) {
			  if(dmResourceParams.getDefaultValue()!=null&&!dmResourceParams.getDefaultValue().equals("")) {
				  xml.append("<constraint type=\"DEFAULT_VALUE\"> \n");
			      		xml.append("<column name=\""+dmResourceParams.getParamName().toUpperCase()+"\" /> \n");
			      xml.append("</constraint> \n");	 
			  }
			  if(dmResourceParams.getIsUnique()==1) {
				  xml.append("<constraint type=\"UNIQUE\"> \n");
		      		xml.append("<column name=\""+dmResourceParams.getParamName().toUpperCase()+"\" /> \n");
		          xml.append("</constraint> \n");	 
			  }
		  }
		  xml.append("</constraints>\n");
		  xml.append("<validators>\n");
		  for(DmResourceParam dmResourceParams:paramslist) {
			  xml.append("<validator name=\""+dmResourceParams.getParamName().toUpperCase()+"\"  enableNotNull=\"true\" enableDataType=\"true\" /> \n");
		  }
		  xml.append("</validators>\n");
		  xml.append("</table>\n");
		  xml.append("</mapping-configuration>");
		  return xml.toString();
	  }
	
	private void montageCreateModel(DmResource dmResource,StringBuilder xml) {
		  switch(dmResource.getCreateModel()) {
		     case 0:
		        xml.append(" createMode=\"none\"");
	        break;
	     case 1:
		    xml.append(" createMode=\"create\"");
		    break;
	     case 2:
			xml.append(" createMode=\"drop_create\"");
			break;
	     case 3:
			xml.append(" createMode=\"dynamic_update\"");
				break;
		  }
	  }
	
	private void montageCondition(DmResourceParam dmResourceParams,StringBuilder xml) {
		  if(dmResourceParams.getPrecision()!=0 && dmResourceParams.getDatatype().equals("number")) {
			  if(dmResourceParams.getPrecision()>38) {
				  xml.append(" precision=\"38\"");  
			  }else {
				  xml.append(" precision=\""+dmResourceParams.getPrecision()+"\"");  
			  }
		  }
		  if(dmResourceParams.getIsNullable()==1) {
			  xml.append(" nullable=\"true\"");  
		  }
 	  }
	
	private String transTypeToString(int a) {
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
}
