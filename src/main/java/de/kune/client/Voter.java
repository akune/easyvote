package de.kune.client;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Voter implements EntryPoint {

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final VotingServiceAsync votingService = GWT
			.create(VotingService.class);

	private FlowPanel votingSessionPanel;

	String voterId;
	final String votingSessionId = Window.Location
			.getParameter("votingSessionId");

	private List<ToggleButton> votingOptionButtons;

	private ClickHandler votingOptionButtonClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			triggerVote();
		}
	};

	// private final Messages messages = GWT.create(Messages.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		votingService.join(votingSessionId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				voterId = result;
				RootPanel.get()
						.add(new Label("Welcome to Easy Vote, your ID is "
								+ voterId));
				RootPanel.get().add(getVotingSessionPanel());
				RootPanel.get().add(getWaitingForVotingRoundPanel());
				update();
			}

			@Override
			public void onFailure(Throwable caught) {
				RootPanel
						.get()
						.add(new Label(
								"Welcome to Easy Vote - There is no such session"));
			}
		});
	}

	private Timer voteTimer = new Timer() {
		@Override
		public void run() {
			GWT.log("Voting: " + determineVote());
			votingService.vote(votingSessionId, voterId, determineVote(),
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
		}
	};

	private FlowPanel waitingForVotingRoundPanel;

	private Set<String> determineVote() {
		HashSet<String> options = new HashSet<String>();
		for (ToggleButton button : getVotingOptionButtons()) {
			if (button.isDown()) {
				options.add(button.getText());
			}
		}
		return options;
	}

	protected void triggerVote() {
		voteTimer.cancel();
		voteTimer.schedule(1000);
	}

	private void updateOptions(Set<String> options) {
		while (getVotingOptionButtons().size() > options.size()) {
			removeLastVotingOptionButton();
		}
		while (getVotingOptionButtons().size() < options.size()) {
			addVotingOptionButton();
		}
		String[] o = options.toArray(new String[0]);
		for (int i = 0; i < options.size(); i++) {
			try {
				getVotingOptionButtons().get(i).setText(o[i]);
			} catch (NullPointerException e) {
				// Ignore
			}
		}
		getWaitingForVotingRoundPanel().setVisible(options.isEmpty());
	}

	private Panel getWaitingForVotingRoundPanel() {
		if (waitingForVotingRoundPanel == null) {
			waitingForVotingRoundPanel = new FlowPanel();
			waitingForVotingRoundPanel.add(new Label(
					"Waiting for voting round to begin..."));
		}
		return waitingForVotingRoundPanel;
	}

	private void addVotingOptionButton() {
		ToggleButton button = new ToggleButton();
		button.addClickHandler(votingOptionButtonClickHandler);
		RootPanel.get().add(button);
		getVotingOptionButtons().add(button);
	}

	private void removeLastVotingOptionButton() {
		if (getVotingOptionButtons().size() > 0) {
			ToggleButton button = getVotingOptionButtons().get(
					getVotingOptionButtons().size() - 1);
			getVotingOptionButtons().remove(button);
			RootPanel.get().remove(button);
		}
	}

	private List<ToggleButton> getVotingOptionButtons() {
		if (votingOptionButtons == null) {
			votingOptionButtons = new LinkedList<ToggleButton>();
		}
		return votingOptionButtons;
	}

	private void update() {
		votingService.getOptions(votingSessionId,
				new AsyncCallback<Set<String>>() {

					@Override
					public void onSuccess(Set<String> result) {
						updateOptions(result);
						new Timer() {
							@Override
							public void run() {
								update();
							}
						}.schedule(1000);
					}

					@Override
					public void onFailure(Throwable caught) {
						new Timer() {
							@Override
							public void run() {
								update();
							}
						}.schedule(1000);
					}
				});
	}

	private Panel getVotingSessionPanel() {
		if (votingSessionPanel == null) {
			votingSessionPanel = new FlowPanel();
		}
		return votingSessionPanel;
	}

}
