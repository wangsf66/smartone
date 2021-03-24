package com.smartone.ddm.query.object.conditions;

/**
 * 
 * @author wangShuFang
 */
public  class BtnMethod extends SplicingConditionsUtil{
	
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

	public BtnMethod(String column, String name) {
		this.column = column;
		this.name = name;
	}

	public String toDBScriptStatement() {
		if(name!=null) {
			return  " and "+name+"."+column +" "+notOperator+" BETWEEN  ?  AND  ?";  
		}else {
			return  " and "+ column +" "+notOperator+" BETWEEN  ?  AND  ?";
		}	
	}
	
//	public static String toDBScriptStatement(String column,String name) {
//		if(name!=null) {
//			return  " and "+name+"."+column +" "+notOperator+" BETWEEN  ?  AND  ?";  
//		}else {
//			return  " and "+ column +" "+notOperator+" BETWEEN  ?  AND  ?";
//		}	
//    }
}