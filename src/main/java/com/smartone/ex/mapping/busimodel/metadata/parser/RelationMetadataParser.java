package com.smartone.ex.mapping.busimodel.metadata.parser;

import org.dom4j.Element;

import com.douglei.orm.mapping.metadata.MetadataParseException;
import com.douglei.tools.StringUtil;
import com.smartone.ex.mapping.busimodel.metadata.RelationMetadata;

public class RelationMetadataParser {
	public RelationMetadata parse(Element element) throws MetadataParseException {
		String name = element.attributeValue("refBusiModelName");
		if(StringUtil.isEmpty(name)) 
			throw new MetadataParseException("<relation>元素的refBusiModelName属性值不能为空");
		
		return new RelationMetadata(name,
				element.attributeValue("parentName"),
				element.attributeValue("idPropName"),
				element.attributeValue("refResourceType"),
				element.attributeValue("refResourceKeyName"),
				element.attributeValue("refParentResourcePropId"),
				element.attributeValue("isCascadeDelete"),
				element.attributeValue("orderCode"),
				element.attributeValue("refResourceName"));
	   }
 }
