package com.smartone.ddm.query.builtin.conditions;


/**
 * 
 * @author wangShuFang
 */
public  class PageMethod{
	public static final  String  _rowsKey = "_rows";
	public static final  String  _pageKey = "_page";
	private int rows;
	private int  page;
	
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	
	public PageMethod(int rows, int page) {
		super();
		this.rows = rows;
		this.page = page;
	}
	
	public PageMethod() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Object getDatas() {
		// TODO Auto-generated method stub
		return null;
	}
}