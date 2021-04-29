package com.smartone.ddm.resource.entity;

import gudusoft.gsqlparser.ESqlStatementType;

/**
 * 
 * @author wangShuFang
 */
public class SqlStatementTypeConstants {
	/**
	 * select
	 */
	public static final int SELECT = 1;
	/**
	 * update
	 */
	public static final int UPDATE = 2;
	/**
	 * insert
	 */
	public static final int INSERT = 3;
	/**
	 * delete
	 */
	public static final int DELETE = 4;
	/**
	 * view
	 */
	public static final int VIEW = 5 ;
	/**
	 * procedure
	 */
	public static final int PROCEDURE = 6;
	/**
	 * declare
	 */
	public static final int DECLARE = 7;
	
	
	public static int transType(ESqlStatementType sqlStatementType) {
		switch(sqlStatementType){
        case sstselect:
           return SqlStatementTypeConstants.SELECT;
        case sstupdate:
           return  SqlStatementTypeConstants.UPDATE;
        case sstinsert:
            return  SqlStatementTypeConstants.INSERT;
        case sstdelete:
            return  SqlStatementTypeConstants.DELETE;
        case sstplsql_createprocedure:
            return  SqlStatementTypeConstants.PROCEDURE;
        case sstmssqlcreateprocedure:
        	return  SqlStatementTypeConstants.PROCEDURE;
        case sstcreateview:
        	return  SqlStatementTypeConstants.VIEW;
        default:
        	return  SqlStatementTypeConstants.DECLARE;
       }
	}
}
