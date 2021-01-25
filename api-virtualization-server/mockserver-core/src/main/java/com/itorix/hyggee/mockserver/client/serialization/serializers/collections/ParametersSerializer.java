package com.itorix.hyggee.mockserver.client.serialization.serializers.collections;

import com.itorix.hyggee.mockserver.model.Parameters;

/**
 *   
 */
public class ParametersSerializer extends KeysToMultiValuesSerializer<Parameters> {

    public ParametersSerializer() {
        super(Parameters.class);
    }

}
