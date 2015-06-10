package de.kune.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

import de.kune.client.common.SearchUtils;
import de.kune.client.voter.VoterController;
import de.kune.client.voter.VoterModel;
import de.kune.client.voter.VoterView;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Voter implements EntryPoint {

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
			if (e.getTagName().toUpperCase().equals("DIV")) {
				String id = e.getId();
				if (e.getId() == null || e.getId().isEmpty()) {
					id = "voting-client-" + Random.nextInt();
					e.setId(id);
				}
				VoterModel model = new VoterModel();
				VoterView view = new VoterView();
				VoterController controller = new VoterController();
				view.initialize(RootPanel.get(id));
				view.setModel(model);
				controller.initialize(model, view);
			}
		}
	}

}
