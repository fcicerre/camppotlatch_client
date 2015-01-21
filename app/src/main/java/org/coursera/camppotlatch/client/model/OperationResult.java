package org.coursera.camppotlatch.client.model;

import com.google.gson.annotations.Expose;

/**
 * Created by Fabio on 07/11/2014.
 */
public class OperationResult {
    public enum OperationResultState { SUCCEEDED, FAILED }

    @Expose
    private OperationResultState result;

    public OperationResult(OperationResultState result) {
        this.result = result;
    }

    public OperationResultState getResult() {
        return result;
    }
    public void setResult(OperationResultState result) {
        this.result = result;
    }
}
