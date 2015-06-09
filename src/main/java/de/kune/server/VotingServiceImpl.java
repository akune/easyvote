package de.kune.server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kune.client.common.Options;
import de.kune.shared.VotingClientService;
import de.kune.shared.VotingManagerService;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class VotingServiceImpl extends RemoteServiceServlet implements
		VotingManagerService, VotingClientService {

	private static final String SERVLET_CONTEXT_DATA_KEY = "de.kune.easyvote.data";
	private static final long TIMEOUT_IN_MILLIS = 30 * 60 * 1000;
	private ConcurrentMap<String, Object> data;
	private ConcurrentMap<String, VotingSession> votingSessionsBySessionId;
	private ConcurrentMap<String, VotingSession> votingSessionsBySessionPin;

	@Override
	@SuppressWarnings("unchecked")
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		synchronized (getServletContext()) {
			data = (ConcurrentMap<String, Object>) getServletContext()
					.getAttribute(SERVLET_CONTEXT_DATA_KEY);
			if (data == null) {
				getServletContext().setAttribute(SERVLET_CONTEXT_DATA_KEY,
						data = new ConcurrentHashMap<String, Object>());
			}
		}
		data.putIfAbsent("votingSessionsBySessionId",
				new ConcurrentHashMap<String, VotingSession>());
		data.putIfAbsent("votingSessionsBySessionPin",
				new ConcurrentHashMap<String, VotingSession>());
		votingSessionsBySessionId = (ConcurrentMap<String, VotingSession>) data
				.get("votingSessionsBySessionId");
		votingSessionsBySessionPin = (ConcurrentMap<String, VotingSession>) data
				.get("votingSessionsBySessionPin");
	}

	@Override
	public String createVotingSession(String name) {
		VotingSession session = new VotingSession(name);
		while (session != votingSessionsBySessionPin.putIfAbsent(
				session.getPin(), session))
			;
		votingSessionsBySessionId.put(session.getId(), session);
		return session.getId();
	}

	private VotingSession getVotingSessionBySessionId(String votingSessionId) {
		VotingSession result = votingSessionsBySessionId.get(votingSessionId);
		if (result == null
				|| result.getActivityTimestamp() < System.currentTimeMillis()
						- TIMEOUT_IN_MILLIS) {
			if (result != null) {
				votingSessionsBySessionId.remove(votingSessionId);
				votingSessionsBySessionPin.remove(result.getPin());
			}
			throw new IllegalArgumentException("No such voting session");
		}
		return result;
	}

	private VotingSession getVotingSessionBySessionPin(String pin) {
		VotingSession result = votingSessionsBySessionPin.get(pin);
		if (result == null
				|| result.getActivityTimestamp() < System.currentTimeMillis()
						- TIMEOUT_IN_MILLIS) {
			if (result != null) {
				votingSessionsBySessionId.remove(result.getId());
				votingSessionsBySessionPin.remove(pin);
			}
			throw new IllegalArgumentException("No such voting session");
		}
		return result;
	}

	@Override
	public void vote(String sessionPin, String voterId, Set<String> options) {
		if (!getVotingSessionBySessionPin(sessionPin).isVotingRoundOpen()) {
			throw new IllegalStateException("Voting is currently not allowed");
		}
		getVotingSessionBySessionPin(sessionPin).vote(voterId, options);
	}

	@Override
	public void beginVotingRound(String sessionId, String title,
			String optionsKey, boolean multipleSelectionAllowed) {
		VotingSession session = getVotingSessionBySessionId(sessionId);
		session.setRoundTitle(title);
		session.resetVotes();
		session.setOptions(optionsKey,
				multipleSelectionAllowed);
		session.setVotingRoundOpen(true);
	}

	@Override
	public void endVotingRound(String sessionId) {
		getVotingSessionBySessionId(sessionId).setVotingRoundOpen(false);
	}

	@Override
	public Map<String, Set<String>> getVotes(String votingSessionId) {
		return getVotingSessionBySessionId(votingSessionId).getVotes();
	}

	@Override
	public void closeVotingSession(String sessionPin) {
		votingSessionsBySessionId.remove(sessionPin);
	}

	@Override
	public Options getOptions(String sessionPin) {
		return getVotingSessionBySessionPin(sessionPin).isVotingRoundOpen() ? getVotingSessionBySessionPin(
				sessionPin).getOptions()
				: new Options(null, false);
	}

	@Override
	public Set<String> getVoters(String votingSessionId) {
		return getVotingSessionBySessionId(votingSessionId).getVoters();
	}

	@Override
	public String join(String sessionPin) {
		String voterId = UuidUtil.getCompressedUuid(true);
		getVotingSessionBySessionPin(sessionPin).addVoter(voterId);
		return voterId;
	}

	@Override
	public void leave(String sessionPin, String voterId) {
		System.out.println(voterId + " is leaving " + sessionPin);
		getVotingSessionBySessionPin(sessionPin).removeVoter(voterId);
	}

	@Override
	public String getSessionPin(String sessionId) {
		return getVotingSessionBySessionId(sessionId).getPin();
	}

}
