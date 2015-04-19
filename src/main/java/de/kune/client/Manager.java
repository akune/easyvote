package de.kune.client;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Manager implements EntryPoint {

	private ManagerModel model = new ManagerModel();
	private ManagerView view = new ManagerView();
	private ManagerController controller = new ManagerController();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		view.initialize(mainPanel());
		view.setModel(model);
		view.setOptions(options());
		controller.initialize(model, view);
	}

	private Panel mainPanel() {
		return RootPanel.get("voting-manager");
	}

	private Map<String, Runnable> options() {
		Map<String, Runnable> result = new LinkedHashMap<String, Runnable>();
		result.put("abcOptionsLabel", new Runnable() {
			@Override
			public void run() {
				model.updateOptions(new String[] { "A", "B", "C" }, true);
			}
		});
		result.put("yesNoOptionsLabel", new Runnable() {
			@Override
			public void run() {
				model.updateOptions(new String[] { "Yes", "No" }, false);
			}
		});
		return result;
	}

}
