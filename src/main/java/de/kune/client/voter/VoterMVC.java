package de.kune.client.voter;

import static de.kune.shared.VotingClientServiceAsync.Util.getInstance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;

import de.kune.shared.VotingClientServiceAsync;
import de.kune.client.common.Options;
import de.kune.client.common.OptionsConstants;

public class VoterMVC {

	private final VotingClientServiceAsync votingService = getInstance();

	private FlowPanel votingSessionPanel;

	private String voterId;
	private String votingSessionId = Window.Location
			.getParameter("votingSessionId");

	private List<ToggleButton> votingOptionButtons;

	protected boolean multipleSelectionAllowed;

	private ClickHandler votingOptionButtonClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			if (!multipleSelectionAllowed) {
				for (ToggleButton b : getVotingOptionButtons()) {
					if (b != event.getSource()) {
						b.setDown(false);
					}
				}
			}
			triggerVote();
		}
	};

	private final Panel mainPanel;

	private final VoterMessages messages;

	private void joinSession(final String votingSessionId) {
		votingService.join(votingSessionId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				GWT.log("Joined session " + votingSessionId);
				Window.addWindowClosingHandler(new ClosingHandler() {
					@Override
					public void onWindowClosing(ClosingEvent event) {
						event.setMessage("Are you sure?");
					}
				});
				Window.addCloseHandler(new CloseHandler<Window>() {
					@Override
					public void onClose(CloseEvent<Window> event) {
						GWT.log("Sending leave message for " + voterId
								+ " to session " + votingSessionId + ".");
						votingService.leave(votingSessionId, voterId,
								new AsyncCallback<Void>() {
									@Override
									public void onSuccess(Void result) {
										GWT.log("Left session "
												+ votingSessionId);
									}

									@Override
									public void onFailure(Throwable caught) {
										GWT.log("Could not leave.");
									}
								});

					}
				});

				mainPanel().clear();
				voterId = result;
				VoterMVC.this.votingSessionId = votingSessionId;
				// mainPanel()
				// .add(new Label("Welcome to Easy Vote, your ID is "
				// + voterId));
				mainPanel().add(getVotingSessionPanel());
				mainPanel().add(getWaitingForVotingRoundPanel());
				update();
			}

			@Override
			public void onFailure(Throwable caught) {
				mainPanel().add(new Label(messages.noSuchSession()));
			}
		});
	}

	protected Panel mainPanel() {
		return mainPanel;
	}

	public VoterMVC(Panel mainPanel, VoterMessages messages) {
		this.mainPanel = mainPanel;
		this.messages = messages;
		if (votingSessionId == null) {
			FlowPanel pinPanel = new FlowPanel();
			mainPanel().add(pinPanel);
			final TextBox pinTextBox = new TextBox();
			pinTextBox.getElement().setAttribute("type", "tel");
			pinTextBox.getElement().setAttribute("placeholder", messages.pinPlaceholder());
			pinTextBox.addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					if (event.getNativeEvent().getKeyCode() == 13) {
						joinSession(pinTextBox.getText().trim());
					}
				}
			});
			pinPanel.add(pinTextBox);
			Button joinButton = new Button(messages.joinButton());
			pinPanel.add(joinButton);
			joinButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					joinSession(pinTextBox.getText().trim());
				}
			});
			new Timer() {
				@Override
				public void run() {
					pinTextBox.setFocus(true);
				}
				
			}.schedule(500);
		} else {
			joinSession(votingSessionId);
		}
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
		List<String> availableOptions = Arrays.asList(optionValues);
		for (ToggleButton button : getVotingOptionButtons()) {
			if (button.isDown()) {
				int index = availableOptions.indexOf(button.getText());
				if (index >= 0) {
					options.add(Integer.toString(index));
				}
			}
		}
		return options;
	}

	protected void triggerVote() {
		GWT.log("Triggering vote.");
		voteTimer.cancel();
		voteTimer.schedule(1000);
	}

	private final OptionsConstants constants = GWT
			.create(OptionsConstants.class);

	private String[] optionValues;

	private void updateOptions(Options options) {
		this.optionValues = "none".equals(options.getOptionsKey()) ? new String[0]
				: constants.getStringArray(options.getOptionsKey());
		GWT.log("Options: " + Arrays.toString(optionValues) + "/"
				+ optionValues.length);
		while (getVotingOptionButtons().size() > optionValues.length) {
			removeLastVotingOptionButton();
		}
		while (getVotingOptionButtons().size() < optionValues.length) {
			addVotingOptionButton();
		}
		this.multipleSelectionAllowed = options.isMultipleSelectionAllowed();
		for (int i = 0; i < optionValues.length; i++) {
			try {
				getVotingOptionButtons().get(i).setText(optionValues[i]);
			} catch (NullPointerException e) {
				// Ignore
			}
		}
		getWaitingForVotingRoundPanel().setVisible(optionValues.length == 0);
	}

	private Panel getWaitingForVotingRoundPanel() {
		if (waitingForVotingRoundPanel == null) {
			waitingForVotingRoundPanel = new FlowPanel();
			waitingForVotingRoundPanel.add(new Label(messages
					.waitingForVotingRound()));
		}
		return waitingForVotingRoundPanel;
	}

	private void addVotingOptionButton() {
		ToggleButton button = new ToggleButton();
		button.addClickHandler(votingOptionButtonClickHandler);
		mainPanel().add(button);
		getVotingOptionButtons().add(button);
	}

	private void removeLastVotingOptionButton() {
		if (getVotingOptionButtons().size() >= 0) {
			ToggleButton button = getVotingOptionButtons().get(
					getVotingOptionButtons().size() - 1);
			getVotingOptionButtons().remove(button);
			mainPanel().remove(button);
		}
	}

	private List<ToggleButton> getVotingOptionButtons() {
		if (votingOptionButtons == null) {
			votingOptionButtons = new LinkedList<ToggleButton>();
		}
		return votingOptionButtons;
	}

	private void update() {
		votingService.getOptions(votingSessionId, new AsyncCallback<Options>() {
			@Override
			public void onSuccess(Options result) {
				GWT.log("Updated. Options: " + result);
				updateOptions(result);
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

	private Panel getVotingSessionPanel() {
		if (votingSessionPanel == null) {
			votingSessionPanel = new FlowPanel();
		}
		return votingSessionPanel;
	}

}
