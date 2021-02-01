package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.Body;
import com.itorix.hyggee.mockserver.model.RegexBody;

/**
 *   
 */
public class RegexBodyDTO extends BodyDTO {

    private String regex;

    public RegexBodyDTO(RegexBody regexBody) {
        this(regexBody, false);
    }

    public RegexBodyDTO(RegexBody regexBody, Boolean not) {
        super(Body.Type.REGEX, not);
        this.regex = regexBody.getValue();
    }

    public String getRegex() {
        return regex;
    }

    public RegexBody buildObject() {
        return new RegexBody(getRegex());
    }
}
