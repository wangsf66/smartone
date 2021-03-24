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
import com.smartone.ddm.resource.entity.DmResourcePublishTarget;
import com.smartone.ddm.resource.service.DmResourcePublishTargetService;

@RestController
@RequestMapping("/resourcepublishtarget")
public class DmResourcePublishTargetController extends BasicController{
	
	@Autowired
	private DmResourcePublishTargetService dmResourcePublishTargetService;
	
	@Api(name="批量分配勾选状态资源",
			 request=@ApiParam(params ={ 
				 @ApiParam_(entity=DmResourcePublishTarget.class,entityStruct=ParamStructType.ARRAY)
			 }))
	@RequestMapping({"/insertIsChecked"})
	public Response insertIschecked(@CommonParams(cls=DmResourcePublishTarget.class)CommonParamsModel<DmResourcePublishTarget> commonParamsModel){  
		if(commonParamsModel.getList()!=null) {
	       	if(validate4Table(commonParamsModel.getList(),UpdateValidateHandler.getInstance4UpdateNullValue())==DataValidationResult.SUCCESS) { 
	       		dmResourcePublishTargetService.insertIschecked(commonParamsModel.getList().get(0));
	   		} 
        }
	 	return ResponseContext.getFinalResponse(true);
	}
	
	@Api(name="批量分配锁定状态资源",
			 request=@ApiParam(params ={ 
				 @ApiParam_(entity=DmResourcePublishTarget.class,entityStruct=ParamStructType.ARRAY)
			 }))
	@RequestMapping({"/insertIsLocking"})
	public Response insertIsLocking(@CommonParams(cls=DmResourcePublishTarget.class)CommonParamsModel<DmResourcePublishTarget> commonParamsModel){  
		if(commonParamsModel.getList()!=null) {
	       	if(validate4Table(commonParamsModel.getList(),UpdateValidateHandler.getInstance4UpdateNullValue())==DataValidationResult.SUCCESS) { 
	       		dmResourcePublishTargetService.insertIsLocking(commonParamsModel.getList().get(0));
	   		} 
       }
	   return ResponseContext.getFinalResponse(true);
	}
	
	@Api(name="查询资源")
	@RequestMapping(value="/test",method=RequestMethod.GET)
	public Response query(HttpServletRequest request) {
		String id = request.getParameter("id");
		dmResourcePublishTargetService.test(id);
	    return ResponseContext.getFinalResponse();
	}
}
