package de.kune.client;

public interface Messages extends com.google.gwt.i18n.client.Messages {

	@DefaultMessage("Invalid PIN")
	String noSuchSession();

	@DefaultMessage("Join")
	String joinButton();

	@DefaultMessage("Please wait for the voting round to begin.")
	String waitingForVotingRound();

	@DefaultMessage("PIN")
	String pinPlaceholder();

}
