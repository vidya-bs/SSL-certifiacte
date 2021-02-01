package com.itorix.hyggee.mockserver.model;

import java.util.List;

/**
 *   
 */
public class Parameters extends KeysToMultiValues<Parameter, Parameters> {

    public Parameters(List<Parameter> parameters) {
        withEntries(parameters);
    }

    public Parameters(Parameter... parameters) {
        withEntries(parameters);
    }

    @Override
    public Parameter build(NottableString name, List<NottableString> values) {
        return new Parameter(name, values);
    }

    public Parameters clone() {
        return new Parameters().withEntries(getEntries());
    }
}
