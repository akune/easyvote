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
import com.google.gwt.user.client.Window;
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
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.ColumnChart;
import com.googlecode.gwt.charts.client.corechart.ColumnChartOptions;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.googlecode.gwt.charts.client.options.VAxis;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Manager implements EntryPoint {

	public static native void closeWindow() /*-{
		$wnd.close();
	}-*/;
	
	private Button closeVotingSessionButton;
	private Button endVotingRoundButton;
	private ToggleButton realTimeUpdateButton;
	private final ClickHandler closeVotingSessiongHandler=new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			evaluateVotesTimer.cancel();
			getVotingSessionPanel().setVisible(false);
			votingService.closeVotingSession(Manager.this.votingSessionId,
					new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							getVotingSessionPanel().setVisible(false);
						}

						@Override
						public void onSuccess(Void result) {
							getStartSessionPanel().setVisible(true);
							closeWindow();
						}
					});
		}
	};
	private final ClickHandler endVotingRoundClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			getEndVotingRoundButton().setVisible(false);
			votingService.endVotingRound(Manager.this.votingSessionId,
					new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							getEndVotingRoundButton().setVisible(true);
							getRealTimeUpdateButton().setVisible(true);
							evaluateVotes();
						}

						@Override
						public void onSuccess(Void result) {
							getStartNewVotingRoundButton().setVisible(true);
							getRealTimeUpdateButton().setVisible(false);
							getCloseVotingSessionButton().setVisible(true);
							evaluateVotes();
						}
					});
		}
	};
	private final Timer evaluateVotesTimer = new Timer() {
		@Override
		public void run() {
			GWT.log("Evaluating Votes for " + votingSessionId);
			votingService.getVotes(votingSessionId,
					new AsyncCallback<Map<String, Set<String>>>() {

						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Could not get votes");
							evaluateVotesTimer.schedule(1000);
						}

						@Override
						public void onSuccess(Map<String, Set<String>> result) {
							GWT.log("Current votes: " + result);
							Manager.this.votes = result;
							updateVotesChart();
							evaluateVotesTimer.schedule(1000);
						}
					});
		}
	};
	private final ClickHandler newVotingRoundClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			getStartNewVotingRoundButton().setVisible(false);
			final String[] options = new String[] { "A", "B", "C" };
			votingService.beginVotingRound(Manager.this.votingSessionId,
					"New Voting Round", options, new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							getStartNewVotingRoundButton().setVisible(true);
							getRealTimeUpdateButton().setVisible(false);
							evaluateVotesTimer.cancel();
						}

						@Override
						public void onSuccess(Void result) {
							Manager.this.options = options;
//							getVoterQrCodeLink().setVisible(false);
							getVotesChartPanel().setVisible(true);
							getVotingSessionPanel().setVisible(true);
							resetVotesChart();
							updateVotesChart();
							getVotesChart().redraw();
							getEndVotingRoundButton().setVisible(true);
							getRealTimeUpdateButton().setVisible(true);
							getCloseVotingSessionButton().setVisible(false);
							if (getRealTimeUpdateButton().isDown()) {
								evaluateVotesTimer.schedule(1000);
							}
						}
					});
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
	private String[] options;
	private TextBox sessionNameTextBox;
	private Button startNewVotingRoundButton;
	private Button startSessionButton;
	private Panel startSessionPanel;
	private final String startVotingSession = Window.Location
			.getParameter("startVotingSession");
	private ColumnChartOptions voteChartOptions;
	private Anchor voterImageQrLink;
	protected Map<String, Set<String>> votes;
	private ColumnChart votesChart;
	private SimplePanel votesChartPanel;

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final VotingServiceAsync votingService = GWT
			.create(VotingService.class);

	private String votingSessionId;
	private FlowPanel votingSessionPanel;
	private FlowPanel buttonsPanel;
	
	private FlowPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new FlowPanel();
		}
		return buttonsPanel;
	}
	
	private void beginVotingSession(String votingSessionId) {
		this.votingSessionId = votingSessionId;
		RootPanel.get().add(getVotingSessionPanel());
		getVotingSessionPanel().clear();
		
		getButtonsPanel().add(getStartNewVotingRoundButton());
		getButtonsPanel().add(getEndVotingRoundButton());
		getButtonsPanel().add(getRealTimeUpdateButton());
		getButtonsPanel().add(getCloseVotingSessionButton());
		getVotingSessionPanel().add(getButtonsPanel());
		
		getStartNewVotingRoundButton().setVisible(true);
		getRealTimeUpdateButton().setVisible(false);
		getEndVotingRoundButton().setVisible(false);
		getVotingSessionPanel().setVisible(true);
		getStartNewVotingRoundButton().addClickHandler(newVotingRoundClickHandler);
		getEndVotingRoundButton().addClickHandler(endVotingRoundClickHandler);
		getCloseVotingSessionButton().addClickHandler(closeVotingSessiongHandler);
		getRealTimeUpdateButton().addClickHandler(realTimeUpdateClickHandler);

		SimplePanel pinCodePanel = new SimplePanel();
		pinCodePanel.setStyleName("pinCodePanel");
		getVotingSessionPanel().add(pinCodePanel);
		pinCodePanel.add(new Label("Pin: " + votingSessionId));

		SimplePanel qrCodePanel = new SimplePanel();
		getVotingSessionPanel().add(qrCodePanel);
		updateVoterImageQrLink();
		qrCodePanel.add(getVoterQrCodeLink());
		getVotingSessionPanel().add(getVotesChartPanel());
		
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);
		chartLoader.loadApi(new Runnable() {
			@Override
			public void run() {
				GWT.log("Loaded " + ChartPackage.CORECHART);
				getVotesChartPanel().setVisible(false);
				getVotesChartPanel().add(getVotesChart());
			}
		});

	}
	private void evaluateVotes() {
		GWT.log("Evaluating Votes for " + votingSessionId);
		votingService.getVotes(votingSessionId,
				new AsyncCallback<Map<String, Set<String>>>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Could not get votes");
					}

					@Override
					public void onSuccess(Map<String, Set<String>> result) {
						GWT.log("Current votes: " + result);
						Manager.this.votes = result;
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

		// RootPanel.get().add(new Label("Welcome to Easy Vote Manager"));
		RootPanel.get().add(getStartSessionPanel());
		getStartSessionPanel().add(getSessionNameTextBox());
		getStartSessionPanel().add(getStartSessionButton());
		getStartSessionButton().addClickHandler(startSessionClickHandler);

		// String votingSessionId =
		// Window.Location.getParameter("votingSessionId");
		// if (votingSessionId != null) {
		// beginVotingSession(votingSessionId);
		// }

		if (startVotingSession != null) {
			getStartSessionPanel().setVisible(false);
			votingService.createVotingSession(startVotingSession,
					new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							getStartSessionPanel().setVisible(true);
						}

						@Override
						public void onSuccess(String votingSessionId) {
							beginVotingSession(votingSessionId);
						}
					});
		}

	}

	private void resetVotesChart() {
		votes = null;
	}

	private void updateVoterImageQrLink() {
		String voterUrl = GWT.getModuleBaseURL().replace(
				GWT.getModuleName() + "/", "")
				+ "voter.html?votingSessionId=" + votingSessionId;
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
		if (options != null) {
			for (String option : options) {
				answerCounts.put(option, 0);
			}
		}
		int votesCount = 0;
		GWT.log("Votes: " + votes);
		if (votes != null) {
			votesCount = votes.keySet().size();
			for (Set<String> vote : votes.values()) {
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
