package de.kune.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class VoterModule implements EntryPoint {

	private final Messages messages = GWT.create(Messages.class);

	protected Panel mainPanel() {
		if (RootPanel.get("voting-client") == null) {
			FlowPanel votingPanel = new FlowPanel();
			votingPanel.getElement().setId("voting-client");
			RootPanel.get().add(votingPanel);
		}
		return RootPanel.get("voting-client");
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		for (Element e : SearchUtils.findElementsForClass(RootPanel.get()
				.getElement(), "voting-client")) {
			GWT.log(e.getTagName());
			if (e.getTagName().toUpperCase().equals("DIV")) {
				String id = e.getId();
				if (e.getId() == null || e.getId().isEmpty()) {
					id = "voting-client-" + Random.nextInt();
					e.setId(id);
				}
				GWT.log(id);
				new Voter(RootPanel.get(id), messages);
			}
		}
		// List<Element> element = ;
		// if (votingSessionId == null) {
		// FlowPanel pinPanel = new FlowPanel();
		// mainPanel().add(pinPanel);
		// pinPanel.add(new Label(messages.enterPin()));
		// final TextBox pinTextBox = new TextBox();
		// pinPanel.add(pinTextBox);
		// Button joinButton = new Button(messages.joinButton());
		// pinPanel.add(joinButton);
		// joinButton.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent event) {
		// joinSession(pinTextBox.getText().trim());
		// }
		// });
		// } else {
		// joinSession(votingSessionId);
		// }
//		new Voter(mainPanel(), messages);
	}

}
