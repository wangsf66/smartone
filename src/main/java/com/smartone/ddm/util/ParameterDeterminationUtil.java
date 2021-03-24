package com.smartone.ddm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourceParam;

import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.TResultColumn;
import gudusoft.gsqlparser.nodes.TResultColumnList;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
/**
 * 
 * @author wangShuFang
 */
public class ParameterDeterminationUtil {
	private static final Pattern prefixPattern = Pattern.compile("\\$", Pattern.MULTILINE);// 匹配$
//	private static final Pattern suffixPattern = Pattern.compile("\\$", Pattern.MULTILINE);// 匹配$
	
	/**
	  * 从content中解析出parameter集合
	  */
	public static Object parseParamsSet(DmResource dmResource) {
		List<DmResourceParam> sqlResultSets = new ArrayList<DmResourceParam>();
		String content = dmResource.getContent();
		String sqlparam = null;
		DmResourceParam dmResourceParams = null;
		Map<String,String> parameters = new HashMap<String,String>(10);
		int startIndex, endIndex;
		int sort = 0;
		Matcher perfixMatcher = prefixPattern.matcher(content);
		// 如果前后缀一样, 则只用前缀去匹配, 获取每个sql参数
		while (perfixMatcher.find()) {
			startIndex = perfixMatcher.start();
			if (perfixMatcher.find()) {
				endIndex = perfixMatcher.start();
				sqlparam = content.substring(startIndex + 1, endIndex).toString();
				parameters = resolvingPropertyMap(sqlparam);
				sort++;
				dmResourceParams = new DmResourceParam(dmResource.getId(),parameters.get("name"),0,sort,parameters.get("datatype"));
				if(!sqlResultSets.contains(dmResourceParams)){
					sqlResultSets.add(dmResourceParams);
				}
			} else {
				throw new IllegalArgumentException("content=[" + content + "], 参数配置异常, ["
						+ prefixPattern + "]标识符不匹配(多一个/少一个), 请检查");
			}
		}
		sort = 0;
		return sqlResultSets;
	}

    // 解析出属性map集合
	private  static Map<String, String> resolvingPropertyMap(String configText) {
		String[] cts = configText.split("\\|");
		int length = cts.length;
		if(length < 1) {
			throw new IllegalArgumentException("sql参数, 必须配置参数名");
		}
		Map<String, String> propertyMap = new HashMap<String, String>(length);
		propertyMap.put("name", cts[0].trim());
		
		if(length > 1) {
			String[] keyValue = null;
			for(int i=1;i<length;i++) {
				keyValue = getKeyValue(cts[i]);
				if(keyValue != null) {
					propertyMap.put(keyValue[0], keyValue[1]);
				}
			}
		}
		return propertyMap;
	}
		
	private static String[] getKeyValue(String confText) {
		if(confText != null) {
			confText = confText.trim();
			int equalIndex = confText.indexOf("=");
			if(equalIndex > 0 && equalIndex < (confText.length()-1)) {
				String[] keyValue = new String[2];
				keyValue[0] = confText.substring(0, equalIndex).trim().toLowerCase();
				keyValue[1] = confText.substring(equalIndex+1).trim();
				return keyValue;
			}
		}
		return null;
	}
	
	/**
	 * 解析出查询的结果列
	 * @param selectSqlStatement
	 * @return
	 */
	private static TResultColumnList analysisResultColumnList(TSelectSqlStatement selectSqlStatement) {
		if(selectSqlStatement.isCombinedQuery()){
			return analysisResultColumnList(selectSqlStatement.getLeftStmt());
		}else{
			return selectSqlStatement.getResultColumnList();
		}
	}
	
	

	public static Object  parseResultSet(TGSqlParser parser,DmResource dmResource) {
		parser.sqltext = dmResource.getContent();
		parser.parse();
		TSelectSqlStatement selectSqlStatement = (TSelectSqlStatement) parser.sqlstatements.get(0);
		TResultColumnList columns = analysisResultColumnList(selectSqlStatement);
		if(columns == null || columns.size() == 0){
			ResponseContext.addValidationFull(columns,"PARAM_NAME","没有解析到任何查询结果字段","smartone.dynamic.sql.paramAnalysis.noFields");
		}
		int columnSize = columns.size();
		List<DmResourceParam> sqlResultList = new ArrayList<DmResourceParam>(columnSize);
		boolean includeIdColumn = false;// 是否包含id字段，如果不包括，则系统抛出异常
		String columnName = null;
		TResultColumn column = null;
		DmResourceParam dmResourceParams = null;
		for(int i=0;i<columnSize ;i++){
			column = columns.getResultColumn(i);
			columnName = column.getAliasClause() == null ? column.getColumnNameOnly() : column.getColumnAlias();
			if(columnName.equals("")){
				ResponseContext.addValidationFull(columnName,"PARAM_NAME","请指定合法列名","smartone.dynamic.sql.paramAnalysis.specifyALegalColumnName");
		        return null;
			}
			if("*".equals(columnName)){
				ResponseContext.addValidationFull(columnName,"PARAM_NAME","请指定具体查询列，禁止使用*查询","smartone.dynamic.sql.paramAnalysis.prohibitionOfUse");
				return null;
			}
			dmResourceParams = new DmResourceParam(dmResource.getId(),columnName,1,(i+1));
			
			if(!sqlResultList.contains(dmResourceParams)){
				sqlResultList.add(dmResourceParams);
			}
			
			if(!includeIdColumn && dmResourceParams.getParamName().equalsIgnoreCase("ID")){
				includeIdColumn = true;
			}
		}
		
		if(!includeIdColumn){
			ResponseContext.addValidationFull(columnName,"ID","其查询结果字段，不包含id列，系统无法处理，请修改该sql脚本，增加查询id字段的语句","smartone.dynamic.sql.paramAnalysis.doesNotContainId");
			return null;
		}
		return sqlResultList;
	}
	
//	public static void main(String[] args) {
//		String content  = "DECLARE @temptable TABLE(BOM_ID varchar(50))\r\n" + 
//				"INSERT INTO @temptable(BOM_ID) SELECT[value] FROM [dbo].F_Split($BOM_ID$,',')\r\n" + 
//				"\r\n" + 
//				"INSERT INTO MPM_WEIGH(ID,PROCESS_ID,STEP_ID,RECIPE_ID,PBOM_ID,MATERIAL_ID,BATCH_NUMBER,\r\n" + 
//				"                      FACTORY,MATERIAL_NUMBER,MATERIAL_WEIGHT,ERROR_VALUE,USE_WEIGHTER,USE_VESSEL,\r\n" + 
//				"					  RATIO,DATA_TYPE,SORTCODE,REMARK,\r\n" + 
//				"					  CREATE_USER_ID,CREATE_USER_NAME,CREATE_DATE,LAST_UPDATE_USER_ID,LAST_UPDATE_USER_NAME,LAST_UPDATE_DATE,PROJECT_ID,PROJECT_NAME,CUSTOMER_ID,CUSTOMER_NAME)\r\n" + 
//				"SELECT NEWID(),$PROCESS_ID$,$STEP_ID$,NULL,A.ID,A.MATERIAL_ID,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,NULL,NULL,\r\n" + 
//				"'accountId','accountId',GETDATE(),'accountId','accountId',GETDATE(),'projectId','projectId','customerId','customerId' \r\n" + 
//				"FROM MPM_PROCESS_BOM A\r\n" + 
//				"WHERE A.ID IN(SELECT BOM_ID FROM @temptable)\r\n" + 
//				"";
//		String  sqlparam = content.substring(120).toString();
//		System.out.println(sqlparam);
//	}
}
