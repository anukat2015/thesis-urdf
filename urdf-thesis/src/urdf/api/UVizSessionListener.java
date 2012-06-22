/**
 * 
 */
package urdf.api;

import flex.messaging.FlexSession;
import flex.messaging.FlexSessionListener;

/**
 * @author Timm Meiser
 * 
 */
public class UVizSessionListener implements FlexSessionListener {

	/**
	 * 
	 */
	public UVizSessionListener() {
		// TODO Auto-generated constructor stub
	}

	public void sessionCreated(FlexSession session) {
		// System.out.println("FlexSession created : " + session.getId());
		// Add the FlexSession destroyed listener.
		session.addSessionDestroyedListener(this);
	}

	public void sessionDestroyed(FlexSession session) {
		// System.out.println("FlexSession destroyed : " + session.getId());
		try {
			UReasoner.killReasoner(session.getId());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
