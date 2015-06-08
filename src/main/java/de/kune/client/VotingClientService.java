package de.kune.client;

import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import de.kune.client.common.Options;

@RemoteServiceRelativePath("vote")
public interface VotingClientService extends RemoteService {

	void vote(String sessionPin, String voterId, Set<String> options);

	Options getOptions(String sessionPin);

	String join(String sessionPin);

	void leave(String sessionPin, String voterId);

}
