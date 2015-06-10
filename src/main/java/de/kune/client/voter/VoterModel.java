package de.kune.client.voter;


public class VoterModel {

	private String votingSessionId;
	private String voterId;
	private String optionsKey;
	private boolean multipleSelectionAllowed;

	public boolean isMultipleSelectionAllowed() {
		return multipleSelectionAllowed;
	}

	public void joinSession(String votingSessionId, String voterId) {
		this.votingSessionId = votingSessionId;
		this.voterId = voterId;
	}

	public String getVotingSessionId() {
		return votingSessionId;
	}

	public void updateOptions(String optionsKey, boolean multipleSelectionAllowed) {
		this.optionsKey = optionsKey;
		this.multipleSelectionAllowed = multipleSelectionAllowed;
	}

	public String getOptionsKey() {
		return optionsKey;
	}

	public String getVoterId() {
		return voterId;
	}
	
}
