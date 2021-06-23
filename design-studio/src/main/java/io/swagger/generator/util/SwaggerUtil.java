package io.swagger.generator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.design.studio.model.Swagger3VO;
import com.itorix.apiwiz.design.studio.model.SwaggerCloneDetails;
import com.itorix.apiwiz.design.studio.model.SwaggerVO;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;

import java.util.UUID;

public class SwaggerUtil {

    @SneakyThrows
    private SwaggerUtil () {
        throw new IllegalAccessException();
    }

    @SneakyThrows
    public static void copyAllSwaggerFields(Swagger3VO dest, Swagger3VO orig) {
        BeanUtils.copyProperties(dest, orig);
        dest.setId(null);
    }

    public static void setCloneDetailsFromReq(Swagger3VO dest, SwaggerCloneDetails orig) {
        dest.setName(orig.getName());
        dest.setDescription(orig.getDescription());
        dest.setRevision(1);
        String swaggerId = UUID.randomUUID().toString().replaceAll("-", "");
        dest.setSwaggerId(swaggerId);
    }

    @SneakyThrows
    public static void copyAllSwaggerFields(SwaggerVO dest, SwaggerVO orig) {
        BeanUtils.copyProperties(dest, orig);
        dest.setId(null);
    }

    @SneakyThrows
    public static void setCloneDetailsFromReq(SwaggerVO dest, SwaggerCloneDetails orig) {
        dest.setName(orig.getName());
        dest.setDescription(orig.getDescription());
        dest.setRevision(1);
        String swaggerId = UUID.randomUUID().toString().replaceAll("-", "");
        dest.setSwaggerId(swaggerId);

        ObjectMapper objMapper = new ObjectMapper();
        JsonNode jsonNode = objMapper.readTree(dest.getSwagger());
        ((ObjectNode) jsonNode).put("basePath", orig.getBasePath());
        dest.setSwagger(objMapper.writeValueAsString(jsonNode));
    }

}
