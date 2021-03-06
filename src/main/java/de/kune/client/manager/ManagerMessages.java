package de.kune.client.manager;

import com.google.gwt.i18n.client.Messages;

public interface ManagerMessages extends Messages {

	@DefaultMessage("Begin Voting Round")
	String beginVotingRoundButton();

	@DefaultMessage("End Voting Session")
	String endVotingSessionButton();

	@DefaultMessage("End Voting Round")
	String endVotingRoundButton();

	@DefaultMessage("{0} vote(s)")
	String votesLabel(int voteCount);

	@DefaultMessage("Answer")
	String answerColumnLabel();

	@DefaultMessage("Percentage")
	String percentageColumnLabel();

	@DefaultMessage("Options")
	String optionsButton();

	@DefaultMessage("Real-Time Update")
	String realTimeUpdateToggleButton();

	@DefaultMessage("Session Name")
	String sessionNamePlaceholder();

	@DefaultMessage("Start Session")
	String beginVotingSessionButton();

}
