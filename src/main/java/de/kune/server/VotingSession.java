package de.kune.server;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class VotingSession implements Serializable {

	private static final long serialVersionUID = 1L;

	private static String generatePin(int length) {
		final char[] alphabet = "0123456789".toCharArray();
		StringBuilder b = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			b.append(alphabet[(int) (Math.random() * alphabet.length)]);
		}
		return b.toString();
	}

	private final String id;
	private final String name;
	private final Map<String, Set<String>> votes;
	private final Set<String> voters;
	private boolean votingRoundOpen;
	private Set<String> options;
	private long activityTimestamp;

	private String roundTitle;

	public VotingSession(String name) {
		// this.id = UuidUtil.getCompressedUuid(true);
		this.id = generatePin(6);
		this.name = name;
		this.votes = new ConcurrentHashMap<String, Set<String>>();
		this.voters = new ConcurrentSkipListSet<String>();
		updateActivityTimestamp();
	}

	public String getRoundTitle() {
		updateActivityTimestamp();
		return roundTitle;
	}

	public boolean isVotingRoundOpen() {
		updateActivityTimestamp();
		return votingRoundOpen;
	}

	public Set<String> getOptions() {
		updateActivityTimestamp();
		return options;
	}

	public String getId() {
		updateActivityTimestamp();
		return id;
	}

	public String getName() {
		updateActivityTimestamp();
		return name;
	}

	public Map<String, Set<String>> getVotes() {
		updateActivityTimestamp();
		return new LinkedHashMap<String, Set<String>>(votes);
	}

	public void vote(String voterId, Set<String> options) {
		updateActivityTimestamp();
		if (!voters.contains(voterId)) {
			throw new IllegalArgumentException("Unregistered voter");
		}
		if (!options.containsAll(options)) {
			throw new IllegalArgumentException("Invalid options");
		}
		votes.put(voterId, new LinkedHashSet<String>(options));
	}

	public void setRoundTitle(String title) {
		updateActivityTimestamp();
		this.roundTitle = title;
	}

	public void resetVotes() {
		updateActivityTimestamp();
		votes.clear();
	}

	public void setOptions(Set<String> options) {
		updateActivityTimestamp();
		this.options = new LinkedHashSet<String>(options);
	}

	public void setVotingRoundOpen(boolean b) {
		updateActivityTimestamp();
		votingRoundOpen = b;
	}

	public void addVoter(String voterId) {
		updateActivityTimestamp();
		voters.add(voterId);
	}

	public Set<String> getVoters() {
		updateActivityTimestamp();
		return new HashSet<String>(voters);
	}

	private void updateActivityTimestamp() {
		activityTimestamp = System.currentTimeMillis();
	}

	public long getActivityTimestamp() {
		return activityTimestamp;
	}

	public void removeVoter(String voterId) {
		updateActivityTimestamp();
		votes.remove(voterId);
		voters.remove(voterId);
	}

}
