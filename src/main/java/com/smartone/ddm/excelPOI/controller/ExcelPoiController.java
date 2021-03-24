package com.smartone.ddm.excelPOI.controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.douglei.api.doc.annotation.ApiCatalog;
import com.ibs.code.controller.BasicController;
import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.excelPOI.service.ExcelPoiService;
/**
 * 
 * @author wangShuFang
 */
@ApiCatalog(name="上传")
@RestController
@RequestMapping("/poi")
public class ExcelPoiController extends BasicController{
	
	  @Autowired
	  private ExcelPoiService excelPoiService;
	
	  @RequestMapping(value="/upload",method={RequestMethod.POST})
	  public Response  uploadExcel(@RequestParam("file") MultipartFile file,HttpServletRequest request,HttpServletResponse response) throws Exception {
		    String resourceName = request.getParameter("resourceName");
		    String excelExportEncryption = request.getParameter("excelExportEncryption");
		    Integer pageSize = null;
		    if(request.getParameter("pageSize")!=null) {
		    	 pageSize =Integer.parseInt(request.getParameter("pageSize"));
		    }
		    excelPoiService.poiExcel(file, resourceName,pageSize,excelExportEncryption); 
		    return ResponseContext.getFinalResponse(true);
	 } 
	  
	  @RequestMapping(value="/export",method={RequestMethod.POST})
	  public Response  exportBigDataExcel(HttpServletRequest request,HttpServletResponse response) throws Exception {
		    String resourceName = request.getParameter("resourceName");
		    Integer pageSize = null;
		    if(request.getParameter("pageSize")!=null) {
		    	 pageSize =Integer.parseInt(request.getParameter("pageSize"));
		    }
		    excelPoiService.exportDataExcel(resourceName,pageSize); 
		    return ResponseContext.getFinalResponse(true);
	 }           
}
