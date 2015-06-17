package de.kune.client;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

import de.kune.client.manager.ManagerController;
import de.kune.client.manager.ManagerModel;
import de.kune.client.manager.ManagerView;

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
		result.put("abcLabel", new Runnable() {
			@Override
			public void run() {
				model.updateOptions("abc", true);
			}
		});
		result.put("yesNoLabel", new Runnable() {
			@Override
			public void run() {
				model.updateOptions("yesNo", false);
			}
		});
		result.put("scrumLabel", new Runnable() {
			@Override
			public void run() {
				model.updateOptions("scrum", false);
			}
		});
		return result;
	}

}
