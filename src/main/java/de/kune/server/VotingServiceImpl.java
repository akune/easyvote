package de.kune.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kune.client.VotingService;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class VotingServiceImpl extends RemoteServiceServlet implements
		VotingService {

	private static final String SERVLET_CONTEXT_DATA_KEY = "de.kune.easyvote.data";
	private static final long TIMEOUT_IN_MILLIS = 30 * 60 * 1000;
	private ConcurrentMap<String, VotingSession> votingSessions;

	@Override
	@SuppressWarnings("unchecked")
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		synchronized (getServletContext()) {
			votingSessions = (ConcurrentMap<String, VotingSession>) getServletContext()
					.getAttribute(SERVLET_CONTEXT_DATA_KEY);
			if (votingSessions == null) {
				getServletContext()
						.setAttribute(
								SERVLET_CONTEXT_DATA_KEY,
								votingSessions = new ConcurrentHashMap<String, VotingSession>());
			}
		}
	}

	@Override
	public String createVotingSession(String name) {
		VotingSession session = new VotingSession(name);
		while (session != votingSessions.putIfAbsent(session.getId(), session))
			;
		return session.getId();
	}

	private VotingSession getVotingSession(String votingSessionId) {
		VotingSession result = votingSessions.get(votingSessionId);
		if (result == null
				|| result.getActivityTimestamp() < System.currentTimeMillis()
						- TIMEOUT_IN_MILLIS) {
			votingSessions.remove(votingSessionId);
			throw new IllegalArgumentException("No such voting session");
		}
		return result;
	}

	@Override
	public void vote(String votingSessionId, String voterId, Set<String> options) {
		if (!getVotingSession(votingSessionId).isVotingRoundOpen()) {
			throw new IllegalStateException("Voting is currently not allowed");
		}
		getVotingSession(votingSessionId).vote(voterId, options);
	}

	@Override
	public void beginVotingRound(String votingSessionId, String title,
			String[] options) {
		VotingSession session = getVotingSession(votingSessionId);
		session.setRoundTitle(title);
		session.resetVotes();
		session.setOptions(new LinkedHashSet<String>(Arrays.asList(options)));
		session.setVotingRoundOpen(true);
	}

	@Override
	public void endVotingRound(String votingSessionId) {
		getVotingSession(votingSessionId).setVotingRoundOpen(false);
	}

	@Override
	public Map<String, Set<String>> getVotes(String votingSessionId) {
		return getVotingSession(votingSessionId).getVotes();
	}

	@Override
	public void closeVotingSession(String votingSessionId) {
		votingSessions.remove(votingSessionId);
	}

	@Override
	public Set<String> getOptions(String votingSessionId) {
		return getVotingSession(votingSessionId).isVotingRoundOpen() ? getVotingSession(
				votingSessionId).getOptions()
				: Collections.<String> emptySet();
	}

	@Override
	public Set<String> getVoters(String votingSessionId) {
		return getVotingSession(votingSessionId).getVoters();
	}

	@Override
	public String join(String votingSessionId) {
		String voterId = UuidUtil.getCompressedUuid(true);
		getVotingSession(votingSessionId).addVoter(voterId);
		return voterId;
	}

	@Override
	public void leave(String votingSessionId, String voterId) {
		System.out.println(voterId + " is leaving " + votingSessionId);
		getVotingSession(votingSessionId).removeVoter(voterId);
	}

}
