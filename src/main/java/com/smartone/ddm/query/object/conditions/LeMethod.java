package com.smartone.ddm.query.object.conditions;

/**
 * 
 * @author wangShuFang
 */
public class LeMethod{
	private String column;
	private String name;
	
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

	public LeMethod(String column, String name) {
		this.column = column;
		this.name = name;
	}

	public String toDBScriptStatement() {
		if(name!=null) {
			return  " and "+name+"."+column +" <=  ?";    
		}else {
			return  " and "+ column +" <=  ?";    
		}
	}

	
//	public static String toDBScriptStatement(String column,String name) {
//		if(name!=null) {
//			return  " and "+name+"."+column +" <=  ?";    
//		}else {
//			return  " and "+ column +" <=  ?";    
//		}
//	}
}
