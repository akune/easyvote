package de.kune.client.voter;

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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;

import de.kune.client.common.OptionsConstants;

public class VoterView {

	static interface Action<R, P> {
		public R run(P param);
	}
	
	private final OptionsConstants constants = GWT
			.create(OptionsConstants.class);
	private final VoterMessages messages = GWT.create(VoterMessages.class);

	private Panel mainPanel;
	private VoterModel model;
	private TextBox pinTextBox;
	private Button joinButton;
	private Panel votingSessionPanel;
	private Panel waitingForVotingRoundPanel;
	private List<ToggleButton> votingOptionButtons;
	
	private ClickHandler votingOptionButtonClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			if (!model.isMultipleSelectionAllowed()) {
				for (ToggleButton b : getVotingOptionButtons()) {
					if (b != event.getSource()) {
						b.setDown(false);
					}
				}
			}
			triggerVote();
		}
	};
	private Action<Void, Set<String>> votingAction;

	
	public void setJoinSessionAction(final Action<Void, String> action) {
		pinTextBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == 13) {
					action.run(pinTextBox.getText().trim());
				}
			}
		});
		joinButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				action.run(pinTextBox.getText().trim());
			}
		});
	}
	
	public void initialize(Panel mainPanel) {
		this.mainPanel = mainPanel;
		FlowPanel pinPanel = new FlowPanel();
		mainPanel.add(pinPanel);
		pinTextBox = new TextBox();
		pinTextBox.getElement().setAttribute("type", "tel");
		pinTextBox.getElement().setAttribute("placeholder", messages.pinPlaceholder());
		pinPanel.add(pinTextBox);
		joinButton = new Button(messages.joinButton());
		pinPanel.add(joinButton);
		new Timer() {
			@Override
			public void run() {
				pinTextBox.setFocus(true);
			}
		}.schedule(500);
	}

	public void setModel(VoterModel model) {
		this.model = model;
	}

	public void joinSession() {
		Window.addWindowClosingHandler(new ClosingHandler() {
			@Override
			public void onWindowClosing(ClosingEvent event) {
				event.setMessage("Are you sure?");
			}
		});
//		Window.addCloseHandler(new CloseHandler<Window>() {
//			@Override
//			public void onClose(CloseEvent<Window> event) {
//				votingService.leave(votingSessionId, voterId,
//						new AsyncCallback<Void>() {
//							@Override
//							public void onSuccess(Void result) {
//								GWT.log("Left session "
//										+ votingSessionId);
//							}
//
//							@Override
//							public void onFailure(Throwable caught) {
//								GWT.log("Could not leave.");
//							}
//						});
//
//			}
//		});
		mainPanel.clear();
		mainPanel.add(getVotingSessionPanel());
		mainPanel.add(getWaitingForVotingRoundPanel());
	}
	
	private Panel getVotingSessionPanel() {
		if (votingSessionPanel == null) {
			votingSessionPanel = new FlowPanel();
		}
		return votingSessionPanel;
	}

	private Panel getWaitingForVotingRoundPanel() {
		if (waitingForVotingRoundPanel == null) {
			waitingForVotingRoundPanel = new FlowPanel();
			waitingForVotingRoundPanel.add(new Label(messages
					.waitingForVotingRound()));
		}
		return waitingForVotingRoundPanel;
	}

	public void showNoSuchSessionHint() {
		mainPanel.add(new Label(messages.noSuchSession()));
	}
	
	private String[] getOptionsValues() {
		return model.getOptionsKey() == null
				|| "none".equals(model.getOptionsKey()) ? new String[0]
				: constants.getStringArray(model.getOptionsKey());
	}

	public void updateOptions() {
		String[] optionValues = getOptionsValues();
		GWT.log("Options: " + Arrays.toString(optionValues) + "/"
				+ optionValues.length);
		while (getVotingOptionButtons().size() > optionValues.length) {
			removeLastVotingOptionButton();
		}
		while (getVotingOptionButtons().size() < optionValues.length) {
			addVotingOptionButton();
		}
		for (int i = 0; i < optionValues.length; i++) {
			try {
				getVotingOptionButtons().get(i).setText(optionValues[i]);
			} catch (NullPointerException e) {
				// Ignore
			}
		}
		getWaitingForVotingRoundPanel().setVisible(optionValues.length == 0);
	}

	private List<ToggleButton> getVotingOptionButtons() {
		if (votingOptionButtons == null) {
			votingOptionButtons = new LinkedList<ToggleButton>();
		}
		return votingOptionButtons;
	}

	private void removeLastVotingOptionButton() {
		if (getVotingOptionButtons().size() >= 0) {
			ToggleButton button = getVotingOptionButtons().get(
					getVotingOptionButtons().size() - 1);
			getVotingOptionButtons().remove(button);
			mainPanel.remove(button);
		}
	}

	private void addVotingOptionButton() {
		ToggleButton button = new ToggleButton();
		button.addClickHandler(votingOptionButtonClickHandler);
		mainPanel.add(button);
		getVotingOptionButtons().add(button);
	}

	protected void triggerVote() {
		GWT.log("Triggering vote.");
		voteTimer.cancel();
		voteTimer.schedule(1000);
	}

	private Set<String> determineVote() {
		HashSet<String> options = new HashSet<String>();
		List<String> availableOptions = Arrays.asList(getOptionsValues());
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
	
	public void setVotingAction(Action<Void, Set<String>> votingAction) {
		this.votingAction = votingAction;
	}

	private Timer voteTimer = new Timer() {
		@Override
		public void run() {
			GWT.log("Voting: " + determineVote());
			votingAction.run(determineVote());
		}
	};

}
