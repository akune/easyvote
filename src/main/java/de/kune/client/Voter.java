package de.kune.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

import de.kune.client.voter.VoterMVC;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Voter implements EntryPoint {

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
				new VoterMVC(RootPanel.get(id), messages);
			}
		}
	}

}
