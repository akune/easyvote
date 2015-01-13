package de.kune.client;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("vote")
public interface VotingService extends RemoteService {

	String createVotingSession(String name);

	void closeVotingSession(String votingSessionId);

	void endVotingRound(String votingSessionId);

	Map<String, Set<String>> getVotes(String votingSessionId);

	void vote(String votingSessionId, String voterId, Set<String> options);

	void beginVotingRound(String votingSessionId, String title,
			String[] options);
	
	Set<String> getOptions(String votingSessionId);
	
	String join(String votingSessionId);

	Set<String> getVoters(String votingSessionId);
	
	void leave(String votingSessionId, String voterId);

}
