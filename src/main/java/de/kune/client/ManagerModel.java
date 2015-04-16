package de.kune.client;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides the manager model that is its state.
 */
public class ManagerModel {

	public static enum State {
		NO_VOTIING_SESSION, VOTING_SESSION_STARTED, VOTING_ROUND_STARTED
	}

	/**
	 * The current state.
	 */
	private State state;

	/**
	 * The options currently available for vote.
	 */
	private Options options = new Options(new LinkedHashSet<String>(
			Arrays.asList(new String[] { "A", "B", "C" })), true);

	/**
	 * The current voting session's id.
	 */
	private String votingSessionId;

	/**
	 * The current voting session's pin.
	 */
	private String votingSessionPin;

	/**
	 * The current voting session's participants.
	 */
	private Set<String> participants;

	/**
	 * The votes determined so far in the current voting session.
	 */
	protected Map<String, Set<String>> votes;

	public void beginSession(String votingSessionId, String votingSessionPin) {
		this.votingSessionId = votingSessionId;
		this.votingSessionPin = votingSessionPin;
		state = State.VOTING_SESSION_STARTED;
	}

	public String getVotingSessionId() {
		return votingSessionId;
	}

	public State getState() {
		return state;
	}

	public void endVotingRound() {
		state = State.VOTING_SESSION_STARTED;
	}

	public void updateVotes(Map<String, Set<String>> votes) {
		this.votes = votes;
	}

	public int getVoteCount() {
		return votes.size();
	}

	public void beginVotingRound() {
		state = State.VOTING_ROUND_STARTED;
		votes = null;
	}

	public Map<String, Set<String>> getVotes() {
		return votes;
	}

	public void updateParticipants(Set<String> participants) {
		this.participants = participants;
	}

	public String[] getOptions() {
		return options == null ? null : options.getOptions().toArray(
				new String[0]);
	}

	public boolean isMultipleSelectionAllowed() {
		return options == null ? null : options.isMultipleSelectionAllowed();
	}

	public void updateOptions(String[] options, boolean multipleSelectionAllowed) {
		this.options = new Options(new LinkedHashSet<String>(
				Arrays.asList(options == null ? new String[0] : options)),
				multipleSelectionAllowed);
	}

	public String getVotingSessionPin() {
		return votingSessionPin;
	}

	public Set<String> getParticipants() {
		return participants;
	}

}
