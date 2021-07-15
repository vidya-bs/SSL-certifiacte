package io.swagger.generator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.design.studio.model.Swagger3VO;
import com.itorix.apiwiz.design.studio.model.SwaggerCloneDetails;
import com.itorix.apiwiz.design.studio.model.SwaggerVO;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
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
    public static void setCloneDetailsFromReq(SwaggerVO dest, SwaggerCloneDetails orig, String swaggerStr) {
        dest.setName(orig.getName());
        dest.setDescription(orig.getDescription());
        dest.setRevision(1);
        String swaggerId = UUID.randomUUID().toString().replaceAll("-", "");
        dest.setSwaggerId(swaggerId);

        SwaggerParser swaggerParser = new SwaggerParser();
        Swagger swagger = swaggerParser.parse(swaggerStr);
        swagger.setBasePath(orig.getBasePath());
        ObjectMapper objMapper = new ObjectMapper();
        dest.setSwagger(objMapper.writeValueAsString(swagger));
    }

}
