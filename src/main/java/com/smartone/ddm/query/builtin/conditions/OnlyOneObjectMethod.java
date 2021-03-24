package com.smartone.ddm.query.builtin.conditions;

import java.util.List;
import java.util.Map;

import com.douglei.orm.context.SessionContext;
import com.smartone.ddm.util.ResultSetMapConvertUtil;

/**
 * 
 * @author wangShuFang
 */
public class OnlyOneObjectMethod {
	
    public static final  String _onlyOneObject = "_onlyOneObject";
	
	private String onlyOneObject;
	
	private String tablename;
	
	private Map<String,Object> requestResourceParams;

	public String getOnlyOneObject() {
		return onlyOneObject;
	}

	public void setOnlyOneObject(String onlyOneObject) {
		this.onlyOneObject = onlyOneObject;
	}
	
	public OnlyOneObjectMethod(String onlyOneObject, Map<String, Object> requestResourceParams, String tableName) {
		super();
		this.onlyOneObject = onlyOneObject;
		this.tablename = tableName;
		this.requestResourceParams = requestResourceParams;
	}
	
	private MapToObjectMethod mapToObjectMethod;
    public void setMapToObjectMethod(MapToObjectMethod mapToObjectMethod) {
    	this.mapToObjectMethod = mapToObjectMethod;
	}

	public Object getDatas() {
		List<Map<String, Object>> rootlist  =  SessionContext.getSqlSession().query("select * from "+tablename+" where ID = '"+requestResourceParams.get("ID")+"'");
		if(rootlist.size()>0) {
			if(mapToObjectMethod==null) {
				return rootlist.get(0);
			}else {
				return ResultSetMapConvertUtil.resultsetListMapKey2PropertyName(rootlist).get(0);
			}
		}else {
			return null;
		}
	}
}
