package de.kune.client;

import static de.kune.client.VotingManagerServiceAsync.Util.getInstance;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.ColumnChart;
import com.googlecode.gwt.charts.client.corechart.ColumnChartOptions;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.googlecode.gwt.charts.client.options.VAxis;

import de.kune.client.ManagerModel.State;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Manager implements EntryPoint {

	public static native void closeWindow() /*-{
											$wnd.close();
											}-*/;

	private ManagerModel model = new ManagerModel();

	private Button closeVotingSessionButton;
	private Button endVotingRoundButton;
	private ToggleButton realTimeUpdateButton;
	private final ClickHandler closeVotingSessionHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			evaluateVotesTimer.cancel();
			votingService.closeVotingSession(model.getVotingSessionId(),
					new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							updateVisibilityStates();
						}

						@Override
						public void onSuccess(Void result) {
							updateVisibilityStates();
							closeWindow();
						}
					});
		}
	};
	private final ClickHandler endVotingRoundClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			endVotingRound();
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
											updateVotes(result);
											updateParticipantsPanel();
											evaluateParticipantsTimer
													.schedule(1000);
										}
									});
						}
					});
		}
	};
	private FlowPanel participantsPanel;

	private void updateParticipantsPanel() {
		Label participantsLabel = getParticipantsLabel();
		participantsLabel.setText(model.getVoteCount() + " vote(s)");
	}

	protected void endVotingRound() {
		getEndVotingRoundButton().setVisible(false);
		votingService.endVotingRound(model.getVotingSessionId(),
				new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						getEndVotingRoundButton().setVisible(true);
						getRealTimeUpdateButton().setVisible(true);
						evaluateVotes();
					}

					@Override
					public void onSuccess(Void result) {
						model.endVotingRound();
						updateVisibilityStates();
						evaluateVotes();
					}
				});
	}

	protected void updateVisibilityStates() {
		switch (model.getState()) {
		case NO_VOTIING_SESSION:
			getStartSessionPanel().setVisible(true);
			break;
		case VOTING_SESSION_STARTED:
			getStartNewVotingRoundButton().setVisible(true);
			getRealTimeUpdateButton().setVisible(false);
			getEndVotingRoundButton().setVisible(false);
			getCloseVotingSessionButton().setVisible(true);
			getVotingSessionPanel().setVisible(true);
			getOptionsPanel().setVisible(true);
			break;
		case VOTING_ROUND_STARTED:
			getStartNewVotingRoundButton().setVisible(false);
			getEndVotingRoundButton().setVisible(true);
			getRealTimeUpdateButton().setVisible(true);
			getCloseVotingSessionButton().setVisible(false);
			getVotingSessionPanel().setVisible(true);
			getOptionsPanel().setVisible(false);
			break;
		default:
			break;
		}
	}

	private Label getParticipantsLabel() {
		FlowPanel panel = getParticipantsPanel();
		if (panel.getWidgetCount() == 0) {
			panel.add(new Label());
		}
		return (Label) panel.getWidget(0);
	}

	private FlowPanel getParticipantsPanel() {
		if (participantsPanel == null) {
			participantsPanel = new FlowPanel();
		}
		return participantsPanel;
	}

	private void updateVotes(Map<String, Set<String>> votes) {
		model.updateVotes(votes);
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
						public void onSuccess(Map<String, Set<String>> result) {
							GWT.log("Current votes: " + result);
							if (model.getState() == State.VOTING_ROUND_STARTED) {
								updateVotes(result);
								updateVotesChart();
							}
							evaluateVotesTimer.schedule(1000);
						}
					});
		}
	};
	private final ClickHandler newVotingRoundClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			beginVotingRound();
		}
	};
	private final ClickHandler realTimeUpdateClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			if (((ToggleButton) event.getSource()).isDown()) {
				evaluateVotesTimer.schedule(1000);
			} else {
				evaluateVotesTimer.cancel();
			}
		}
	};
	private final ClickHandler startSessionClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			Window.open(GWT.getHostPageBaseURL()
					+ "manager.html?startVotingSession="
					+ getSessionNameTextBox().getText(), "_new",
					"right=20,top=20,width=400,height=800,toolbar=0,resizable=0");
		}
	};
	private TextBox sessionNameTextBox;
	private Button startNewVotingRoundButton;
	private Button startSessionButton;
	private Panel startSessionPanel;
	private final String startVotingSession = Window.Location
			.getParameter("startVotingSession");
	private ColumnChartOptions voteChartOptions;
	private Anchor voterImageQrLink;
	private ColumnChart votesChart;
	private SimplePanel votesChartPanel;

	private final VotingManagerServiceAsync votingService = getInstance();

	private FlowPanel votingSessionPanel;
	private FlowPanel buttonsPanel;
	private DisclosurePanel optionsPanel;

	private FlowPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new FlowPanel();
			buttonsPanel.setStyleName("buttonsPanel");
		}
		return buttonsPanel;
	}

	protected void beginVotingRound() {
		votingService.beginVotingRound(model.getVotingSessionId(),
				"New Voting Round", model.getOptions(),
				model.isMultipleSelectionAllowed(), new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						updateVisibilityStates();
						evaluateVotesTimer.cancel();
					}

					@Override
					public void onSuccess(Void result) {
						model.beginVotingRound();
						// getVoterQrCodeLink().setVisible(false);
						getVotesChartPanel().setVisible(true);
						getVotingSessionPanel().setVisible(true);
						updateVotesChart();
						getVotesChart().redraw();
						updateVisibilityStates();
						getOptionsPanel().setOpen(false);
						if (getRealTimeUpdateButton().isDown()
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
		mainPanel().add(getVotingSessionPanel());
		getVotingSessionPanel().clear();

		getButtonsPanel().add(getStartNewVotingRoundButton());
		getButtonsPanel().add(getEndVotingRoundButton());
		getButtonsPanel().add(getRealTimeUpdateButton());
		getButtonsPanel().add(getCloseVotingSessionButton());
		getButtonsPanel().add(getOptionsPanel());

		getVotingSessionPanel().add(getButtonsPanel());

		updateVisibilityStates();
		getStartNewVotingRoundButton().addClickHandler(
				newVotingRoundClickHandler);
		getEndVotingRoundButton().addClickHandler(endVotingRoundClickHandler);
		getCloseVotingSessionButton()
				.addClickHandler(closeVotingSessionHandler);
		getRealTimeUpdateButton().addClickHandler(realTimeUpdateClickHandler);

		SimplePanel pinCodePanel = new SimplePanel();
		pinCodePanel.setStyleName("pinCodePanel");
		getVotingSessionPanel().add(pinCodePanel);
		pinCodePanel.add(new Label(votingSessionPin));

		// SimplePanel qrCodePanel = new SimplePanel();
		// getVotingSessionPanel().add(qrCodePanel);
		// updateVoterImageQrLink();
		// qrCodePanel.add(getVoterQrCodeLink());

		getVotingSessionPanel().add(getVotesChartPanel());
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);
		chartLoader.loadApi(new Runnable() {
			@Override
			public void run() {
				GWT.log("Loaded " + ChartPackage.CORECHART);
				getVotesChartPanel().setVisible(false);
				getVotesChartPanel().add(getVotesChart());

				if (mainPanel().getElement().hasAttribute("auto-round")) {
					beginVotingRound();
				}
			}
		});

		mainPanel().add(getParticipantsPanel());
		evaluateParticipantsTimer.schedule(1000);

	}

	private DisclosurePanel getOptionsPanel() {
		if (optionsPanel == null) {
			optionsPanel = new DisclosurePanel("Options: A, B, C");
			getButtonsPanel().add(optionsPanel);
			Panel sp = new VerticalPanel();
			optionsPanel.add(sp);
			RadioButton abcOptions = new RadioButton("options", "A, B, C");
			abcOptions.setValue(true);
			abcOptions.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					optionsPanel.getHeaderTextAccessor().setText(
							"Options: A, B, C");
					model.updateOptions(new String[] { "A", "B", "C" }, true);
					optionsPanel.setOpen(false);
				}
			});
			sp.add(abcOptions);
			RadioButton yesNoOptions = new RadioButton("options", "Yes / No");
			sp.add(yesNoOptions);
			yesNoOptions.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					optionsPanel.getHeaderTextAccessor().setText(
							"Options: Yes / No");
					model.updateOptions(new String[] { "Yes", "No" }, false);
					optionsPanel.setOpen(false);
				}
			});
		}
		return optionsPanel;
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
						updateVotesChart();
					}
				});
	}

	private Button getCloseVotingSessionButton() {
		if (closeVotingSessionButton == null) {
			closeVotingSessionButton = new Button();
			closeVotingSessionButton.setText("End Voting Session");
		}
		return closeVotingSessionButton;
	}

	private Button getEndVotingRoundButton() {
		if (endVotingRoundButton == null) {
			endVotingRoundButton = new Button();
			endVotingRoundButton.setText("End Voting Round");
		}
		return endVotingRoundButton;
	}

	private ToggleButton getRealTimeUpdateButton() {
		if (realTimeUpdateButton == null) {
			realTimeUpdateButton = new ToggleButton("Real-Time Update");
		}
		return realTimeUpdateButton;
	}

	private TextBox getSessionNameTextBox() {
		if (sessionNameTextBox == null) {
			sessionNameTextBox = new TextBox();
			sessionNameTextBox.setText("New Voting Session");
		}
		return sessionNameTextBox;
	}

	private Button getStartNewVotingRoundButton() {
		if (startNewVotingRoundButton == null) {
			startNewVotingRoundButton = new Button();
			startNewVotingRoundButton.setText("Begin Voting Round");
		}
		return startNewVotingRoundButton;
	}

	private Button getStartSessionButton() {
		if (startSessionButton == null) {
			startSessionButton = new Button();
			startSessionButton.setText("Start Session");
		}
		return startSessionButton;
	}

	private Panel getStartSessionPanel() {
		if (startSessionPanel == null) {
			startSessionPanel = new FlowPanel();
		}
		return startSessionPanel;
	}

	private Anchor getVoterQrCodeLink() {
		if (voterImageQrLink == null) {
			updateVoterImageQrLink();
		}
		return voterImageQrLink;
	}

	private ColumnChart getVotesChart() {
		if (votesChart == null) {
			votesChart = new ColumnChart();
			votesChart.setWidth("100%");
			votesChart.setHeight("100%");
		}
		return votesChart;
	}

	private ColumnChartOptions getVotesChartOptions() {
		if (voteChartOptions == null) {
			VAxis vAxis = VAxis.create();
			vAxis.setMinValue(0);
			vAxis.setMaxValue(1);
			vAxis.setBaseline(0d);
			voteChartOptions = ColumnChartOptions.create();
			voteChartOptions.setVAxis(vAxis);
			vAxis.setFormat("#%");
			voteChartOptions.setLegend(Legend.create(LegendPosition.NONE));
		}
		return voteChartOptions;
	}

	private SimplePanel getVotesChartPanel() {
		if (votesChartPanel == null) {
			votesChartPanel = new SimplePanel();
			votesChartPanel.setHeight("350px");
			votesChartPanel.setWidth("400px");
		}
		return votesChartPanel;
	}

	private Panel getVotingSessionPanel() {
		if (votingSessionPanel == null) {
			votingSessionPanel = new FlowPanel();
		}
		return votingSessionPanel;
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		mainPanel().add(getStartSessionPanel());
		getStartSessionPanel().add(getSessionNameTextBox());
		getStartSessionPanel().add(getStartSessionButton());
		getStartSessionButton().addClickHandler(startSessionClickHandler);

		if (startVotingSession != null
				|| mainPanel().getElement().hasAttribute("start-session")) {
			getStartSessionPanel().setVisible(false);
			votingService.createVotingSession(startVotingSession,
					new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							getStartSessionPanel().setVisible(true);
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
											getStartSessionPanel().setVisible(
													true);
										}
									});
						}
					});
		}

	}

	private void updateVoterImageQrLink() {
		String voterUrl = GWT.getModuleBaseURL().replace(
				GWT.getModuleName() + "/", "")
				+ "?votingSessionId=" + model.getVotingSessionId();
		Image image = new Image(GWT.getModuleBaseURL().replace(
				GWT.getModuleName() + "/", "")
				+ "qr/img.png?url=" + URL.encode(voterUrl));
		image.setSize("400px", "400px");
		voterImageQrLink = new Anchor("", voterUrl, "_blank");
		voterImageQrLink.getElement().appendChild(image.getElement());

	}

	private void updateVotesChart() {
		GWT.log("Updating votes chart");
		DataTable votesData = DataTable.create();
		GWT.log("Updating votes chart data");
		votesData.addColumn(ColumnType.STRING, "Answer");
		votesData.addColumn(ColumnType.NUMBER, "Percentage");
		Map<String, Integer> answerCounts = new LinkedHashMap<String, Integer>();
		if (model.getOptions() != null) {
			for (String option : model.getOptions()) {
				answerCounts.put(option, 0);
			}
		}
		int votesCount = 0;
		GWT.log("Votes: " + model.getVotes());
		if (model.getVotes() != null) {
			votesCount = model.getVotes().keySet().size();
			for (Set<String> vote : model.getVotes().values()) {
				for (String option : vote) {
					Integer count = answerCounts.get(option);
					if (count != null) {
						answerCounts.put(option, ++count);
					}
				}
			}
		}
		GWT.log("Answers: " + answerCounts);
		for (Entry<String, Integer> e : answerCounts.entrySet()) {
			if (votesCount == 0) {
				votesData.addRow(e.getKey(), 0);
			} else {
				votesData.addRow(e.getKey(),
						(Double) ((double) e.getValue() / (double) votesCount));
			}
		}
		getVotesChart().draw(votesData, getVotesChartOptions());

	}

}
