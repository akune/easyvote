package de.kune.shared;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("manage")
public interface VotingManagerService extends RemoteService {

	String createVotingSession(String name);

	String getSessionPin(String sessionId);

	void closeVotingSession(String sessionId);

	void endVotingRound(String sessionId);

	Map<String, Set<String>> getVotes(String sessionId);
	
	Map<String, Set<String>> waitForNewVotes(String sessionId);

	void beginVotingRound(String sessionId, String title, String optionsKey,
			boolean multipleSelectionAllowed);

	Set<String> getVoters(String sessionId);

}
