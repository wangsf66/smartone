package com.smartone.ddm.busimodel.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.douglei.api.doc.annotation.Api;
import com.douglei.api.doc.annotation.ApiCatalog;
import com.douglei.api.doc.annotation.ApiParam;
import com.douglei.api.doc.annotation.ApiParam_;
import com.douglei.api.doc.types.ParamStructType;
import com.douglei.orm.mapping.validator.ValidateFailResult;
import com.douglei.orm.sessionfactory.validator.table.handler.InsertValidateHandler;
import com.douglei.orm.sessionfactory.validator.table.handler.UpdateValidateHandler;
import com.douglei.tools.StringUtil;
import com.ibs.code.controller.BasicController;
import com.ibs.code.controller.ControllerValidator;
import com.ibs.code.result.DataValidationResult;
import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.ibs.spring.resolver.method.argument.CommonParams;
import com.ibs.spring.resolver.method.argument.CommonParamsModel;
import com.smartone.ddm.busimodel.service.DmCfgBusiModelService;
import com.smartone.ddm.resource.entity.DmResource;

/**
 * 
 * @author wangShuFang
 */
@ApiCatalog(name="业务表资源")
@RestController
@RequestMapping({"/busimodel","/cfgBusiModel"})
public class DmBusiModelController extends BasicController {
	
	@Autowired
	private DmCfgBusiModelService dmCfgBusiModelService;
	
	@Api(name = "添加业务表资源", request = @ApiParam(params = {
			@ApiParam_(entity = DmResource.class, entityStruct = ParamStructType.OBJECT) }))
	@RequestMapping({"/insert"})
	public Response insert(
			@CommonParams(cls = DmResource.class) CommonParamsModel<DmResource> commonParamsModel) {
		if (commonParamsModel.getList() != null) {
			if (validate4Table(commonParamsModel.getList(),InsertValidateHandler.getSingleton()) == DataValidationResult.SUCCESS) {
				dmCfgBusiModelService.insertMany(commonParamsModel.getList());
			}
		}
		return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	
	@Api(name="修改表业务资源",
			 request=@ApiParam(params ={
				 @ApiParam_(entity=DmResource.class,entityStruct=ParamStructType.OBJECT)
			 }))
	@RequestMapping({"/update"})
	public Response update(@CommonParams(cls=DmResource.class)CommonParamsModel<DmResource> commonParamsModel) {
		if(commonParamsModel.getList() !=null) {
        	if(validate4Table(commonParamsModel.getList(),UpdateValidateHandler.getInstance4UpdateNullValue())==DataValidationResult.SUCCESS) { 
        		dmCfgBusiModelService.updateMany(commonParamsModel.getList());
    		}
        }
		return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	
	@Api(name="删除业务表资源",
			 url=@ApiParam(params ={
					 @ApiParam_(name="ids", required=true, description="业务表资源id", egValue="1111")
			 }))
	@RequestMapping(value="/delete",method=RequestMethod.DELETE)
	public Response delete(HttpServletRequest request) {
		String ids = getDeleteIds(request);
		if(ids!=null) {
			dmCfgBusiModelService.delete(ids);
		}
	    return ResponseContext.getFinalResponse();
	}
	
	/**
	 * 发布资源
	 * @return 
	 * @throws IOException 
	 */
	@RequestMapping(value = "/createModel", method = RequestMethod.POST)
	public Response createModel(@CommonParams(cls=DmResource.class)CommonParamsModel<DmResource> commonParamsModel) {
		if(commonParamsModel.getList()!=null&&commonParamsModel.getList().size()>1) {
			ResponseContext.addValidationFull(commonParamsModel.getList(), "id", "不可以进行批量建模", "smartone.resource.createModel.noBatch");
        	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
        }else {
        	if(validateByValidator(commonParamsModel.getList().get(0))==DataValidationResult.SUCCESS) {                                             
        		dmCfgBusiModelService.createBusiModel(commonParamsModel.getList().get(0));
        	}
        	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
        }
	}
	
	@RequestMapping(value = "/createModel", method = RequestMethod.GET)
	public Response createModel() {
		dmCfgBusiModelService.createModel();
    	return ResponseContext.getFinalResponse(true);
	}
	
	/**
	 * 取消发布资源
	 * @return 
	 * @throws IOException 
	 */
	@RequestMapping(value = "/cancelModel", method = RequestMethod.GET)
	public Response cancelModel(@CommonParams(cls=DmResource.class)CommonParamsModel<DmResource> commonParamsModel) {
		if(commonParamsModel.getList()!=null&&commonParamsModel.getList().size()>1) {
			ResponseContext.addValidationFull(commonParamsModel.getList(), "id", "不可以进行批量建模", "smartone.resource.createModel.noBatch");
        	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
        }else {
        	if(validateByValidator(commonParamsModel.getList().get(0))==DataValidationResult.SUCCESS) {                                             
        		dmCfgBusiModelService.cancelBusiModel(commonParamsModel.getList().get(0));
        	}
        	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
        }
	}
	
	@RequestMapping({"/{nameSpace}/operate","/{nameSpace}/batchOperate"})
	public Response businessOperate(@PathVariable(name="nameSpace") String nameSpace,@CommonParams(cls=Map.class)CommonParamsModel<Map<String,Object>> commonParamsModel) {
		if(commonParamsModel.getList()!=null) {
			dmCfgBusiModelService.batchBusinessOperate(nameSpace,commonParamsModel.getList(),commonParamsModel.getIsBatch());
        }
		return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	
	@RequestMapping("/{resourceName}/query")
	public Response queryCondition(HttpServletRequest request,@RequestBody  Map<String,Object> map,@PathVariable(name = "resourceName") String resourceName){
		String isRecursive = request.getParameter("isRecursive");
		dmCfgBusiModelService.query(map,resourceName,isRecursive);
		return ResponseContext.getFinalResponse();
	}
}

class IdNotNullValidator implements ControllerValidator<DmResource>{

	@Override
	public ValidateFailResult validate(int arg0, DmResource validateData, List<DmResource> list) {
		if(StringUtil.isEmpty(validateData.getId())) {
			return new ValidateFailResult("id", "不能为空", "smartone.table.createModel.idIsNotNull");
		}
		return null;
	}
}
