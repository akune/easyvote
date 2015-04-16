package de.kune.client;

import static de.kune.client.VotingManagerServiceAsync.Util.getInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

import de.kune.client.ManagerModel.State;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Manager implements EntryPoint {

	public static native void closeWindow() /*-{
											$wnd.close();
											}-*/;

	private ManagerModel model = new ManagerModel();
	private ManagerView view = new ManagerView();

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
							updateParticipants(result);
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
	private FlowPanel participantsPanel;

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

	private final String startVotingSession = Window.Location
			.getParameter("startVotingSession");

	private final VotingManagerServiceAsync votingService = getInstance();

	protected void beginVotingRound() {
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
								|| mainPanel().getElement().hasAttribute(
										"real-time-update")) {
							evaluateVotesTimer.schedule(1000);
						}
					}
				});
	}

	private void updateParticipants(Set<String> participants) {
		model.updateParticipants(participants);
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
								closeWindow();
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

	private Panel mainPanel() {
		return RootPanel.get("voting-manager");
	}

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

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		view.initialize(mainPanel());
		view.setModel(model);

		view.setStartVotingSessionAction(new Runnable() {
			@Override
			public void run() {
				view.startVotingSessionInNewWindow();
			}
		});

		if (startVotingSession != null
				|| mainPanel().getElement().hasAttribute("start-session")) {
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

}
