package io.takari.bpm.actions;

import io.takari.bpm.misc.CoverageIgnore;

public class ActivateElementAction implements Action {

    private static final long serialVersionUID = 1L;

    private final String definitionId;
    private final String elementId;
    private final int count;

    public ActivateElementAction(String definitionId, String elementId, int count) {
        this.definitionId = definitionId;
        this.elementId = elementId;
        this.count = count;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public String getElementId() {
        return elementId;
    }

    public int getCount() {
        return count;
    }

    @Override
    @CoverageIgnore
    public String toString() {
        return "ActivateElementAction [definitionId=" + definitionId + ", elementId=" + elementId + ", count=" + count + "]";
    }
}
