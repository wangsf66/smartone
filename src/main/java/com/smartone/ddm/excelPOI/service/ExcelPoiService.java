package com.smartone.ddm.excelPOI.service;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.sql.query.page.PageResult;
import com.douglei.tools.file.reader.PropertiesReader;
import com.ibs.code.service.BasicService;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourceParam;
import com.smartone.ddm.util.ImportExcelUtils;
/**
 * 
 * @author wangShuFang
 */
@TransactionComponent
public class ExcelPoiService extends BasicService{
	
	@Transaction
	public void poiExcel(MultipartFile file,String resourceName,Integer pageSize,String excelExportEncryption){
		InputStream in =null;
        try {
        if(file.isEmpty()){
            throw new Exception("文件不存在！");
        }
	        in = file.getInputStream();
	        getBankListByExcel(in,file.getOriginalFilename(),resourceName,pageSize,excelExportEncryption);
	        in.close();
        } catch (Exception e) {
			e.printStackTrace();
		}
       
	}
	
	public void getBankListByExcel(InputStream in, String fileName,String resourceName,Integer pageSize,String excelExportEncryption) throws Exception {
		List<List<Object>> list = null;
		// 创建Excel工作薄
		Workbook work = ImportExcelUtils.getWorkbook(in, fileName);
		if (null == work) {
			throw new Exception("创建Excel工作薄为空！");
		}
		// 工作表
		Sheet sheet = null;
		// 行
		Row row = null;
		// 单元格
		Cell cell = null;
		list = new ArrayList<List<Object>>(); 
		sheet = work.getSheetAt(0);
		//一次允许导入的最大数据量，，可以由用户提供，用户不提供，就使用默认的数量
		if(pageSize==null) {
			pageSize = 5;
		}
		int totalNumber = sheet.getLastRowNum()-sheet.getFirstRowNum();
		int page = 0;
		List<Object> li  = null;
		if(totalNumber>pageSize) {
			//分批导入的次数
			page = (totalNumber % pageSize == 0) ? (totalNumber / pageSize) : (totalNumber / pageSize + 1); 
			int i = 0; 
			while(i<page) {
				for (int j = pageSize*i+1; j<=pageSize*(i+1); j++) {
					row = sheet.getRow(j);
					if (row == null || row.getFirstCellNum() == j) {
						continue;
					}
					addLi(li,list,row,cell);					
					}
				saveList(resourceName,list,excelExportEncryption);
				list.clear();
				i++;
				if(i>page) {
		    		break;
		    	}
		    }
		}else {
			//遍历当前sheet中的所有行
			for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
				row = sheet.getRow(j);
				if (row == null || row.getFirstCellNum() == j) {
					continue;
				}
				addLi(li,list,row,cell);
			}
			saveList(resourceName,list,excelExportEncryption);
		}
	}
	
	public  void saveList(String resourceName,List<List<Object>> list,String excelExportEncryption) {
		 List<DmResourceParam> paramsList =  SessionContext.getSqlSession().query(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS where RESOURCE_ID in(\r\n" + 
	        		"select ID from DM_RESOURCE where RESOURCE_NAME='"+resourceName+"') order by ORDER_CODE");
	        Map<String,Object>  params = null;
	        for (int i = 0; i < list.size(); i++) { 
	        	List<Object> lo = list.get(i);
	        	params = new HashMap<String,Object>();
	        	for(int a=0;a<lo.size();a++) {
	        		for(int b=0;b<paramsList.size();b++) {
	        			if(a==b) {
	        				if(excelExportEncryption.equals("true")) {
	        					params.put(paramsList.get(b).getParamName(),Base64.getDecoder().decode((String)lo.get(a)));
	        				   
	        				}else {
	        					params.put(paramsList.get(b).getParamName(),lo.get(a));
	        				}
	        			}
	        		}
	        	}
	        	tableSessionSave(resourceName,params);
	        }       
	}


	//遍历当前row中的所有列
	public void addLi(List<Object> li,List<List<Object>> list,Row row,Cell cell) {
		li = new ArrayList<Object>();
		for (int k = row.getFirstCellNum(); k < row.getLastCellNum(); k++) {
			cell = row.getCell(k);
			if (cell == null) {
				continue;
			}
			li.add(ImportExcelUtils.getCellValue(cell));
		}
		list.add(li);
	}

	
	@Transaction
	public void exportDataExcel(String resourceName,Integer pageSize) {
		OutputStream out = null;	
		//获取表对应的列数组，得到列的描述，即表头
		try {
		List<DmResourceParam> paramsList =  SessionContext.getSqlSession().query(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS where RESOURCE_ID in(\r\n" + 
        		"select ID from DM_RESOURCE where RESOURCE_NAME='"+resourceName+"') order by ORDER_CODE");
		//获取表格的标题
		DmResource dmResource = SessionContext.getSqlSession().uniqueQuery(DmResource.class,"select * from  DM_RESOURCE where RESOURCE_NAME='"+resourceName+"'");
		// 1.创建工作簿
        // 阈值，内存中的对象数量最大值，超过这个值会生成一个临时文件存放到硬盘中
		XSSFWorkbook  workbook = new XSSFWorkbook();
		// 生成一个表格
		XSSFSheet sheet = workbook.createSheet(dmResource.getDescName()+"数据");
        // 设置表格默认列宽度为15个字节
     	sheet.setDefaultColumnWidth(15);
        // 产生表格标题行
     	XSSFRow row = sheet.createRow(0);
     	for (short i = 0; i <paramsList.size(); i++) {
     		XSSFCell cell = row.createCell(i);
     		XSSFRichTextString text = new XSSFRichTextString(paramsList.get(i).getDescName());
     		cell.setCellValue(text);
     	}
	     	int pageNum = 1;
	     	//一次允许导出的数据量，可以由用户提供，用户不提供，就使用默认的数量
	     	if(pageSize==null) {
				pageSize = 5;
			}
 	     	List<Map<String,Object>> list = SessionContext.getSqlSession().query("select * from "+resourceName);
 	     	@SuppressWarnings("rawtypes")
			PageResult page = null;
	     	//数据总量
	     	int totalNumber = list.size();
	     	//总页数
	     	int pageCount = (totalNumber % pageSize == 0) ? (totalNumber / pageSize) : (totalNumber / pageSize + 1);  ;
	     	
	     	while(pageNum<=pageCount) {
	     		page = SessionContext.getSqlSession().pageQuery(pageNum, pageSize, "select "+pjParams(paramsList)+" from "+resourceName);
	     		importList(page,row,sheet,pageSize,pageNum);
	     		pageNum++;
	     		if(pageNum>pageCount) {
	     			break;
	     		}
	     	}
			String tmpPath = "D:\\excel\\" + dmResource.getDescName()+"数据" + ".xlsx";
			out = new FileOutputStream(tmpPath);
			workbook.write(out);
			Map<String,Object> idsMap = new HashMap<String,Object>();
			idsMap.put("message", "导出成功");
			ResponseContext.addData(idsMap);
		} catch (IOException e) {
			e.printStackTrace();
			ResponseContext.addError("message",e);
		}finally{
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void importList(PageResult page,XSSFRow row,XSSFSheet sheet,int pageSize,int pageNum) {
		Iterator<Map<String, Object>> it = page.getResultDatas().iterator();
		int index = 0;
		while (it.hasNext()) {
			index++;
			row = sheet.createRow((pageSize*(pageNum-1))+index);
			Map<String, Object> data = it.next();
			data.remove("RN");
			int i = 0;
			for(String key : data.keySet()){
				XSSFCell cell = row.createCell(i);
				XSSFRichTextString text = new XSSFRichTextString(data.get(key)+""); 
				if(readProfile().equals("true")) {
					cell.setCellValue(Base64.getEncoder().encodeToString(text.getString().getBytes()));
				}else {
					cell.setCellValue(text);
				}
				i++;
			}
		}
	}
	
	//读取配置文件
	public String readProfile() {
		PropertiesReader propertiesReader = new PropertiesReader("basicConfiguration.properties");
		return  propertiesReader.readProperty("excelExportEncryption");
	}
	
	//获取导出的列
	private String pjParams(List<DmResourceParam> paramsList) {
		StringBuilder string = new StringBuilder();
		if(paramsList!=null&&paramsList.size()>0) {
			for(int i=0;i<paramsList.size();i++) {
				if(i ==paramsList.size()-1) {
					string.append(paramsList.get(i).getParamName());
				}else {
					string.append(paramsList.get(i).getParamName()+",");
				}
			}
		}
		return string.toString();
	}
}
