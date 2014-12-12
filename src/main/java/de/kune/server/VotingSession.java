package de.kune.server;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class VotingSession implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;
	private final String name;
	private final Map<String, Set<String>> votes;
	private final Set<String> voters;
	private boolean votingRoundOpen;
	private Set<String> options;

	private String roundTitle;

	public String getRoundTitle() {
		return roundTitle;
	}

	public boolean isVotingRoundOpen() {
		return votingRoundOpen;
	}

	public Set<String> getOptions() {
		return options;
	}

	public VotingSession(String name) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.votes = new ConcurrentHashMap<String, Set<String>>();
		this.voters = new ConcurrentSkipListSet<String>();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Map<String, Set<String>> getVotes() {
		return new LinkedHashMap<String, Set<String>>(votes);
	}

	public void vote(String voterId, Set<String> options) {
		if (!voters.contains(voterId)) {
			throw new IllegalArgumentException("Unregistered voter");
		}
		if (!options.containsAll(options)) {
			throw new IllegalArgumentException("Invalid options");
		}
		votes.put(voterId, new LinkedHashSet<String>(options));
	}

	public void setRoundTitle(String title) {
		this.roundTitle = title;
	}

	public void resetVotes() {
		votes.clear();
	}

	public void setOptions(Set<String> options) {
		this.options = new LinkedHashSet<String>(options);
	}

	public void setVotingRoundOpen(boolean b) {
		votingRoundOpen = b;
	}

	public void addVoter(String voterId) {
		voters.add(voterId);
	}

	public Set<String> getVoters() {
		return new HashSet<String>(voters);
	}

}
