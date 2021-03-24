package com.smartone.ddm.query.builtin.conditions;

/**
 * 
 * @author wangShuFang
 */
public  class OrderByMethod{
	
	public static final  String _sortKey = "_sort";
	
	private String sort;

	public OrderByMethod(String sort) {
		super();
		this.sort = sort;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public OrderByMethod() {
		super();
	}
	
	private MapToObjectMethod mapToObjectMethod;
    public void setMapToObjectMethod(MapToObjectMethod mapToObjectMethod) {
    	this.mapToObjectMethod = mapToObjectMethod;
	}
	
	public String getSql() {
		if(this.sort==null) {
			return "";
		}
		String sort[] = this.sort.split(",");
		String sql = " order by ";
        if(mapToObjectMethod==null) {
    		for(int i=0;i<sort.length;i++) {
    			if(i==sort.length-1) {
    				sql+= sort[i];
    			}else {
    				sql+= sort[i]+",";
    			}
    		}
		}else {
			for(int i=0;i<sort.length;i++) {
				String sortItem[] = sort[i].trim().split(" ");
     			if(i==sort.length-1) {
    				sql+= paramToColumn(sortItem[0])+" "+sortItem[1];
    			}else {
    				sql+= paramToColumn(sortItem[0])+" "+sortItem[1]+",";
    			}
    		}
		}
		return  sql;
	}
	
	
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
}