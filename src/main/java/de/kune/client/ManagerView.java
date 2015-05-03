package de.kune.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.UIObject;
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
 * Provides the manager view.
 */
public class ManagerView {

	private final ManagerMessages messages = GWT.create(ManagerMessages.class);
	private final OptionsConstants constants = GWT
			.create(OptionsConstants.class);

	private ManagerModel model;
	private Panel mainPanel;
	private FlowPanel startSessionPanel;
	private Button startNewVotingRoundButton;
	private Button startSessionButton;
	private TextBox sessionNameTextBox;
	private ToggleButton realTimeUpdateButton;
	private Button endVotingRoundButton;
	private Button closeVotingSessionButton;
	private FlowPanel votingSessionPanel;
	private PopupPanel optionsPanel;
	private Panel buttonsPanel;
	private FlowPanel participantsPanel;
	private SimplePanel votesChartPanel;
	private ColumnChartOptions voteChartOptions;
	private ColumnChart votesChart;
	private Anchor optionsButton;

	public void initialize(Panel mainPanel) {
		this.mainPanel = mainPanel;

		this.mainPanel.add(getStartSessionPanel());
		getStartSessionPanel().add(getSessionNameTextBox());
		getStartSessionPanel().add(getStartSessionButton());
	}

	public void setModel(ManagerModel model) {
		this.model = model;
	}

	private Panel getStartSessionPanel() {
		if (startSessionPanel == null) {
			startSessionPanel = new FlowPanel();
		}
		return startSessionPanel;
	}

	public void updateVisibilityStates() {
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
			getOptionsButton().setVisible(true);
			getOptionsButton().removeStyleName("disabled");
			getVotesChartPanel().setVisible(true);
			break;
		case VOTING_ROUND_STARTED:
			getStartNewVotingRoundButton().setVisible(false);
			getEndVotingRoundButton().setVisible(true);
			getRealTimeUpdateButton().setVisible(true);
			getCloseVotingSessionButton().setVisible(false);
			getVotingSessionPanel().setVisible(true);
			getOptionsButton().setVisible(true);
			getOptionsButton().addStyleName("disabled");
			getVotesChartPanel().setVisible(true);
			break;
		default:
			break;
		}
	}

	private Panel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new FlowPanel();
			buttonsPanel.setStyleName("buttonsPanel");
//			buttonsPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		}
		return buttonsPanel;
	}

	public void setOptions(Map<String, Runnable> optionsAndActions) {
		getOptionsPanel().clear();
		Panel panel = new VerticalPanel();
		optionsPanel.add(panel);
		for (final Entry<String, Runnable> e : optionsAndActions.entrySet()) {
			RadioButton optionsRadioButton = new RadioButton("options",
					constants.getString(e.getKey()));
			optionsRadioButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					e.getValue().run();
					getOptionsButton().setText(messages.optionsButton().trim() + ": " + constants.getString(e.getKey()));
					getOptionsPanel().hide();
				}
			});
			if (!panel.iterator().hasNext()) {
				e.getValue().run();
				optionsRadioButton.setValue(true);
				getOptionsButton().setText(messages.optionsButton().trim() + ": " + constants.getString(e.getKey()));
			}
			panel.add(optionsRadioButton);
		}
	}

	private PopupPanel getOptionsPanel() {
		if (optionsPanel == null) {
			optionsPanel = new PopupPanel(true, false);
			optionsPanel.setGlassEnabled(true);
			optionsPanel.setAnimationEnabled(true);
		}
		return optionsPanel;
	}

	private Panel getVotingSessionPanel() {
		if (votingSessionPanel == null) {
			votingSessionPanel = new FlowPanel();
		}
		return votingSessionPanel;
	}

	private ToggleButton getRealTimeUpdateButton() {
		if (realTimeUpdateButton == null) {
			realTimeUpdateButton = new ToggleButton(
					messages.realTimeUpdateToggleButton());
		}
		return realTimeUpdateButton;
	}

	private TextBox getSessionNameTextBox() {
		if (sessionNameTextBox == null) {
			sessionNameTextBox = new TextBox();
			sessionNameTextBox.setText(messages.votingSessionLabel());
		}
		return sessionNameTextBox;
	}

	private Button getStartSessionButton() {
		if (startSessionButton == null) {
			startSessionButton = new Button();
			startSessionButton.setText(messages.beginVotingSessionButton());
		}
		return startSessionButton;
	}

	private Button getStartNewVotingRoundButton() {
		if (startNewVotingRoundButton == null) {
			startNewVotingRoundButton = new Button();
			startNewVotingRoundButton
					.setText(messages.beginVotingRoundButton());
		}
		return startNewVotingRoundButton;
	}

	private Button getCloseVotingSessionButton() {
		if (closeVotingSessionButton == null) {
			closeVotingSessionButton = new Button();
			closeVotingSessionButton.setText(messages.endVotingSessionButton());
		}
		return closeVotingSessionButton;
	}

	private Button getEndVotingRoundButton() {
		if (endVotingRoundButton == null) {
			endVotingRoundButton = new Button();
			endVotingRoundButton.setText(messages.endVotingRoundButton());
		}
		return endVotingRoundButton;
	}

	public void updateParticipantsPanel() {
		Label participantsLabel = getParticipantsLabel();
		participantsLabel.setText(messages.votesLabel(model.getVoteCount()));
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

	public void closeOptionsPanel() {
		getOptionsPanel().hide();
	}

	private SimplePanel getVotesChartPanel() {
		if (votesChartPanel == null) {
			votesChartPanel = new SimplePanel();
			votesChartPanel.setHeight("350px");
			votesChartPanel.setWidth("400px");
		}
		return votesChartPanel;
	}

	public void beginSession(final Runnable postChartLoaderRunnable) {
		getStartSessionPanel().setVisible(false);
		mainPanel.add(getVotingSessionPanel());
		getVotingSessionPanel().clear();

		getButtonsPanel().add(getStartNewVotingRoundButton());
		getButtonsPanel().add(getEndVotingRoundButton());
		getButtonsPanel().add(getRealTimeUpdateButton());
		getButtonsPanel().add(getCloseVotingSessionButton());
		VerticalPanel vp = new VerticalPanel();
		vp.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		vp.add(getOptionsButton());
		getButtonsPanel().add(vp);
		getVotingSessionPanel().add(getButtonsPanel());

		SimplePanel pinCodePanel = new SimplePanel();
		pinCodePanel.setStyleName("pinCodePanel");
		getVotingSessionPanel().add(pinCodePanel);
		pinCodePanel.add(new Label(model.getVotingSessionPin()));

		getVotingSessionPanel().add(getVotesChartPanel());
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);
		chartLoader.loadApi(new Runnable() {
			@Override
			public void run() {
				GWT.log("Loaded " + ChartPackage.CORECHART);
				getVotesChartPanel().setVisible(false);
				getVotesChartPanel().add(getVotesChart());

				postChartLoaderRunnable.run();
			}
		});

		mainPanel.add(getParticipantsPanel());
	}

	private Anchor getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new Anchor(messages.optionsButton());
			optionsButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (model.getState() == State.VOTING_SESSION_STARTED) {
						getOptionsPanel().center();
						getOptionsPanel().setPopupPosition(getOptionsPanel().getPopupLeft(), 170);
					}
				}
			});
		}
		return optionsButton;
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
			voteChartOptions.setColors("#06265c");
		}
		return voteChartOptions;
	}

	public void updateVotesChart(boolean redraw) {
		GWT.log("Updating votes chart");
		DataTable votesData = DataTable.create();
		GWT.log("Updating votes chart data");
		votesData.addColumn(ColumnType.STRING, messages.answerColumnLabel());
		votesData
				.addColumn(ColumnType.NUMBER, messages.percentageColumnLabel());
		Map<String, Integer> answerCounts = new LinkedHashMap<String, Integer>();
		if (model.getOptionsKey() != null) {
			for (int i = 0; i < constants.getStringArray(model.getOptionsKey()).length; i++) {
				answerCounts.put(Integer.toString(i), 0);
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
				votesData.addRow(
						constants.getStringArray(model.getOptionsKey())[Integer
								.parseInt(e.getKey())], 0);
			} else {
				votesData.addRow(
						constants.getStringArray(model.getOptionsKey())[Integer
								.parseInt(e.getKey())], (Double) ((double) e
								.getValue() / (double) votesCount));
			}
		}
		getVotesChart().draw(votesData, getVotesChartOptions());
		if (redraw) {
			getVotesChart().redraw();
		}
	}

	public UIObject getMainPanel() {
		return mainPanel;
	}

	public boolean isRealTimeUpdateSelected() {
		return getRealTimeUpdateButton().isDown();
	}

	public void startVotingSessionInNewWindow() {
		Window.open(GWT.getHostPageBaseURL()
				+ "manager.html?startVotingSession="
				+ getSessionNameTextBox().getText(), "_blank",
				"right=20,top=20,width=400,height=800,toolbar=0,resizable=0");
	}

	public void setStartNewVotingRoundAction(final Runnable runnable) {
		getStartNewVotingRoundButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				runnable.run();
			}
		});
	}

	public void setEndVotingRoundAction(final Runnable runnable) {
		getEndVotingRoundButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				runnable.run();
			}
		});
	}

	public void setCloseVotingSessionAction(final Runnable runnable) {
		getCloseVotingSessionButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				runnable.run();
			}
		});
	}

	public void setRealTimeUpdateActions(final Runnable start,
			final Runnable stop) {
		getRealTimeUpdateButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (((ToggleButton) event.getSource()).isDown()) {
					start.run();
				} else {
					stop.run();
				}
			}
		});
	}

	public void setStartVotingSessionAction(final Runnable runnable) {
		getStartSessionButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				runnable.run();
			}
		});
	}

	public native void closeWindow() /*-{
										$wnd.close();
										}-*/;

}
