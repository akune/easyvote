package de.kune.client;

import static de.kune.client.VotingManagerServiceAsync.Util.getInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kune.client.ManagerModel.State;

/**
 * Controls the manager view and model.
 */
public class ManagerController {

	private final String startVotingSession = Window.Location
			.getParameter("startVotingSession");

	private final VotingManagerServiceAsync votingService = getInstance();


	private ManagerModel model;
	private ManagerView view;

	public void initialize(ManagerModel model, ManagerView view) {
		this.model = model;
		this.view = view;

		view.setStartVotingSessionAction(new Runnable() {
			@Override
			public void run() {
				ManagerController.this.view.startVotingSessionInNewWindow();
			}
		});

		if (startVotingSession != null
				|| view.getMainPanel().getElement().hasAttribute("start-session")) {
			// getStartSessionPanel().setVisible(false);
			votingService.createVotingSession(startVotingSession,
					new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							// getStartSessionPanel().setVisible(true);
						}

						@Override
						public void onSuccess(final String votingSessionId) {
							votingService.getSessionPin(votingSessionId,
									new AsyncCallback<String>() {

										@Override
										public void onSuccess(
												String votingSessionPin) {
											beginVotingSession(votingSessionId,
													votingSessionPin);
										}

										@Override
										public void onFailure(Throwable caught) {
											// getStartSessionPanel().setVisible(
											// true);
										}
									});
						}
					});
		}

	}

	private void beginVotingSession(String votingSessionId,
			String votingSessionPin) {
		model.beginSession(votingSessionId, votingSessionPin);
		view.beginSession(new Runnable() {
			@Override
			public void run() {
				if (view.getMainPanel().getElement().hasAttribute("auto-round")) {
					beginVotingRound();
				}
			}
		});

		view.updateVisibilityStates();
		view.setStartNewVotingRoundAction(new Runnable() {
			@Override
			public void run() {
				beginVotingRound();
			}
		});
		view.setEndVotingRoundAction(new Runnable() {
			@Override
			public void run() {
				endVotingRound();
			}
		});
		view.setCloseVotingSessionAction(new Runnable() {
			@Override
			public void run() {
				evaluateVotesTimer.cancel();
				votingService.closeVotingSession(model.getVotingSessionId(),
						new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								view.updateVisibilityStates();
							}

							@Override
							public void onSuccess(Void result) {
								view.updateVisibilityStates();
								view.closeWindow();
							}
						});
			}
		});
		view.setRealTimeUpdateActions(new Runnable() {
			@Override
			public void run() {
				evaluateVotesTimer.schedule(1000);
			}
		}, new Runnable() {
			@Override
			public void run() {
				evaluateVotesTimer.cancel();
			}
		});
		evaluateParticipantsTimer.schedule(1000);
	}

	private void beginVotingRound() {
		votingService.beginVotingRound(model.getVotingSessionId(),
				"Voting Round", model.getOptions(),
				model.isMultipleSelectionAllowed(), new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						view.updateVisibilityStates();
						evaluateVotesTimer.cancel();
					}

					@Override
					public void onSuccess(Void result) {
						model.beginVotingRound();
						// getVoterQrCodeLink().setVisible(false);
						model.updateVotes(new HashMap<String, Set<String>>());
						view.updateVisibilityStates();
						view.updateVotesChart(false);
						view.closeOptionsPanel();
						if (view.isRealTimeUpdateSelected()
								|| view.getMainPanel().getElement().hasAttribute(
										"real-time-update")) {
							evaluateVotesTimer.schedule(1000);
						}
					}
				});
	}

	protected void endVotingRound() {
		// getEndVotingRoundButton().setVisible(false);
		votingService.endVotingRound(model.getVotingSessionId(),
				new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						// getEndVotingRoundButton().setVisible(true);
						// getRealTimeUpdateButton().setVisible(true);
						// evaluateVotes();
					}

					@Override
					public void onSuccess(Void result) {
						model.endVotingRound();
						view.updateVisibilityStates();
						evaluateVotes();
					}
				});
	}

	private final Timer evaluateVotesTimer = new Timer() {
		@Override
		public void run() {
			GWT.log("Evaluating Votes for " + model.getVotingSessionId());
			votingService.getVotes(model.getVotingSessionId(),
					new AsyncCallback<Map<String, Set<String>>>() {

						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Could not get votes");
							evaluateVotesTimer.schedule(1000);
						}

						@Override
						public void onSuccess(Map<String, Set<String>> votes) {
							GWT.log("Current votes: " + votes);
							if (model.getState() == State.VOTING_ROUND_STARTED) {
								model.updateVotes(votes);
								view.updateVotesChart(false);
							}
							evaluateVotesTimer.schedule(1000);
						}
					});
		}
	};

	private final Timer evaluateParticipantsTimer = new Timer() {
		@Override
		public void run() {
			GWT.log("Evaluating participants for " + model.getVotingSessionId());
			votingService.getVoters(model.getVotingSessionId(),
					new AsyncCallback<Set<String>>() {
						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Could not get voters");
							evaluateParticipantsTimer.schedule(1000);
						}

						@Override
						public void onSuccess(Set<String> result) {
							GWT.log("Current voters: " + result);
							model.updateParticipants(result);
							votingService.getVotes(
									model.getVotingSessionId(),
									new AsyncCallback<Map<String, Set<String>>>() {
										@Override
										public void onFailure(Throwable caught) {
											GWT.log("Could not get votes");
											evaluateParticipantsTimer
													.schedule(1000);
										}

										@Override
										public void onSuccess(
												Map<String, Set<String>> result) {
											model.updateVotes(result);
											view.updateParticipantsPanel();
											evaluateParticipantsTimer
													.schedule(1000);
										}
									});
						}
					});
		}
	};
	
	private void evaluateVotes() {
		GWT.log("Evaluating Votes for " + model.getVotingSessionId());
		votingService.getVotes(model.getVotingSessionId(),
				new AsyncCallback<Map<String, Set<String>>>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Could not get votes");
					}

					@Override
					public void onSuccess(Map<String, Set<String>> votes) {
						GWT.log("Current votes: " + votes);
						model.updateVotes(votes);
						view.updateVotesChart(false);
					}
				});
	}

}
