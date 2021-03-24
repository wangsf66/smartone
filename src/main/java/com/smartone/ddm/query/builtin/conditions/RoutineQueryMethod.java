package com.smartone.ddm.query.builtin.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.douglei.orm.configuration.environment.EnvironmentContext;
import com.douglei.orm.context.SessionContext;
import com.douglei.orm.dialect.DatabaseType;
import com.douglei.orm.sessionfactory.sessions.Session;
import com.douglei.orm.sessionfactory.sessions.session.sql.ExecutableSqlEntity;
import com.douglei.orm.sql.query.page.PageResult;
import com.smartone.ddm.query.object.conditions.SplicingConditionsUtil;
import com.smartone.ddm.util.ResultSetMapConvertUtil;

/**
 * 
 * @author wangShuFang
 */
public class RoutineQueryMethod {

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
	
    private String tablename;
    
    private ExecutableSqlEntity es;
	
	private Map<String,Object> requestResourceParams;
	
	public RoutineQueryMethod(Map<String, Object> requestResourceParams, String tablename) {
		this.tablename  = tablename;
		this.requestResourceParams = requestResourceParams;
	}

	//sql建模查询
	public RoutineQueryMethod(Map<String, Object> requestResourceParams, ExecutableSqlEntity es) {
		this.es = es;
		this.requestResourceParams = requestResourceParams;
	}

	public Object getDatas() {
		List<Object> paramlist = new ArrayList<Object>();
		if(es!=null &&es.getParameterValues()!=null&& es.getParameterValues().size()!=0) {
			for(Object str:es.getParameterValues()) {
				paramlist.add(str);
			}
		}
		List<Map<String, Object>> rootlist = null;
		PageResult<Map<String, Object>> pageObject = null;
		if(pageMethod==null) {
			rootlist =  SessionContext.getSqlSession().query(getSql(paramlist),paramlist);
			if(mapToObjectMethod!=null) {
				return ResultSetMapConvertUtil.resultsetListMapKey2PropertyName(rootlist);
			}
			return rootlist;
		}else {
			pageObject =  SessionContext.getSqlSession().pageQuery(pageMethod.getPage(), pageMethod.getRows(),getSql(paramlist),paramlist);
			if(mapToObjectMethod!=null&&pageObject.getResultDatas().size()!=0) {
				pageObject.setResultDatas(ResultSetMapConvertUtil.resultsetListMapKey2PropertyName(pageObject.getResultDatas()));
			}
			return pageObject;
		}
	}
	
	public Object getBusiDatas(Session session) {
		List<Object> paramlist = new ArrayList<Object>();
		if(es!=null &&es.getParameterValues()!=null && es.getParameterValues().size()!=0) {
			for(Object str:es.getParameterValues()) {
				paramlist.add(str);
			}
		}
		List<Map<String, Object>> rootlist = session.getSqlSession().query(getSql(paramlist),paramlist);
		if(mapToObjectMethod!=null) {
			return ResultSetMapConvertUtil.resultsetListMapKey2PropertyName(rootlist);
		}
		return rootlist;
	}
	
	public static String voidsql() {
		String str = "";
		if(EnvironmentContext.getEnvironment().getDialect().getDatabaseType() == DatabaseType.SQLSERVER) {
			str = " TOP 100 PERCENT ";
		}
		return str;
	}
	
	public static String getWithClause(String str) {
		if(str==null) {
			return "";
		}
		return str;
	}
	
	private String getSql(List<Object> paramlist) {
		if(tablename!=null) {
			if(orderByMethod.getSort()==null) {
			    return "Select * from "+tablename+" where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestResourceParams, paramlist,null);
			}else {
				return "Select  "+voidsql()+"  * from "+tablename+" where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestResourceParams, paramlist,null)+"  "+orderByMethod.getSql();
			}
		}else {
			if(orderByMethod.getSort()==null) {
				String sql =  getWithClause(es.getSql())+" Select * from  ( "+es.getSql()+" ) _smartOne_sq_ where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestResourceParams, paramlist,"_smartOne_sq_");
				return  sql;
			}else {
				String sql =  getWithClause(es.getSql())+" Select "+voidsql()+" * from  ( "+es.getSql()+" ) _smartOne_sq_ where 1=1 "+SplicingConditionsUtil.montageSqlParams(requestResourceParams, paramlist,"_smartOne_sq_")+"  "+orderByMethod.getSql();
				return sql;
			}
		}
	}
}
