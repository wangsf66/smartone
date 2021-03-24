package com.smartone.ddm.query.builtin.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.douglei.orm.configuration.environment.EnvironmentContext;
import com.douglei.orm.context.SessionContext;
import com.douglei.orm.dialect.DatabaseType;
import com.douglei.orm.sessionfactory.sessions.session.sql.ExecutableSqlEntity;
import com.douglei.orm.sessionfactory.sessions.sqlsession.RecursiveEntity;
import com.douglei.orm.sql.query.page.PageResult;
import com.smartone.ddm.query.object.conditions.SplicingConditionsUtil;
import com.smartone.ddm.util.ResultSetMapConvertUtil;

/**
 * 
 * @author wangShuFang
 */
public  class RecursiveMethod{
	
	public static final  String _deepKey = "_deep";
	
	public static final  String _pcName = "_pcName";
	
	public static final  String _recursive = "_recursive";
	
	private int deep;
	
	private String  pcName;
	
	//是否是第一次调用，默认是false
	private boolean executeFirst = false;
	
	private String tablename;
	
	private ExecutableSqlEntity	 es;
	
	private int paramSize = 0;
	
	private Map<String,Object> requestResourceParams;
	
	private Map<String,Object> requestParentResourceParams;

	public RecursiveMethod(int deep, String pcName,String tablename,Map<String,Object> requestResourceParams,Map<String,Object> requestParentResourceParams) {
		super();
		this.deep = deep;
		this.pcName = pcName==null?"PARENT_ID":pcName.toUpperCase();
		this.tablename  = tablename;
		this.requestResourceParams = requestResourceParams;
		this.requestParentResourceParams = requestParentResourceParams;
	}
    public RecursiveMethod(int deep, String pcName, ExecutableSqlEntity es, Map<String, Object> requestResourceParams,
			Map<String, Object> requestParentResourceParams) {
    	this.deep = deep;
		this.pcName = pcName==null?"PARENT_ID":pcName.toUpperCase();
		this.es  = es;
		this.requestResourceParams = requestResourceParams;
		this.requestParentResourceParams = requestParentResourceParams;
	}
	//排序组件
	private OrderByMethod orderByMethod;
	public void setOrderByMethod(OrderByMethod orderByMethod) {  
		this.orderByMethod = orderByMethod;
	}
	//分页组件
	private PageMethod pageMethod;
	public void setPageMethod(PageMethod pageMethod) {
		this.pageMethod = pageMethod;
	}
	
	private MapToObjectMethod mapToObjectMethod;
    public void setMapToObjectMethod(MapToObjectMethod mapToObjectMethod) {
    	this.mapToObjectMethod = mapToObjectMethod;
	}
    
	//执行sql，返回结果集
	public Object getDatas() {
		List<Object> paramlist = new ArrayList<Object>();
		if(es!=null &&es.getParameterValues()!=null&& es.getParameterValues().size()!=0) {
			for(Object str:es.getParameterValues()) {
				paramlist.add(str);
			}
		}
		paramSize = paramlist.size();
		List<Map<String, Object>> rootlist = null;
		PageResult<Map<String, Object>> pageObject = null;
		if(pageMethod==null) {
			rootlist = SessionContext.getSqlSession().query(getSql(paramlist,null),paramlist);
			removeParam(paramlist,paramSize);
			recursiveQuery(rootlist,paramlist);
			if(mapToObjectMethod!=null) {
				return ResultSetMapConvertUtil.resultsetListMapKey2PropertyName(rootlist);
			}
			return rootlist;
		}else {
			pageObject = SessionContext.getSqlSession().pageQuery(pageMethod.getPage(), pageMethod.getRows(),getSql(paramlist,null),paramlist);
			removeParam(paramlist,paramSize);
			recursiveQuery(pageObject.getResultDatas(),paramlist);
			if(mapToObjectMethod!=null&&pageObject.getResultDatas().size()!=0) {
				pageObject.setResultDatas(ResultSetMapConvertUtil.resultsetListMapKey2PropertyName(pageObject.getResultDatas()));
			}
			return pageObject;
		}
	}
	
    private List<Object> removeParam(List<Object> paramlist,int paramSize){
    	List<Object> items = new ArrayList<Object>();
    	if(paramSize==0) {
    		paramlist.clear();
    	}else {
    		for(int i=0;i<paramlist.size();i++) {
        		if(i>paramSize-1) {
        			items.add(paramlist.get(i));
        		}
        	}
    		for(int i=0;i<paramlist.size();i++) {
    			for(int j=0;j<items.size();j++) {
    				if(paramlist.get(i)==items.get(j)) {
            			paramlist.remove(i);
            		}
    			}
        	}
    	}
    	return paramlist;
    }
	
	//递归方法
	private void recursiveQuery(List<Map<String, Object>> list,List<Object> paramlist) {
		if(list.size()==0) {
			return;
		}else {
			List<String> idlist = null;
			removeParam(paramlist,paramSize);
			if(list.size()>0&&(this.deep==-1||this.deep-1>0)) {
			   RecursiveEntity recursiveEntity  = new RecursiveEntity();
			   recursiveEntity.setDeep(this.deep-1);
			   recursiveEntity.setChildren("children");
			   recursiveEntity.setParentColumn(pcName);
			   recursiveEntity.setColumn("ID");
			   recursiveEntity.setValue(getIdList(list,idlist));
			   List<Map<String, Object>> childlist = SessionContext.getSqlSession().recursiveQuery(recursiveEntity, getSql(paramlist,list),paramlist);
			   addchild(list,childlist);
			}
		}
	}
	
	//为父节点添加子节点
	public void addchild(List<Map<String, Object>> list,List<Map<String, Object>> childlist) {
		List <Map<String, Object>> listObject= null;
		for(int i=0;i<list.size();i++) {
			listObject = new ArrayList<>();
			for(int j=0;j<childlist.size();j++) {
				//TODO
				if(childlist.get(j).get(pcName).toString().trim().equals(list.get(i).get("ID"))) {
					listObject.add(childlist.get(j));
						if(mapToObjectMethod!=null) {
				 			list.get(i).put("children", ResultSetMapConvertUtil.resultsetListMapKey2PropertyName(listObject));
				 		}else {
				 			list.get(i).put("children", listObject);
				 		}
    			  }
			 }
		}		
	}
	
	public static String getWithClause(String str) {
		if(str==null) {
			return "";
		}
		return str;
	}
	
	//sql语句
	private String getSql(List<Object> paramlist,List<Map<String, Object>> list) {
		if(executeFirst) {
		    return getNextSql(list,paramlist);
		}
		return getFirstSql(paramlist);
	}
    
	//递归除第一次后面的语句
	private String getNextSql(List<Map<String, Object>> list,List<Object> paramlist){
		String startSql = "Select TOP 100 PERCENT * from "+tablename;
		if(tablename!=null) {
			if(orderByMethod.getSort()==null) {
			    String sql = startSql+"  where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestResourceParams, paramlist,null);
			    return sql;
			}else {
				return startSql+"  where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestResourceParams, paramlist,null)+orderByMethod.getSql();
			}
		}else {
			if(orderByMethod.getSort()==null) {
				return	getWithClause(es.getSql())+" Select * from  ( "+es.getSql()+" ) _smartOne_sq_  where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestResourceParams, paramlist,"_smartOne_sq_");
			}else {
				return  getWithClause(es.getSql())+" Select TOP 100 PERCENT * from  ( "+es.getSql()+" ) _smartOne_sq_  where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestResourceParams, paramlist,"_smartOne_sq_")+orderByMethod.getSql();  
			}
		}
	}
	
	public static String voidsql() {
		String str = "";
		if(EnvironmentContext.getEnvironment().getDialect().getDatabaseType() == DatabaseType.SQLSERVER) {
			str = " TOP 100 PERCENT ";
		}
		return str;
	}
    
	   //递归首次执行
	   private String getFirstSql(List<Object> paramlist) {
			String sql = "";
			if(tablename!=null) {
				if(requestParentResourceParams.isEmpty()) {
					sql = "Select "+voidsql()+" * from "+ tablename +"  where ( "+pcName+" is null or "+pcName+" ='') " +SplicingConditionsUtil.montageSqlParams(requestParentResourceParams, paramlist,null)+"  "+orderByMethod.getSql();
				}else {
					String pId =(String)requestParentResourceParams.remove(pcName);
					if("null".equalsIgnoreCase(pId)) {
						sql = "Select "+voidsql()+" * from "+ tablename +"  where ( "+pcName+" is null or "+pcName+" ='') " +SplicingConditionsUtil.montageSqlParams(requestParentResourceParams, paramlist,null)+"  "+orderByMethod.getSql();
					}else if(null==pId){
						sql = "Select "+voidsql()+" * from "+ tablename +" where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestParentResourceParams, paramlist,null)+"  "+orderByMethod.getSql();
					}else {
						sql = "Select "+voidsql()+" * from "+ tablename +"  where "+pcName+" = '"+pId+"' "+SplicingConditionsUtil.montageSqlParams(requestParentResourceParams, paramlist,null)+"  "+orderByMethod.getSql();
					}
				}	
			}else {
				if(requestParentResourceParams.isEmpty()) {
					sql = getWithClause(es.getSql())+" Select "+voidsql()+" * from  ( "+es.getSql()+" ) _smartOne_sq_  where ( "+pcName+" is null or "+pcName+" ='') " +SplicingConditionsUtil.montageSqlParams(requestParentResourceParams, paramlist,"_smartOne_sq_")+"  "+orderByMethod.getSql();
				}else {
					String pId = (String)requestParentResourceParams.remove(pcName);
					if("null".equalsIgnoreCase(pId)) {
						sql = getWithClause(es.getSql())+" Select "+voidsql()+" * from  ( "+es.getSql()+" ) _smartOne_sq_  where ( "+pcName+" is null or "+pcName+" ='') " +SplicingConditionsUtil.montageSqlParams(requestParentResourceParams, paramlist,"_smartOne_sq_")+"  "+orderByMethod.getSql();
					} else if(null==pId){
						sql = getWithClause(es.getSql())+" Select "+voidsql()+" * from  ( "+es.getSql()+" ) _smartOne_sq_  where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestParentResourceParams, paramlist,"_smartOne_sq_")+"  "+orderByMethod.getSql();
					}else {
						sql = getWithClause(es.getSql())+" Select "+voidsql()+" * from  ( "+es.getSql()+" ) _smartOne_sq_  where "+pcName+" = '"+pId+"' "+SplicingConditionsUtil.montageSqlParams(requestParentResourceParams, paramlist,"_smartOne_sq_")+"  "+orderByMethod.getSql();
					}
				}	
			}
			executeFirst = true;
			return sql;
		}
	
	//拼接？号
	public String getParam(List<Map<String, Object>> list,List<Object> paramlist) {
		String sql = "";
		for(int i=0;i<list.size();i++) {
			if(i==list.size()-1) {
				sql+= " ?";
			}else {
				sql+= "? ,";
			}
			paramlist.add(list.get(i).get("ID")); 
		}	
		return  sql;
	}	
	
    //获取父级参数id列
	public List<String> getIdList(List<Map<String, Object>> list,List<String> idlist) {
		idlist = new ArrayList<String>();
		for(int i=0;i<list.size();i++) {
			idlist.add((String)list.get(i).get("ID")); 
		}	
		return  idlist;
	}	
}