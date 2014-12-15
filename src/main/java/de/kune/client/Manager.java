package de.kune.client;

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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.BarChart;
import com.googlecode.gwt.charts.client.corechart.BarChartOptions;
import com.googlecode.gwt.charts.client.options.HAxis;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Manager implements EntryPoint {

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final VotingServiceAsync votingService = GWT
			.create(VotingService.class);
	private String votingSessionId;
	private FlowPanel votingSessionPanel;
	private Button startNewVotingRoundButton;
	private Button endVotingRoundButton;
	private TextBox sessionNameTextBox;
	private Button startSessionButton;
	private Panel startSessionPanel;
	private Button closeVotingSessionButton;
	protected Map<String, Set<String>> votes;

	// private final Messages messages = GWT.create(Messages.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		RootPanel.get().add(new Label("Welcome to Easy Vote Manager"));
		RootPanel.get().add(getStartSessionPanel());
		getStartSessionPanel().add(getSessionNameTextBox());
		getStartSessionPanel().add(getStartSessionButton());
		getStartSessionButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				getStartSessionPanel().setVisible(false);
				votingService.createVotingSession(getSessionNameTextBox()
						.getText(), new AsyncCallback<String>() {

					@Override
					public void onSuccess(String votingSessionId) {
						beginVotingSession(votingSessionId);
					}

					@Override
					public void onFailure(Throwable caught) {
						getStartSessionPanel().setVisible(true);
					}
				});
			}
		});

		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);
		chartLoader.loadApi(new Runnable() {
			@Override
			public void run() {
				SimplePanel panel = new SimplePanel();
				panel.setHeight("200px");
				panel.setWidth("100%");
				panel.add(getVotesChart());
				RootPanel.get().add(panel);
			}
		});

		// String votingSessionId =
		// Window.Location.getParameter("votingSessionId");
		// if (votingSessionId != null) {
		// beginVotingSession(votingSessionId);
		// }

	}

	private Panel getStartSessionPanel() {
		if (startSessionPanel == null) {
			startSessionPanel = new FlowPanel();
		}
		return startSessionPanel;
	}

	private Panel getVotingSessionPanel() {
		if (votingSessionPanel == null) {
			votingSessionPanel = new FlowPanel();
		}
		return votingSessionPanel;
	}

	private Button getStartSessionButton() {
		if (startSessionButton == null) {
			startSessionButton = new Button();
			startSessionButton.setText("Start Session");
		}
		return startSessionButton;
	}

	private Button getStartNewVotingRoundButton() {
		if (startNewVotingRoundButton == null) {
			startNewVotingRoundButton = new Button();
			startNewVotingRoundButton.setText("Begin Voting Round");
		}
		return startNewVotingRoundButton;
	}

	private Button getEndVotingRoundButton() {
		if (endVotingRoundButton == null) {
			endVotingRoundButton = new Button();
			endVotingRoundButton.setText("End Voting Round");
		}
		return endVotingRoundButton;
	}

	private Button getCloseVotingSessionButton() {
		if (closeVotingSessionButton == null) {
			closeVotingSessionButton = new Button();
			closeVotingSessionButton.setText("End Voting Session");
		}
		return closeVotingSessionButton;
	}

	private TextBox getSessionNameTextBox() {
		if (sessionNameTextBox == null) {
			sessionNameTextBox = new TextBox();
			sessionNameTextBox.setText("New Voting Session");
		}
		return sessionNameTextBox;
	}

	Timer evaluateVotesTimer = new Timer() {
		@Override
		public void run() {
			GWT.log("Evaluating Votes for " + votingSessionId);
			votingService.getVotes(votingSessionId,
					new AsyncCallback<Map<String, Set<String>>>() {

						@Override
						public void onSuccess(Map<String, Set<String>> result) {
							GWT.log("Current votes: " + result);
							Manager.this.votes = result;
							updateVotesChart();
							evaluateVotesTimer.schedule(5000);
						}

						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Could not get votes");
							evaluateVotesTimer.schedule(5000);
						}
					});
		}
	};

	private BarChart votesChart;
	protected String[] options;
	private BarChartOptions voteChartOptions;

	private BarChart getVotesChart() {
		if (votesChart == null) {
			votesChart = new BarChart();
		}
		return votesChart;
	}

	private BarChartOptions getVotesChartOptions() {
		if (voteChartOptions == null) {
			HAxis hAxis = HAxis.create();
			hAxis.setMinValue(0);
			hAxis.setMaxValue(30);
			hAxis.setBaseline(0d);
			voteChartOptions = BarChartOptions.create();
			voteChartOptions.setHAxis(hAxis);
		}
		return voteChartOptions;
	}

	private void updateVotesChart() {
		GWT.log("Updating votes chart");
		DataTable votesData = DataTable.create();
		GWT.log("Updating votes chart data");
		votesData.addColumn(ColumnType.STRING, "Answer");
		votesData.addColumn(ColumnType.NUMBER, "Count");
		Map<String, Integer> answerCounts = new LinkedHashMap<String, Integer>();
		for (String option : options) {
			answerCounts.put(option, 0);
		}
		for (Set<String> vote : votes.values()) {
			for (String option : vote) {
				Integer count = answerCounts.get(option);
				if (count != null) {
					answerCounts.put(option, ++count);
				}
			}
		}
		GWT.log("Answers: " + answerCounts);
		for (Entry<String, Integer> e : answerCounts.entrySet()) {
			votesData.addRow(e.getKey(), e.getValue());
		}
		getVotesChart().draw(votesData, getVotesChartOptions());

	}

	private void beginVotingSession(String votingSessionId) {
		this.votingSessionId = votingSessionId;
		RootPanel.get().add(getVotingSessionPanel());
		getVotingSessionPanel().clear();
		getVotingSessionPanel().add(getStartNewVotingRoundButton());
		getStartNewVotingRoundButton().setEnabled(true);
		getVotingSessionPanel().add(getEndVotingRoundButton());
		getVotingSessionPanel().add(getCloseVotingSessionButton());
		getEndVotingRoundButton().setEnabled(false);
		getVotingSessionPanel().setVisible(true);
		getStartNewVotingRoundButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getStartNewVotingRoundButton().setEnabled(false);
				final String[] options = new String[] { "A", "B", "C" };
				votingService.beginVotingRound(Manager.this.votingSessionId,
						"New Voting Round", options, new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								Manager.this.options = options;
								getEndVotingRoundButton().setEnabled(true);
								evaluateVotesTimer.schedule(1000);
							}

							@Override
							public void onFailure(Throwable caught) {
								getStartNewVotingRoundButton().setEnabled(true);
								evaluateVotesTimer.cancel();
							}
						});
			}
		});
		getEndVotingRoundButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getEndVotingRoundButton().setEnabled(false);
				votingService.endVotingRound(Manager.this.votingSessionId,
						new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								getStartNewVotingRoundButton().setEnabled(true);
							}

							@Override
							public void onFailure(Throwable caught) {
								getEndVotingRoundButton().setEnabled(true);
							}
						});
			}
		});
		getCloseVotingSessionButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				evaluateVotesTimer.cancel();
				getVotingSessionPanel().setVisible(false);
				votingService.closeVotingSession(Manager.this.votingSessionId,
						new AsyncCallback<Void>() {

							@Override
							public void onSuccess(Void result) {
								getStartSessionPanel().setVisible(true);
							}

							@Override
							public void onFailure(Throwable caught) {
								getVotingSessionPanel().setVisible(false);
							}
						});
			}
		});
		String voterUrl = GWT.getModuleBaseURL().replace(
				GWT.getModuleName() + "/", "")
				+ "voter.html?votingSessionId=" + votingSessionId;
		getVotingSessionPanel().add(new Label("Voter URL: "));
		getVotingSessionPanel().add(new Anchor(voterUrl, voterUrl, "_blank"));
		Image image;
		image = new Image(GWT.getModuleBaseURL().replace(
				GWT.getModuleName() + "/", "")
				+ "qr/img.png?url=" + URL.encode(voterUrl));
		RootPanel.get().add(image);
	}

}
