package com.smartone.ddm.query.object.conditions;

/**
 * 
 * @author wangShuFang
 */
public  class InMethod extends SplicingConditionsUtil{
	
	private String column;
	private String name;
	
	private String[] param;
	
	
	public String[] getParam() {
		return param;
	}

	public void setParam(String[] param) {
		this.param = param;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public InMethod(String column, String name, String[] param) {
		super();
		this.column = column;
		this.name = name;
		this.param = param;
	}

	public String toDBScriptStatement() {
		StringBuilder hql = new StringBuilder();
		if(name!=null) {
			hql.append(" and "+name+"."+column +" "+notOperator+" IN (");
		}else {
			hql.append(" and "+ column +" "+notOperator+" IN (");
		}
		
		for(int i=0;i<param.length;i++) {
			if(param[i].equals("null")) {
				returnHql(i,param,hql);
			}else {
				hql.append("? ,");
				returnHql(i,param,hql);
			}		
		}
		return hql.toString();  
	}
	
//	public static String toDBScriptStatement(String column,String para[],String name) {
//		StringBuilder hql = new StringBuilder();
//		if(name!=null) {
//			hql.append(" and "+name+"."+column +" "+notOperator+" IN (");
//		}else {
//			hql.append(" and "+ column +" "+notOperator+" IN (");
//		}
//		
//		for(int i=0;i<para.length;i++) {
//			if(para[i].equals("null")) {
//				returnHql(i,para,hql);
//			}else {
//				hql.append("? ,");
//				returnHql(i,para,hql);
//			}		
//		}
//		return hql.toString();  
//    }
	
	//当元素为最后一个字符串时做的拼接操作
	public static StringBuilder returnHql(int i,String para[],StringBuilder hql) {
		if(i==para.length-1) {
			hql.deleteCharAt(hql.length()-1);
			hql.append(")");
		}
		return hql;
	}
}