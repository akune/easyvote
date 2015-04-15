package de.kune.client;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Options implements Serializable {

	private static final long serialVersionUID = 1L;
	private Set<String> options;
	private boolean multipleSelectionAllowed;

	public Options() {
		this.options = new HashSet<String>();
		this.multipleSelectionAllowed = false;
	}
	
	public Options(Set<String> options, boolean multipleSelectionAllowed) {
		this.options = new LinkedHashSet<String>(options);
		this.multipleSelectionAllowed = multipleSelectionAllowed;
	}

	public Set<String> getOptions() {
		return options;
	}

	public boolean isMultipleSelectionAllowed() {
		return multipleSelectionAllowed;
	}

}