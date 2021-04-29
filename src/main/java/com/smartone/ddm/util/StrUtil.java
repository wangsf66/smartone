package com.smartone.ddm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.douglei.orm.configuration.environment.EnvironmentContext;

import gudusoft.gsqlparser.EDbVendor;

public class StrUtil {
	
	//将字符串拼接为sql
	public static String stringToSql(String ids) {
		String id[] = ids.split(",");
		String sql = "";
		for (int i = 0; i < id.length; i++) {
			if (i == id.length - 1) {
				sql += "'" + id[i] + "'";
			} else {
				sql += "'" + id[i] + "' ,";
			}
		}
		return sql;
	}
	
	public static boolean isContainChinese(String str) {
    	Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
         Matcher m = p.matcher(str);
         if (m.find()) {
            return true;
         }
         return false;
	}
	
	public static EDbVendor getDialect() {
		switch(EnvironmentContext.getEnvironment().getDialect().getDatabaseType()) {
		   case MYSQL:
			   return EDbVendor.dbvmysql;
		   case ORACLE:
			   return EDbVendor.dbvoracle;
		   case SQLSERVER:
			   return EDbVendor.dbvmssql;
		}
		throw new NullPointerException();
	}
} 
