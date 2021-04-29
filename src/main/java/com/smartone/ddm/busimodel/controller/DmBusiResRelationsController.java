package com.smartone.ddm.busimodel.controller;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.douglei.api.doc.annotation.Api;
import com.douglei.api.doc.annotation.ApiCatalog;
import com.douglei.api.doc.annotation.ApiParam;
import com.douglei.api.doc.annotation.ApiParam_;
import com.douglei.api.doc.types.ParamStructType;
import com.douglei.orm.sessionfactory.validator.table.handler.InsertValidateHandler;
import com.douglei.orm.sessionfactory.validator.table.handler.UpdateValidateHandler;
import com.ibs.code.controller.BasicController;
import com.ibs.code.result.DataValidationResult;
import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.ibs.spring.resolver.method.argument.CommonParams;
import com.ibs.spring.resolver.method.argument.CommonParamsModel;
import com.smartone.ddm.busimodel.entity.DmBusiModelRelation;
import com.smartone.ddm.busimodel.service.DmCfgBusiModelResRelationsService;
/**
 * 
 * @author wangShuFang
 */
@ApiCatalog(name="业务子表资源")
@RestController
@RequestMapping({"/busimodelresrelations"})
public class DmBusiResRelationsController extends BasicController {
	
	@Autowired
	private DmCfgBusiModelResRelationsService dmBusiRelationsService;
	
	@Api(name="添加业务子表资源",
			 request=@ApiParam(params ={
				 @ApiParam_(entity=DmBusiModelRelation.class,entityStruct=ParamStructType.OBJECT)
			 }))
	@RequestMapping({"/insert"})
	public Response insert(@CommonParams(cls=DmBusiModelRelation.class)CommonParamsModel<DmBusiModelRelation> commonParamsModel) {
		if(commonParamsModel.getList()!=null) {
        	if(validate4Table(commonParamsModel.getList(),InsertValidateHandler.getSingleton())==DataValidationResult.SUCCESS) { 
        		dmBusiRelationsService.insertMany(commonParamsModel.getList());
    		}
        }
		return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}

	@Api(name="修改业务子表资源",
			 request=@ApiParam(params ={
				 @ApiParam_(entity=DmBusiModelRelation.class,entityStruct=ParamStructType.OBJECT)
			 }))
	@RequestMapping({"/update"})
	public Response update(@CommonParams(cls=DmBusiModelRelation.class)CommonParamsModel<DmBusiModelRelation> commonParamsModel) {
		if(commonParamsModel.getList()!=null) {
        	if(validate4Table(commonParamsModel.getList(),UpdateValidateHandler.getInstance4UpdateNullValue())==DataValidationResult.SUCCESS) { 
        		dmBusiRelationsService.updateMany(commonParamsModel.getList());
    		}
        }
		return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	
	@Api(name="删除业务子表资源",
			 url=@ApiParam(params ={
					 @ApiParam_(name="ids", required=true, description="业务子表资源id", egValue="1111")
			 }))
	@RequestMapping(value="/delete",method=RequestMethod.DELETE)
	public Response delete(HttpServletRequest request) {
		String ids = getDeleteIds(request);
		if(ids!=null) {
			dmBusiRelationsService.delete(ids);
		}
	    return ResponseContext.getFinalResponse();
	}
	
	@Api(name="查询业务子表资源",
			 url=@ApiParam(params ={
					 @ApiParam_(name="id", required=true, description="业务子表资源id", egValue="1111")
			 }))
	@RequestMapping(value="/query",method=RequestMethod.GET)
	public Response queryList(HttpServletRequest request) {
		String id = request.getParameter("id");
		if(id!=null) {
			dmBusiRelationsService.queryList(id);
		}
	    return ResponseContext.getFinalResponse();
	}
}

