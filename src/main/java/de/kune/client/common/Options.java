package de.kune.client.common;

import java.io.Serializable;

public class Options implements Serializable {

	private static final long serialVersionUID = 1L;
	private String optionsKey;
	private boolean multipleSelectionAllowed;
	
	public Options() {
		this(null, false);
	}

	public Options(String optionsKey, boolean multipleSelectionAllowed) {
		this.optionsKey = optionsKey == null ? "none" : optionsKey;
		this.multipleSelectionAllowed = multipleSelectionAllowed;
	}

	public String getOptionsKey() {
		return optionsKey;
	}

	public boolean isMultipleSelectionAllowed() {
		return multipleSelectionAllowed;
	}
	
	public String toString() {
		return optionsKey + "/" + multipleSelectionAllowed;
	}

}