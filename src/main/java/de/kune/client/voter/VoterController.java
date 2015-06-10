package de.kune.client.voter;

import static de.kune.shared.VotingClientServiceAsync.Util.getInstance;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kune.client.common.Options;
import de.kune.shared.VotingClientServiceAsync;

public class VoterController {

	private final VotingClientServiceAsync votingService = getInstance();
	private VoterModel model;
	private VoterView view;

	public void initialize(VoterModel model, VoterView view) {
		this.model = model;
		this.view = view;
		view.setJoinSessionAction(new VoterView.Action<Void, String>() {
			@Override
			public Void run(String param) {
				joinSession(param);
				return null;
			}
		});
		view.setVotingAction(new VoterView.Action<Void, Set<String>>() {
			@Override
			public Void run(Set<String> vote) {
				votingService.vote(
						VoterController.this.model.getVotingSessionId(),
						VoterController.this.model.getVoterId(), vote,
						new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								// Do nothing.
							}
							@Override
							public void onFailure(Throwable caught) {
								// Do nothing.
							}
						});
				return null;
			}
		});
	}

	private void joinSession(final String votingSessionId) {
		votingService.join(votingSessionId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				GWT.log("Joined session " + votingSessionId);

				model.joinSession(votingSessionId, result);
				view.joinSession();
				update();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showNoSuchSessionHint();
			}
		});
	}

	private void update() {
		votingService.getOptions(model.getVotingSessionId(),
				new AsyncCallback<Options>() {
					@Override
					public void onSuccess(Options result) {
						GWT.log("Updated. Options: " + result);
						model.updateOptions(result.getOptionsKey(),
								result.isMultipleSelectionAllowed());
						view.updateOptions();
						new Timer() {
							@Override
							public void run() {
								update();
							}
						}.schedule(1000);
						GWT.log("Scheduled next update.");
					}

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Updating failed.");
						new Timer() {
							@Override
							public void run() {
								update();
							}
						}.schedule(1000);
					}
				});
	}

	// TODO: Provide controller methods.

}
