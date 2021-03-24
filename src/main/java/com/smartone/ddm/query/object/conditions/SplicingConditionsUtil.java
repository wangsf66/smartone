package com.smartone.ddm.query.object.conditions;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author wangShuFang
 */
public class SplicingConditionsUtil{
	
	public static String notOperator = "";
	
	//判断会否取反
	public static Boolean isInversion(String params) {
			if(params.startsWith("!")) {
				notOperator = "not";
				return true;
			}
			notOperator = "";
			return false;
	}
		
    //判断会否为空
	public static boolean valueIsNullStr(String params) {
			if(params.equals(null)){
				return true;
			}
			return false;
	}
	
	//获取方法名
	public static String getMethodName(String params) {
		if(isInversion(params)) {
			return params.substring(1,params.indexOf("("));
		}else {
			return params.substring(0,params.indexOf("("));
		}
	}
	
	//解析URL
	public static String montageSqlParams(Map<String, Object> requestResourceParams, List<Object> paramlist,String name) {
		String sql = "";
		if(requestResourceParams.size()!= 0) {
			sql +=  executeObjectSql(requestResourceParams,paramlist,name);
		}
		return sql;
	}
	
	//拼接对象条件sql
	public static String executeObjectSql(Map<?, ?> paramMap,List<Object> paramList,String name){
		StringBuilder sql = new StringBuilder();
	    paramMap.forEach((key, value) -> {
	    	String param = value.toString().trim();
	    	String column = key.toString().trim();
            if(param.contains("(")) {
		    	//获取方法中的参数
				String para = param.substring(param.indexOf("(")+1,param.indexOf(")"));
				//参数为空                
				if(valueIsNullStr(para)) {
					if(name==null) {
						sql.append(" and "+ column +" is null "); 
					}else {
						sql.append(" and "+name+"."+column+" is null"); 
					}
				}else{
					sql.append(montageSql(param,column,para,sql,paramList,name)); 	
				}	
			}else {
				if(name==null) {
					sql.append(" and "+ column +" =  ? "); 	 
				}else {
					sql.append(" and "+name+"."+column +" =  ? "); 	
				}
				paramList.add(param);
			}
        });
		return sql.toString();
    }	
	
	//参数为一个时
	//eq("")，ge("")，gt("")，le("")，lt("")，ne("")，in("")，like("")，btn("")
	//参数为多个时
	//in("",""),btn("","")
	public static String montageSql(String value,String column,String para,StringBuilder sql,List<Object> paramList,String name) {
		String sqls = sql.toString();
		//将参数分割为数组，以“，”号分割
		//isInversion(value) 值为true表示取反 false，反之
		String param[] = para.split(",");
		for(String str:param) {
			if(!str.equals("null")){
				paramList.add(str);
			}
		}
		//将函数名全部转为小写
		switch (getMethodName(value).toLowerCase()) {
		        case "eq":
		        	if(param.length ==1) {
			    		if(isInversion(value)){
			    			sqls = new NeMethod(column,name).toDBScriptStatement(); 
			    		}else {
			    			sqls = new EqMethod(column,name).toDBScriptStatement();
			    		}
			    	}else {
			    		isInversion(value);
			    		sqls = new InMethod(column,name,param).toDBScriptStatement();
			    	}
		            break;
	            case "ne":
	            	if(param.length ==1) {
	            		if(isInversion(value)){
		    	    		sqls = new EqMethod(column,name).toDBScriptStatement();
		    			}else {
		    				sqls = new NeMethod(column,name).toDBScriptStatement();
		    			}
			    	}else {
			    		isInversion(value);
			    		sqls = new InMethod(column,name,param).toDBScriptStatement();
			    	}
		             break;
		        case "gt":
		        	if(isInversion(value)){
			    		sqls = new LtMethod(column,name).toDBScriptStatement();
					}else {
						sqls = new GtMethod(column,name).toDBScriptStatement();
					}
		            break;
		        case "ge":
		        	if(isInversion(value)){
			    		sqls = new LeMethod(column,name).toDBScriptStatement();
					}else {
						sqls = new GeMethod(column,name).toDBScriptStatement();
					}
		             break;
	            case "le":
	            	if(isInversion(value)){
	    	    		sqls = new GeMethod(column,name).toDBScriptStatement();
	    			}else {
	    				sqls = new LeMethod(column,name).toDBScriptStatement();
	    			}
		             break;
		        case "lt":
		        	if(isInversion(value)){
			    		sqls = new GtMethod(column,name).toDBScriptStatement(); 
					}else {
						sqls =  new LtMethod(column,name).toDBScriptStatement();
					}
		            break;
		        case "in":
		        	if(param.length ==1) {
			    		if(isInversion(value)){
			    			sqls = new NeMethod(column,name).toDBScriptStatement();
			    		}else {
			    			sqls = new EqMethod(column,name).toDBScriptStatement();
			    		}
			    	}else {
			    		isInversion(value);
			    		sqls = new InMethod(column,name,param).toDBScriptStatement();
			    	}
		            break;
	            case "btn":
	            	if(param.length ==1) {
	    	    		if(isInversion(value)){
	    	    			sqls = new GeMethod(column,name).toDBScriptStatement();
	    	    		}else {
	    	    			sqls = new LtMethod(column,name).toDBScriptStatement();
	    	    		}
	    	    	}else {
	    	    		isInversion(value);
	    	    		sqls = new BtnMethod(column,name).toDBScriptStatement();
	    	    	}
		             break;
		        case "between":
		        	if(param.length ==1) {
			    		if(isInversion(value)){
			    			sqls = new GeMethod(column,name).toDBScriptStatement();
			    		}else {
			    			sqls =  new LtMethod(column,name).toDBScriptStatement(); 
			    		}
			    	}else {
			    		isInversion(value);
			    		sqls = new BtnMethod(column,name).toDBScriptStatement();
			    	}
		            break; 
		         case "ctn":
		        	 isInversion(value);
		 	    	 sqls = new LikeMethod(column,name).toDBScriptStatement();
			         break;
		         case "contains": 
		        	 isInversion(value);
		 	    	 sqls = new LikeMethod(column,name).toDBScriptStatement();
			         break;	
		}
		return sqls;
	}	
}
