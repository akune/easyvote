package de.kune.client.common;

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
	
	@DefaultStringValue("Scrum Poker")
	String scrumLabel();
	
	@DefaultStringArrayValue({ "0", "½", "1", "2", "3", "5", "8", "13", "20", "40", "100", "?", "Coffee Break" })
	String[] scrum();
	
}
