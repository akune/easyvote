package de.kune.client;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface OptionsConstants extends ConstantsWithLookup {

	@DefaultStringValue("None")
	String noneOptionsLabel();
	
	@DefaultStringArrayValue({})
	String[] none();
	
	@DefaultStringValue("A, B, C")
	String abcLabel();
	
	@DefaultStringArrayValue({"A", "B", "C"})
	String[] abc();
	
	@DefaultStringValue("Yes / No")
	String yesNoLabel();
	
	@DefaultStringArrayValue({"Yes", "No"})
	String[] yesNo();
	
}
