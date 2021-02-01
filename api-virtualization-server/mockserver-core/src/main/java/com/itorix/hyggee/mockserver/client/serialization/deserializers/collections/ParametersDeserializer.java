package com.itorix.hyggee.mockserver.client.serialization.deserializers.collections;

import com.itorix.hyggee.mockserver.model.Parameters;

/**
 *   
 */
public class ParametersDeserializer extends KeysToMultiValuesDeserializer<Parameters> {

    public ParametersDeserializer() {
        super(Parameters.class);
    }

    @Override
    public Parameters build() {
        return new Parameters();
    }
}
