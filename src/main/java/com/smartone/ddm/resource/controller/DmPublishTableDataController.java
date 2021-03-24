package com.smartone.ddm.resource.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.douglei.api.doc.annotation.Api;
import com.douglei.api.doc.annotation.ApiParam;
import com.douglei.api.doc.annotation.ApiParam_;
import com.douglei.api.doc.types.ParamStructType;
import com.douglei.orm.sessionfactory.validator.table.handler.UpdateValidateHandler;
import com.ibs.code.controller.BasicController;
import com.ibs.code.result.DataValidationResult;
import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.ibs.spring.resolver.method.argument.CommonParams;
import com.ibs.spring.resolver.method.argument.CommonParamsModel;
import com.smartone.ddm.resource.entity.DmPublishTableData;
import com.smartone.ddm.resource.service.DmPublishTableDataService;

@RestController
@RequestMapping("/tabledata")
public class DmPublishTableDataController extends BasicController{
	
	@Autowired
	private DmPublishTableDataService dmPublishTableDataService;
	
	@Api(name="批量添加发布表数据",
			 request=@ApiParam(params ={ 
				 @ApiParam_(entity=DmPublishTableData.class,entityStruct=ParamStructType.ARRAY)
			 }))
	@RequestMapping({"/insert"})
	public Response insertMany(@CommonParams(cls=DmPublishTableData.class)CommonParamsModel<DmPublishTableData> commonParamsModel){  
		if(commonParamsModel.getList()!=null) {
	       	if(validate4Table(commonParamsModel.getList(),UpdateValidateHandler.getInstance4UpdateNullValue())==DataValidationResult.SUCCESS) { 
	       		dmPublishTableDataService.insertMany(commonParamsModel.getList());
	   		} 
        }
	 	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	
	@Api(name="批量修改发布表数据",
			 request=@ApiParam(params ={
				 @ApiParam_(entity=DmPublishTableData.class,entityStruct=ParamStructType.ARRAY)
			 }))
	@RequestMapping({"/update"})
	public Response updateMany(@CommonParams(cls=DmPublishTableData.class)CommonParamsModel<DmPublishTableData> commonParamsModel){
		if(commonParamsModel.getList()!=null) {
	       	if(validate4Table(commonParamsModel.getList(),UpdateValidateHandler.getInstance4UpdateNullValue())==DataValidationResult.SUCCESS) { 
	       		dmPublishTableDataService.updateMany(commonParamsModel.getList());
	   		} 
        }
		return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	
	@Api(name="删除资源",
			 url=@ApiParam(params ={
					 @ApiParam_(name="ids", required=true, description="资源id", egValue="1111")
			 }))
	@RequestMapping(value="/delete",method=RequestMethod.DELETE)
	public Response delete(HttpServletRequest request) {
		String ids = getDeleteIds(request);
		if(ids!=null) {
			dmPublishTableDataService.delete(ids);
		}
	    return ResponseContext.getFinalResponse();
	}
}
