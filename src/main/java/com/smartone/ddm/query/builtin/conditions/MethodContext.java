package com.smartone.ddm.query.builtin.conditions;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.douglei.orm.sessionfactory.sessions.Session;
import com.douglei.orm.sessionfactory.sessions.session.sql.ExecutableSqlEntity;
import com.douglei.tools.StringUtil;

/**
 * 
 * @author wangShuFang
 */
public class MethodContext {
    //排序
	private OrderByMethod orderByMethod;
	//分页
    private PageMethod pageMethod;
    //递归查询
    private RecursiveMethod recursiveMethod;
    //常规查询
    private RoutineQueryMethod routineQueryMethod;
    
    //map转换为对象
    private MapToObjectMethod mapToObjectMethod;
    
    //返回单条数据
    private OnlyOneObjectMethod onlyOneObjectMethod;
    
    
    //普通对象查询
	public MethodContext(Map<String,Object> requestBuiltinParams,Map<String,Object> requestResourceParams,Map<String,Object> requestParentResourceParams,String tablename) {
		setMapToObjectMethod(requestBuiltinParams,requestResourceParams,requestParentResourceParams);
		setOnlyOneObjectMethod(requestBuiltinParams,tablename,requestResourceParams);
		setOrderByMethod(requestBuiltinParams);
		setPageMethod(requestBuiltinParams);
		setRecursiveMethod(requestBuiltinParams,tablename,requestResourceParams,requestParentResourceParams);
		setRoutineQueryMethod(requestResourceParams,tablename);
	}
	
	//sql建模查询
	public MethodContext(Map<String, Object> requestBuiltinParams, Map<String, Object> requestResourceParams,
			Map<String, Object> requestParentResourceParams, ExecutableSqlEntity es) {	
		setMapToObjectMethod(requestBuiltinParams,requestResourceParams,requestParentResourceParams);
		setOrderByMethod(requestBuiltinParams);
		setPageMethod(requestBuiltinParams);
		setRoutineQueryMethodSql(requestResourceParams,es);
		setRecursiveMethodSql(requestBuiltinParams,es,requestResourceParams,requestParentResourceParams);
	}
	
	
	//业务对象表查询
	public MethodContext(Map<String,Object> requestResourceParams,String tablename) {
		setOrderByMethod(requestResourceParams);
		setRoutineQueryMethod(requestResourceParams,tablename);
	}
	
	//业务对象sql查询
	public MethodContext(Map<String, Object> requestResourceParams, ExecutableSqlEntity es) {
		setOrderByMethod(requestResourceParams);
		setRoutineQueryMethodSql(requestResourceParams,es);
	}
	
	public MethodContext(Map<String,Object> requestResourceParams,Map<String, Object> requestBuiltinParams,String tablename) {
		setOrderByMethod(requestBuiltinParams);
		setRoutineQueryMethod(requestResourceParams,tablename);
	}
	
	//业务对象sql查询
	public MethodContext(Map<String, Object> requestResourceParams,Map<String, Object> requestBuiltinParams, ExecutableSqlEntity es) {
		setOrderByMethod(requestBuiltinParams);
		setRoutineQueryMethodSql(requestResourceParams,es);
	}
	
	
	

	private void setRecursiveMethodSql(Map<String, Object> requestBuiltinParams, ExecutableSqlEntity es,
			Map<String, Object> requestResourceParams, Map<String, Object> requestParentResourceParams) {
		String recursive = (String)requestBuiltinParams.get(RecursiveMethod._recursive);
		String pcName = (String)requestBuiltinParams.get(RecursiveMethod._pcName);
		String  deep = (String)requestBuiltinParams.get(RecursiveMethod._deepKey);
		if("true".equals(recursive)) {
			recursiveMethod = new RecursiveMethod(Integer.parseInt(deep),pcName,es,requestResourceParams,requestParentResourceParams);
			if(orderByMethod!=null) {
				recursiveMethod.setOrderByMethod(orderByMethod);
			}
			if(pageMethod!=null) {
				recursiveMethod.setPageMethod(pageMethod);
			}
			if(mapToObjectMethod!=null) {
				recursiveMethod.setMapToObjectMethod(mapToObjectMethod);
			}
		}	
	}

	private void setRoutineQueryMethodSql(Map<String, Object> requestResourceParams, ExecutableSqlEntity es) {
		 routineQueryMethod = new RoutineQueryMethod(requestResourceParams,es);
		 if(pageMethod!=null) {
			 routineQueryMethod.setPageMethod(pageMethod);
		 }
		 if(orderByMethod!=null) {
			 routineQueryMethod.setOrderByMethod(orderByMethod);
		 }
		 if(mapToObjectMethod!=null) {
			 routineQueryMethod.setMapToObjectMethod(mapToObjectMethod);
		 }
		
	}
    
	//属性转化
	private void setMapToObjectMethod(Map<String, Object> requestBuiltinParams,Map<String,Object> requestResourceParams,Map<String,Object> requestParentResourceParams) {
		String mapToObject = (String)requestBuiltinParams.get(MapToObjectMethod._mapToObject);
		if(mapToObject!=null && "true".equals(mapToObject)) {
			 mapToObjectMethod = new MapToObjectMethod(mapToObject);
			 List<String> keyList = new ArrayList<String>();
			 List<String> parentKeyList = new ArrayList<String>();
			 
			 Map<String, Object> childParams = null;
			 if(requestResourceParams.size()>0) {
				 childParams = new HashMap<String, Object>();
				 Iterator<Map.Entry<String, Object>> iterator = requestResourceParams.entrySet().iterator();
				 Map.Entry<String, Object> entry;
				 while (iterator.hasNext()) {
				     entry = iterator.next();
				     childParams.put(paramToColumn(entry.getKey()), entry.getValue());
				     keyList.add(entry.getKey());
				 }
			 }
			 
			 for(String key:keyList) {
				 requestResourceParams.remove(key);
			 }
			 
			 if(childParams!=null&&childParams.size()>0) {
				 childParams.forEach((key, value) -> {
					 requestResourceParams.put(key, value);
			     });
			 }
			 
			 Map<String, Object> parentParams = null;
			 if(requestParentResourceParams.size()>0) {
				 parentParams = new HashMap<String, Object>();
				 Iterator<Map.Entry<String, Object>> iterator = requestParentResourceParams.entrySet().iterator();
				 Map.Entry<String, Object> entry;
				 while (iterator.hasNext()) {
				     entry = iterator.next();
				     parentParams.put(paramToColumn(entry.getKey()), entry.getValue());
				     parentKeyList.add(entry.getKey());
				 }
			 }
			 
			 for(String key:parentKeyList) {
				 requestParentResourceParams.remove(key);
			 }
			 
			 if(parentParams!=null&&parentParams.size()>0) {
				 parentParams.forEach((key, value) -> {
					 requestParentResourceParams.put(key, value);
			     });
			 }
		 }
	} 
	
	//将属性名转为数据库参数
		public  String paramToColumn(String key) {
			StringBuilder  param = new StringBuilder();
			char []keys = key.toCharArray();
			for(int i=0;i<keys.length;i++) {
				if(i==keys.length-1) {
					param.append(keys[i]);
				}else {
					if(Character.isUpperCase(keys[i])) {
						param.append("_"+keys[i]);
					}else {
						param.append(keys[i]);
					}
				}
			}
			return param.toString().toUpperCase();
		}

	
	//查询时，结果集为数组，当只有一条数据时，返回一个对象
	private void setOnlyOneObjectMethod(Map<String, Object> requestBuiltinParams,String tableName,Map<String, Object> requestResourceParams) {
		String onlyOneObject = (String)requestBuiltinParams.get(OnlyOneObjectMethod._onlyOneObject);
		if(onlyOneObject!=null && "true".equals(onlyOneObject)) {
			 onlyOneObjectMethod = new OnlyOneObjectMethod(onlyOneObject,requestResourceParams,tableName);
			 if(mapToObjectMethod!=null) {
				 onlyOneObjectMethod.setMapToObjectMethod(mapToObjectMethod);
			 }
		}
	}
	
	
    
	private void setRoutineQueryMethod(Map<String, Object> requestResourceParams, String tablename) {
		 routineQueryMethod = new RoutineQueryMethod(requestResourceParams,tablename);
		 if(pageMethod!=null) {
			 routineQueryMethod.setPageMethod(pageMethod);
			 }
		 if(orderByMethod!=null) {
			 routineQueryMethod.setOrderByMethod(orderByMethod);
		 }
		 if(mapToObjectMethod!=null) {
			 routineQueryMethod.setMapToObjectMethod(mapToObjectMethod);
		 }
	 }


	public void setPageMethod(Map<String,Object> requestBuiltinParams) {
		String rows = (String)requestBuiltinParams.get(PageMethod._rowsKey);
		String pageKey =(String) requestBuiltinParams.get(PageMethod._pageKey);
		if(rows!=""&&rows!=null&&pageKey!=""&&pageKey!=null) {
			int row = Integer.parseInt(rows);
			int page = Integer.parseInt(pageKey);
			if(row>0 && page>0) {
				 pageMethod = new PageMethod(row,page);
			}
		}
	}
	
	
	public void setRecursiveMethod(Map<String,Object> requestBuiltinParams,String tablename,Map<String,Object> requestResourceParams,Map<String,Object> requestParentResourceParams) {
		String recursive =(String) requestBuiltinParams.get(RecursiveMethod._recursive);
		String pcName =(String) requestBuiltinParams.get(RecursiveMethod._pcName);
		String  deep = (String)requestBuiltinParams.get(RecursiveMethod._deepKey);
		if("true".equals(recursive)) {
			if(StringUtil.isEmpty(pcName)) {
				pcName = "PID";
			}
			recursiveMethod = new RecursiveMethod(Integer.parseInt(deep),pcName,tablename,requestResourceParams,requestParentResourceParams);
			if(orderByMethod!=null) {
				recursiveMethod.setOrderByMethod(orderByMethod);
			}
			if(pageMethod!=null) {
				recursiveMethod.setPageMethod(pageMethod);
			}
			if(mapToObjectMethod!=null) {
				recursiveMethod.setMapToObjectMethod(mapToObjectMethod);
			}
		}	
	}
	
	public void setOrderByMethod(Map<String,Object> requestBuiltinParams) {
		String sort =(String)requestBuiltinParams.get(OrderByMethod._sortKey);
		if(!"".equals(sort)) {
			orderByMethod = new OrderByMethod(sort);
			if(mapToObjectMethod!=null) {
				orderByMethod.setMapToObjectMethod(mapToObjectMethod);
			}
		}
	}

	public Object getDatas() {
		if(onlyOneObjectMethod!=null) {
			//查询一条数据
			return onlyOneObjectMethod.getDatas();
		}
		if(recursiveMethod!=null) {
			//递归查询
			return recursiveMethod.getDatas();
		}
		if(recursiveMethod==null) {
			//普通查询
			return routineQueryMethod.getDatas();
		}
		return null;
	}
	
	public Object getBusiDatas(Session session) {
		if(routineQueryMethod!=null) {
			return routineQueryMethod.getBusiDatas(session);
		}
		return null;
	}
}
