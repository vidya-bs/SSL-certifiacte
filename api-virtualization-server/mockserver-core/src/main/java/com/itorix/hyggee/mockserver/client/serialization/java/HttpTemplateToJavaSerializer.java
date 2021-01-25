package com.itorix.hyggee.mockserver.client.serialization.java;

import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.model.HttpTemplate;

import org.apache.commons.text.StringEscapeUtils;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.client.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 *   
 */
public class HttpTemplateToJavaSerializer implements ToJavaSerializer<HttpTemplate> {

    @Override
    public String serialize(int numberOfSpacesToIndent, HttpTemplate httpTemplate) {
        StringBuffer output = new StringBuffer();
        if (httpTemplate != null) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append("template(HttpTemplate.TemplateType." + httpTemplate.getTemplateType().name() + ")");
            if (!Strings.isNullOrEmpty(httpTemplate.getTemplate())) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withTemplate(\"").append(StringEscapeUtils.escapeJava(httpTemplate.getTemplate())).append("\")");
            }
            if (httpTemplate.getDelay() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withDelay(").append(new DelayToJavaSerializer().serialize(0, httpTemplate.getDelay())).append(")");
            }
        }

        return output.toString();
    }

    private StringBuffer appendNewLineAndIndent(int numberOfSpacesToIndent, StringBuffer output) {
        return output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent, ' '));
    }
}
