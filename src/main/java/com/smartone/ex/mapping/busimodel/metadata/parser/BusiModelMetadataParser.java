package com.smartone.ex.mapping.busimodel.metadata.parser;

import org.dom4j.Element;

import com.douglei.orm.mapping.metadata.MetadataParseException;
import com.douglei.tools.StringUtil;
import com.smartone.ex.mapping.busimodel.metadata.BusiModelMetadata;

/**
 * view元数据解析
 * @author DougLei
 */
public class BusiModelMetadataParser{

	public BusiModelMetadata parse(Element element) throws MetadataParseException{
		// TODO 具体解析出业务模型的元数据实例
		String name = element.attributeValue("name");
		if(StringUtil.isEmpty(name)) {
			throw new MetadataParseException("<busimodel>元素的name属性值不能为空");
		}
		return new BusiModelMetadata(name, element.attributeValue("oldName"));
	}
}
